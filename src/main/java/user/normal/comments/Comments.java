package user.normal.comments;

import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import net.sf.json.JSONObject;
import user.normal.bean.Comment;
import user.normal.bean.MessageBean;
import user.normal.bean.Posting;
import user.normal.dao.Constant;
import user.normal.jwt.KeyUtil;
import user.normal.jwt.LoginVerify;
import user.normal.jwt.TokenUtil;

@Path("comments")
@Singleton
public class Comments {

	@POST
	@LoginVerify
	@RolesAllowed({"U"})
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("new_comment")
	public MessageBean postIt(@BeanParam Comment comment,@CookieParam("jwtf") String tokenstr,@Context HttpServletRequest req,
			@Context HttpServletResponse res){
		comment.setCreateTime(new Date());
		String key = KeyUtil.getKey();// 获取秘钥
		String id = TokenUtil.getId(tokenstr, key);// 获取用户id
		comment.setCommentUserId(Integer.valueOf(id));
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/comments/new_comment");

		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.buildPost(Entity.entity(comment, MediaType.APPLICATION_JSON)).invoke();
		MessageBean aff = response.readEntity(MessageBean.class);		
		response.close();
		
		WebTarget wt = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/postings/");
		wt = wt.path(comment.getPostingId().toString());
		final Invocation.Builder ib = wt.request();		
		Response resp1 = ib.get();
		String posting = resp1.readEntity(String.class);		
		resp1.close();	
		
		JSONObject jsStr = JSONObject.fromObject(posting);
		Posting postingBean=new Posting();
		postingBean.setCommentTimes(jsStr.getInt("commentTimes")+1);	
		Response resp2 = ib.buildPut(Entity.entity(postingBean, MediaType.APPLICATION_JSON)).invoke();
		MessageBean aff2 = resp2.readEntity(MessageBean.class);
		resp2.close();			
		return aff;
	}
	
	@GET
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{comment_id}")
	public String get_by_id(@PathParam("comment_id") String comment_id, @Context HttpServletRequest req,
			@Context HttpServletResponse res){
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/comments/");
		webTarget = webTarget.path(comment_id);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		String result = response.readEntity(String.class);
		
		response.close();
		return result;
	}
	
	@GET
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("pages")
	public String pages(@QueryParam("postingId") String postingId,@QueryParam("pageNum") String pageNum, @QueryParam("pageSize") String pageSize,
			@QueryParam("displayStatus") String displayStatus, @Context HttpServletRequest req, @Context HttpServletResponse res) {
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		if (pageNum == null || "".equals(pageNum)) {
			pageNum = "1";// 默认是第1页
		}
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/comments/pages");
		webTarget=webTarget.queryParam("postingId", postingId);
		webTarget=webTarget.queryParam("pageNum", pageNum);
		webTarget=webTarget.queryParam("pageSize", pageSize);
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
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/comments/lists")
				.queryParam("displayStatus", displayStatus);

		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		String result = response.readEntity(String.class);
		
		response.close();
		return result;
	}
	
}
