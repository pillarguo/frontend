package user.normal.postings;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.ws.rs.CookieParam;
import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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

import org.apache.lucene.queryparser.classic.ParseException;
import org.glassfish.jersey.client.ClientConfig;

import net.sf.json.JSONObject;
import user.normal.bean.MessageBean;
import user.normal.bean.Page;
import user.normal.bean.Posting;
import user.normal.dao.Constant;
import user.normal.dao.Lucenes;
import user.normal.jwt.KeyUtil;
import user.normal.jwt.LoginVerify;
import user.normal.jwt.NewToken;
import user.normal.jwt.TokenUtil;

@Path("postings")
@Singleton
public class Postings {

	@POST
	@LoginVerify
	@RolesAllowed({"U"})
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("new_posting")
	public MessageBean postIt(@CookieParam("jwtf") String tokenstr,@BeanParam Posting posting,
			@Context HttpServletRequest req,
			@Context HttpServletResponse res) throws IOException {
		MessageBean messageBean=new MessageBean();
		if (posting.getPostingTitle().length()>100 || posting.getPostingTitle().length() <= 0) {
			messageBean.setResultCode("-1");
			messageBean.setResultMsg("标题长度有误");
			return messageBean;
		}
		posting.setCreateTime(new Date());
		
		String key = KeyUtil.getKey();// 获取秘钥
		String id = TokenUtil.getId(tokenstr, key);// 获取用户id

		posting.setUserId(Integer.valueOf(id));
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/postings/new_posting");

		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.buildPost(Entity.entity(posting, MediaType.APPLICATION_JSON)).invoke();

		messageBean = response.readEntity(MessageBean.class);		
		response.close();

		if (messageBean.getResultCode().equals("1")) {			
			Lucenes lucene= new Lucenes();
			//String realPath=req.getSession().getServletContext().getRealPath("/");
			
			posting.setPostingId(Integer.valueOf(messageBean.getResultMsg()));
			lucene.createIndex(posting);
			messageBean.setResultMsg("发表成功");
					}
		
		return messageBean;
	}

	//搜索列表
		@GET
		@Consumes("application/x-www-form-urlencoded")
		@Produces(MediaType.APPLICATION_JSON)
		@Path("search_list")
		public List<Posting> search(@QueryParam("keyword") String keyword, @Context HttpServletRequest req,
				@Context HttpServletResponse res) throws IOException, ParseException {
			
			Lucenes lucene= new Lucenes();		
			List<Posting> result=lucene.search(keyword);
			return result;
			
		}
		
		//分页搜索
		@GET
		@Consumes("application/x-www-form-urlencoded")
		@Produces(MediaType.APPLICATION_JSON)
		@Path("search_page")
		public Page<Posting> search_page(@QueryParam("keyword") String keyword,@QueryParam("pageNum") String pageNum, @QueryParam("pageSize") String pageSize, @Context HttpServletRequest req,
				@Context HttpServletResponse res) throws IOException, ParseException {
			if(pageNum==null||pageNum.length()<=0){
				pageNum="1";
			}
			if(pageSize==null||pageSize.length()<=0){
				pageSize="20";
			}
			Lucenes lucene= new Lucenes();
			//String realPath=req.getSession().getServletContext().getRealPath("/");
			if(keyword==null){
				Page<Posting> result=null;
				return result;
			}
			keyword=keyword.replaceAll("[^0-9a-zA-Z\u4e00-\u9fa5.，,。？“”]+","");
			Page<Posting> result=lucene.search_page(keyword,pageNum,pageSize);
			
			return result;
			
		}
		
		
	@GET
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{posting_id}")
	public String get_by_id(@PathParam("posting_id") String posting_id, @Context HttpServletRequest req,
			@Context HttpServletResponse res) {
		// 创建Client
		ClientConfig clientConfig = new ClientConfig();		
		Client client = ClientBuilder.newClient(clientConfig);
		//获取帖子
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/postings/");
		webTarget = webTarget.path(posting_id);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		String posting = response.readEntity(String.class);		
		response.close();
		
		JSONObject myJsonObject = JSONObject.fromObject(posting);//转化为json对象	   
		String  readingTimes = myJsonObject.getString("readingTimes");
		//修改阅读次数
		WebTarget wt = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/postings/");
		webTarget = wt.path(posting_id);
		Posting posting2=new Posting();
		posting2.setReadingTimes(Integer.valueOf(readingTimes)+1);
		final Invocation.Builder ib = webTarget.request();			
		Response resp = ib.buildPut(Entity.entity(posting2, MediaType.APPLICATION_JSON)).invoke();
		MessageBean aff = resp.readEntity(MessageBean.class);
		resp.close();	
		
		return posting;
	}

	@PUT
	@LoginVerify
	@RolesAllowed({"U"})
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{posting_id}")
	public MessageBean put_by_id(@PathParam("posting_id") String posting_id, @BeanParam Posting posting,
			@CookieParam("jwtf") String tokenstr,
			@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException, ParseException {
		MessageBean messageBean=new MessageBean();
		if (posting.getPostingTitle() != null && (posting.getPostingTitle().length() > 100
				|| posting.getPostingTitle().length() <= 0)) {
			messageBean.setResultCode("-1");
			messageBean.setResultMsg("标题长度有误");
			return messageBean;
		} 
		/*// 获取token
		Cookie[] cookies = req.getCookies();
		String tokenstr = "";
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			String cookieName = cookie.getName();
			if (cookieName.equals("jwtf")) {
				tokenstr = cookie.getValue();
				break;
			}
		}*/
		String key = KeyUtil.getKey();// 获取秘钥
		String id = TokenUtil.getId(tokenstr, key);// 获取用户id
		if (posting.getUserId() != Integer.valueOf(id)) {
			messageBean.setResultCode("0");
			messageBean.setResultMsg("无权修改");
			return messageBean;
		}

		posting.setUpdateTime(new Date());
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/postings/");
		webTarget = webTarget.path(posting_id);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.buildPut(Entity.entity(posting, MediaType.APPLICATION_JSON)).invoke();
		messageBean = response.readEntity(MessageBean.class);		
		response.close();

		if(messageBean.getResultCode().equals("1")&&posting.getPostingTitle()!=null){//更新索引
			posting.setPostingId(Integer.valueOf(posting_id));			
			Lucenes lucene=new Lucenes();			
			lucene.update(posting);
			
		}	
		return messageBean;
	}

	@GET
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("pages")
	public String pages(@QueryParam("pageNum") String pageNum, @QueryParam("pageSize") String pageSize,
			 @QueryParam("displayStatus") String displayStatus, @QueryParam("essence") String essence,
			 @QueryParam("authentication") String authentication, @QueryParam("postingWhether") String postingWhether,
			 @QueryParam("postingOrderBy") String postingOrderBy, @QueryParam("section") String section
			 , @QueryParam("userId") String userId) {
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		if (pageNum == null || "".equals(pageNum)) {
			pageNum = "1";// 默认是第1页
		}
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/postings/pages");
		webTarget=webTarget.queryParam("pageNum", pageNum);
		webTarget=webTarget.queryParam("pageSize", pageSize);
		webTarget=webTarget.queryParam("essence", essence);//加精
		webTarget=webTarget.queryParam("authentication", authentication);//官方认证
		webTarget=webTarget.queryParam("postingWhether", postingWhether);
		webTarget=webTarget.queryParam("postingOrderBy", postingOrderBy);
		webTarget=webTarget.queryParam("section", section);
		webTarget=webTarget.queryParam("userId", userId);
		if(displayStatus==null||displayStatus.length()==0){
			displayStatus="0";
		}
		webTarget=webTarget.queryParam("displayStatus", displayStatus);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		String result = response.readEntity(String.class);
		response.close();
		
		return result;
	}

	
	@GET
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("lists")
	public String lists(@QueryParam("displayStatus") String displayStatus, @Context HttpServletRequest req,
			@Context HttpServletResponse res) {
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/postings/lists")
				.queryParam("displayStatus", displayStatus);

		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		String result = response.readEntity(String.class);
	
		response.close();
		return result;

	}


	
}
