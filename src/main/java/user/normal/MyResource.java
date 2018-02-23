package user.normal;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


@ApplicationPath("/")
@Singleton
public class MyResource extends ResourceConfig{

   
	public MyResource() {
		
		packages("user.normal");
		register(MultiPartFeature.class);
		register(JacksonFeature.class);
		//register(JsonContextProvider.class);
		register(JacksonJsonProvider.class);
		register(RolesAllowedDynamicFeature.class);

	}
}
