package user.normal.reports;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import user.normal.bean.MessageBean;
import user.normal.bean.Report;
import user.normal.dao.Constant;
import user.normal.jwt.KeyUtil;
import user.normal.jwt.LoginVerify;
import user.normal.jwt.TokenUtil;

@Path("reports")
@Singleton
public class Reports {

	@SuppressWarnings("unchecked")
	@POST
	@LoginVerify
	@RolesAllowed({"U"})
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("new_report")
	public MessageBean new_report(@BeanParam Report report,@CookieParam("jwtf") String tokenstr, @Context HttpServletRequest req,
			@Context HttpServletResponse res) {
		MessageBean messageBean = new MessageBean();
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		/*// 获取jwt
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
		String userid = TokenUtil.getId(tokenstr, key);
		WebTarget wt = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/reports/lists")
				.queryParam("postingId", report.getPostingId()).queryParam("commentId", report.getCommentId())
				.queryParam("reportUserId", userid);

		final Invocation.Builder ib = wt.request();
		Response resp = ib.get();
		List<Report> reportlist = resp.readEntity(List.class);
		resp.close();
		if (reportlist.size() > 0) {
			messageBean.setResultCode("-2");
			messageBean.setResultMsg("请勿重复举报");
			return messageBean;
		}

		report.setCreateTime(new Date());
		report.setReportUserId(Integer.valueOf(userid));

		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/reports/new_report");

		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.buildPost(Entity.entity(report, MediaType.APPLICATION_JSON)).invoke();
		MessageBean aff = response.readEntity(MessageBean.class);

		response.close();
		return aff;

	}
}
