package zhenhe.li.ws.jaxrs.example.client;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import zhenhe.li.ws.jaxrs.example.server.User;

public class HelloClient {
	private final static String serverUri = "http://localhost:8083/ws/rest-api";
	public static void main(String[] args) {
		
		System.out.println("****根据id查询用户****");  
        Client client = ClientBuilder.newClient();// 注册json 支持  
        WebTarget target = client.target(serverUri + "/hello/sayhi/001");  
        Response response = target.request().get();  
        User user = response.readEntity(User.class);  
        System.out.println(user.getId() + user.getName());  
        response.close();
	}
}
