package se.repos.restclient.hc;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.embedded.HelloHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestGetClient;
import se.repos.restclient.RestHeadClient;
import se.repos.restclient.RestResponseBean;
import se.repos.restclient.auth.RestAuthenticationSimple;

public class RestClientHcJettyTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGet() throws Exception {
		Server server = new Server(49999); // TOOD random port retry like UnitHttpServer
		server.setHandler(new HelloHandler("repos", "restclient"));
		server.setStopAtShutdown(true);
		server.start();
		
		RestGetClient client = new RestClientHc("http://localhost:49999", null);
		RestResponseBean r1 = new RestResponseBean();
		client.get("/", r1);
		assertTrue("Got: " + r1.getBody(), r1.getBody().contains("<h1>repos</h1>"));
		assertEquals("text/html;charset=UTF-8", r1.getHeaders().getContentType());
		
		server.stop();
	}

	@Test
	public void testHead() throws Exception {
		Server server = new Server(49999); // TOOD random port retry like UnitHttpServer
		server.setHandler(new HelloHandler("repos", "restclient"));
		server.setStopAtShutdown(true);
		server.start();
		
		RestHeadClient client = new RestClientHc("http://localhost:49999", null);
		ResponseHeaders head = client.head("/");
		assertNotNull(head);
		assertEquals(200, head.getStatus());
		assertEquals("26", head.get("Content-Length").get(0));
		
		server.stop();		
	}
	
	@Test
	public void testAuth() throws Exception {
		Server server = new Server(49999); // TOOD random port retry like UnitHttpServer
		final List<String> authHeaders = new LinkedList<String>();
		server.setHandler(new HelloHandler("repos", "restclient") {
			@Override
			public void handle(String target, Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				// TODO currently testing preemtive auth,
				//  but client should not send auth unless prompted
				String authHeader = request.getHeader("Authorization");
				if (authHeader == null) {
					response.addHeader("WWW-Authenticate", "Basic realm=\"test\"");
					response.sendError(401);
					return;
				}
				authHeaders.add(authHeader);
				super.handle(target, baseRequest, request, response);
			}
		});
		server.setStopAtShutdown(true);
		server.start();
		
		RestAuthentication auth = new RestAuthenticationSimple("user", "pwd");
		
		RestHeadClient client = new RestClientHc("http://localhost:49999", auth);
		ResponseHeaders head = client.head("/");
		assertNotNull(head);
		assertEquals(200, head.getStatus());

		assertEquals("Expecting an authentication header with credentials", 1, authHeaders.size());
		assertEquals("Basic " + Base64.encodeBase64String("user:pwd".getBytes()).trim(), authHeaders.get(0));
		
		authHeaders.clear();
		RestGetClient get = new RestClientHc("http://localhost:49999", auth);
		RestResponseBean r2 = new RestResponseBean();
		get.get("/", r2);
		System.out.println(r2.getBody());
		assertEquals("Expecting an authentication header with credentials", 1, authHeaders.size());
		assertEquals("Basic " + Base64.encodeBase64String("user:pwd".getBytes()).trim(), authHeaders.get(0));					
		
		server.stop();
	}

}
