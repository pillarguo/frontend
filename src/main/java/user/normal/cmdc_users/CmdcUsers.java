package user.normal.cmdc_users;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import user.normal.bean.CmdcUser;
import user.normal.bean.MessageBean;
import user.normal.dao.Constant;
import user.normal.dao.UsersAOP;
import user.normal.jwt.ExpirationTime;
import user.normal.jwt.KeyUtil;
import user.normal.jwt.LoginVerify;
import user.normal.jwt.TokenUtil;

@Path("cmdc_user")
@Singleton
public class CmdcUsers {

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("user_login")
	public MessageBean postIt(@BeanParam CmdcUser cmdcUser, @FormParam("identify") String identify,
			@Context HttpServletRequest req, @Context HttpServletResponse res) throws UnsupportedEncodingException {
		MessageBean messageBean = new MessageBean();
		// 验证码
		String rand = (String) req.getSession().getAttribute("rand");
		if (identify == null || identify.length() <= 0) {
			messageBean.setResultCode("-2");
			messageBean.setResultMsg("验证码为空");
			return messageBean;
		}
		// 将前端传来的验证码转化为大写字母（数字不变），然后比较
		if (!identify.toUpperCase().equals(rand)) {
			messageBean.setResultCode("-1");
			messageBean.setResultMsg("验证码不正确");
			return messageBean;
		}

		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/lists");

		webTarget = webTarget.queryParam("userName", cmdcUser.getUserName());
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		@SuppressWarnings("unchecked")
		List<CmdcUser> adminList = response.readEntity(List.class);
		response.close();
		if (adminList.size() != 0) {
			JSONArray json = JSONArray.fromObject(adminList);
			JSONObject job = json.getJSONObject(0);

			if (job.getString("password").equals(cmdcUser.getPassword())) {
				
				String key = KeyUtil.getKey();
				// 这是一个时间戳，代表token的过期时间
				ExpirationTime exp = new ExpirationTime();
				Date expiration = exp.getExpiration();
				// 生成token

				String jwtString = TokenUtil.getJWTString(job.getString("userId"), job.getString("roleType"),
						expiration, key);
				System.out.println("job.getString-userId" + job.getString("userId"));
				// 生成cookie
				Cookie cookie = new Cookie("jwtf", jwtString);
				// cookie在客户端存储3600*12秒
				cookie.setMaxAge(3600 * 12);
				// 设置cookie的应用路径
				cookie.setPath("/");
				// String domain = DomainUtil.getDomain();
				// String domain = req.getHeader("host");
				// System.out.println("domain"+domain);
				// cookie.setDomain(req.getHeader("host"));
				cookie.setHttpOnly(true);
				res.addCookie(cookie);

				// 生成cookie,存储真实姓名
				// Cookie cookie2 =new Cookie("realName",
				// URLEncoder.encode(job.getString("realName"), "UTF-8"));
				Cookie cookie2 = new Cookie("nickname",
						URLEncoder.encode("\"" + job.getString("nickname") + "\"", "UTF-8"));
				// Cookie cookie2 =new Cookie("nickname",
				// "\""+job.getString("nickname")+"\"");
				// cookie在客户端存储3600*12秒
				cookie2.setMaxAge(3600 * 12);
				// 设置cookie的应用路径
				cookie2.setPath("/");
				// cookie2.setDomain(domain);
				cookie2.setHttpOnly(false);
				res.addCookie(cookie2);

				// 生成cookie,存储用户id
				Cookie cookie3 = new Cookie("userId", job.getString("userId"));
				// cookie在客户端存储3600*12秒
				cookie3.setMaxAge(3600 * 12);
				// 设置cookie的应用路径
				cookie3.setPath("/");
				// cookie3.setDomain(domain);
				cookie3.setHttpOnly(false);
				res.addCookie(cookie3);

				// 生成cookie,存储用户角色
				// Cookie cookie4 = new Cookie("roleType",
				// URLEncoder.encode("\""+job.getString("roleType")+"\"",
				// "UTF-8"));
				Cookie cookie4 = new Cookie("roleTypef", job.getString("roleType"));
				// cookie在客户端存储3600秒
				cookie4.setMaxAge(3600 * 12);
				// 设置cookie的应用路径
				cookie4.setPath("/");
				// cookie4.setDomain(domain);
				cookie4.setHttpOnly(false);
				res.addCookie(cookie4);

				// 更新“登录时间”字段
				CmdcUser user_new = new CmdcUser();
				user_new.setLoginTime(new Date());
				WebTarget wt = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/");
				wt = wt.path((job.getString("userId")));
				final Invocation.Builder ib = wt.request();
				Response resp = ib.buildPut(Entity.entity(user_new, MediaType.APPLICATION_JSON)).invoke();
				MessageBean aff = resp.readEntity(MessageBean.class);
				resp.close();

				
				return aff;

			}

		}
		messageBean.setResultCode("0");
		messageBean.setResultMsg("用户名或密码不正确");
		return messageBean;
	}

	@SuppressWarnings("unchecked")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("new_user")
	public MessageBean new_user(@BeanParam CmdcUser cmdcUser, @FormParam("identify") String identify,
			@Context HttpServletRequest req, @Context HttpServletResponse res) {
		MessageBean messageBean = new MessageBean();

		// 验证码
		String rand = (String) req.getSession().getAttribute("rand");

		if (cmdcUser.getUserName().length() > 20 || cmdcUser.getUserName().length() <= 0) {
			messageBean.setResultCode("-1");
			messageBean.setResultMsg("用户名长度有误");
			return messageBean;
		} /*
			 * else if (identify == null || "".equals(identify) ||
			 * !identify.toUpperCase().equals(rand))//
			 * 将前端传来的验证码转化为大写字母（数字不变），然后比较 { messageBean.setResultCode("-1");
			 * messageBean.setResultMsg("验证码不正确"); return messageBean; }
			 */

		cmdcUser.setCreateTime(new Date());
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/new_user");
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.buildPost(Entity.entity(cmdcUser, MediaType.APPLICATION_JSON)).invoke();
		messageBean = response.readEntity(MessageBean.class);
		if (messageBean.getResultCode().equals("0")) {
			// 判断用户名是否已存在
			WebTarget wt1 = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/lists")
					.queryParam("userName", cmdcUser.getUserName());
			final Invocation.Builder ib1 = wt1.request();
			Response resp1 = ib1.get();
			List<CmdcUser> userlist1 = resp1.readEntity(List.class);
			resp1.close();
			if (userlist1.size() > 0) {
				messageBean.setResultCode("-2");
				messageBean.setResultMsg("用户名已存在");
				return messageBean;
			}
			// 判断昵称是否已存在
			WebTarget wt2 = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/lists")
					.queryParam("nickname", cmdcUser.getNickname());
			final Invocation.Builder ib2 = wt2.request();
			Response resp2 = ib2.get();
			List<CmdcUser> userlist2 = resp2.readEntity(List.class);
			resp2.close();
			if (userlist2.size() > 0) {
				messageBean.setResultCode("-2");
				messageBean.setResultMsg("昵称已存在");
				return messageBean;
			}
		}

		return messageBean;
	}

	@GET
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{user_id}")
	public String getadminid(@PathParam("user_id") String user_id, @Context HttpServletRequest req,
			@Context HttpServletResponse res) {
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/");
		webTarget = webTarget.path(user_id);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		String result = response.readEntity(String.class);
		response.close();
		return result;
	}

	@PUT
	@LoginVerify	
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{user_id}")
	public MessageBean putadminid(@PathParam("user_id") String user_id, @BeanParam CmdcUser cmdcUser,
			@CookieParam("jwtf") String tokenstr,@Context HttpServletRequest req, @Context HttpServletResponse res) throws ServletException, IOException {
		MessageBean messageBean = new MessageBean();
		if (cmdcUser.getPhoneNumber() != null) {
			String regEx = "1{1}[0-9]{10}";
			Pattern pattern = Pattern.compile(regEx);
			Matcher matcher = pattern.matcher(cmdcUser.getPhoneNumber());
			if (matcher.matches() == false || cmdcUser.getPhoneNumber().length() != 11) {
				messageBean.setResultCode("-1");
				messageBean.setResultMsg("手机格式有误");
				return messageBean;
			}
		} else if (cmdcUser.getUserName() != null
				&& (cmdcUser.getUserName().length() > 50 || cmdcUser.getUserName().length() <= 0)) {
			messageBean.setResultCode("-1");
			messageBean.setResultMsg("用户名长度有误");
			return messageBean;
		} else if (cmdcUser.getNickname() != null
				&& (cmdcUser.getNickname().length() > 50 || cmdcUser.getNickname().length() < 0)) {
			messageBean.setResultCode("-1");
			messageBean.setResultMsg("昵称长度有误");
			return messageBean;
		} else if (cmdcUser.getRealName() != null
				&& (cmdcUser.getRealName().length() > 50 || cmdcUser.getRealName().length() < 0)) {
			messageBean.setResultCode("-1");
			messageBean.setResultMsg("真实姓名长度有误");
			return messageBean;
		}

		UsersAOP usersAOP = new UsersAOP();
		messageBean = usersAOP.put_auth(user_id, tokenstr);
		if (messageBean.getResultCode().equals("0")) {
			return messageBean;
		}
		
		cmdcUser.setUpdateTime(new Date());
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/");
		webTarget = webTarget.path(user_id);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.buildPut(Entity.entity(cmdcUser, MediaType.APPLICATION_JSON)).invoke();
		MessageBean aff = response.readEntity(MessageBean.class);
		response.close();

		if (cmdcUser.getNickname() != null) {
			// 生成cookie,存储真实姓名
			Cookie cookie2 = new Cookie("nickname", "\"" + URLEncoder.encode(cmdcUser.getNickname() + "\"", "UTF-8"));
			// cookie在客户端存储3600*12秒
			cookie2.setMaxAge(3600 * 12);
			// 设置cookie的应用路径
			cookie2.setPath("/");
			// cookie2.setDomain(domain);
			cookie2.setHttpOnly(false);
			res.addCookie(cookie2);
		}

		return aff;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("lists")
	public String lists(@QueryParam("nickname") String nickname, @QueryParam("userName") String userName,
			@Context HttpServletRequest req, @Context HttpServletResponse res) {
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/lists");
		webTarget = webTarget.queryParam("nickname", nickname);
		webTarget = webTarget.queryParam("userName", userName);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		String result = response.readEntity(String.class);
		response.close();
		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("handwork_lists")
	public String handwork_lists(@QueryParam("nickname") String nickname, @QueryParam("userName") String userName) {
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/handwork_lists");
		webTarget = webTarget.queryParam("nickname", nickname);
		webTarget = webTarget.queryParam("userName", userName);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		String result = response.readEntity(String.class);
		response.close();
		return result;
	}

	@POST
	@LoginVerify
	@Produces(MediaType.APPLICATION_JSON)
	@Path("quit")
	public MessageBean delCookie(@CookieParam("userId") String userId,@Context HttpServletRequest req, @Context HttpServletResponse res) {
		MessageBean messageBean = new MessageBean();
		CmdcUser cmdcUser = new CmdcUser();

		/*// 获取userId
		Cookie[] cookies = req.getCookies();
		String userId = "";
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			String cookieName = cookie.getName();
			if (cookieName.equals("userId")) {
				userId = cookie.getValue();
				break;
			}
		}*/
		if (userId.trim().length() == 0) {
			messageBean.setResultCode("0");
			messageBean.setResultMsg("未登录");
			return messageBean;
		}

		cmdcUser.setLogoutTime(new Date());
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget wt = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/cmdc_user/");
		wt = wt.path(userId);
		final Invocation.Builder ib = wt.request();
		Response resp = ib.buildPut(Entity.entity(cmdcUser, MediaType.APPLICATION_JSON)).invoke();
		MessageBean aff = resp.readEntity(MessageBean.class);
		resp.close();

		// 生成cookie
		Cookie cookie = new Cookie("jwtf", null);
		// cookie在客户端存储0秒
		cookie.setMaxAge(0);
		// 设置cookie的应用路径
		cookie.setPath("/");
		res.addCookie(cookie);

		// 生成cookie
		Cookie cookie2 = new Cookie("nickname", null);
		// cookie在客户端存储0秒
		cookie2.setMaxAge(0);
		// 设置cookie的应用路径
		cookie2.setPath("/");
		res.addCookie(cookie2);

		// 生成cookie
		Cookie cookie3 = new Cookie("userId", null);
		// cookie在客户端存储3600秒
		cookie3.setMaxAge(0);
		// 设置cookie的应用路径
		cookie3.setPath("/");
		res.addCookie(cookie3);

		// 生成cookie
		Cookie cookie4 = new Cookie("roleTypef", null);
		// cookie在客户端存储3600秒
		cookie4.setMaxAge(0);
		// 设置cookie的应用路径
		cookie4.setPath("/");
		res.addCookie(cookie4);

		messageBean.setResultCode("1");
		messageBean.setResultMsg("成功");
		return messageBean;

	}

}
