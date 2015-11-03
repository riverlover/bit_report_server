package zhenhe.li.ws.jaxrs.example.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("hello")
public class HelloWorld {

	@GET
	@Path("sayhi/{guest}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public User sayHi(@PathParam("guest") String id) {
		User u = new User();
		u.setId("0001");
		u.setName("张三");
		return u;
	}
}