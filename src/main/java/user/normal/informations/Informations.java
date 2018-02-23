package user.normal.informations;

import java.io.IOException;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import user.normal.bean.Information;
import user.normal.bean.MessageBean;
import user.normal.dao.Constant;

@Path("informations")
@Singleton
public class Informations {

	@GET	
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("info_list")
	public String getIt(@QueryParam("displayStatus") String displayStatus, @Context HttpServletRequest req,
			@Context HttpServletResponse res) throws ServletException, IOException {
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/informations/lists")
				.queryParam("displayStatus", displayStatus);

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
	public String pages(@QueryParam("pageNum") String pageNum, @QueryParam("pageSize") String pageSize,
			@Context HttpServletRequest req, @Context HttpServletResponse res) throws ServletException, IOException {
			
		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);		
		if(pageNum==null||"".equals(pageNum)){
			pageNum="1";//默认是第1页
		}
		
			WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/informations/pages");
			webTarget=webTarget.queryParam("pageNum", pageNum);
			webTarget=webTarget.queryParam("pageSize", pageSize);
			webTarget=webTarget.queryParam("displayStatus", ">-1");
			final Invocation.Builder invocationBuilder = webTarget.request();
			Response response = invocationBuilder.get();
			String result = response.readEntity(String.class);
			response.close();
			return result;	

	}

	@GET	
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{info_id}")
	public Information infoid(@PathParam("info_id") String info_id, @Context HttpServletRequest req,
			@Context HttpServletResponse res) throws ServletException, IOException {

		ClientConfig clientConfig = new ClientConfig();
		// 创建Client
		Client client = ClientBuilder.newClient(clientConfig);

		WebTarget webTarget = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/informations/");
		webTarget = webTarget.path(info_id);
		final Invocation.Builder invocationBuilder = webTarget.request();
		Response response = invocationBuilder.get();
		Information information = response.readEntity(Information.class);
		response.close();
		WebTarget wt = client.target("http://"+Constant.getDBHost()+"/cmdcforumdb/v1/informations/");
		webTarget = wt.path(info_id);
		final Invocation.Builder ib = webTarget.request();
		information.setViewTimes(information.getViewTimes()+1);
		Response resp = ib.buildPut(Entity.entity(information, MediaType.APPLICATION_JSON)).invoke();
		MessageBean aff = resp.readEntity(MessageBean.class);
		resp.close();
		
		return information;

	}
}
