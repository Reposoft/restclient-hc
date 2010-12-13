package se.repos.restclient.hc;

import static org.junit.Assert.*;

import java.io.IOException;

import org.eclipse.jetty.embedded.HelloHandler;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestGetClient;
import se.repos.restclient.RestHeadClient;
import se.repos.restclient.RestResponseBean;

public class RestClientHcJettyTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGet() throws Exception {
		Server server = new Server(49999); // TOOD random retry like UnitHttpServer
		server.setHandler(new HelloHandler("repos", "restclient"));
		server.setStopAtShutdown(true);
		server.start();
		
		RestGetClient client = new RestClientHc();
		RestResponseBean r1 = new RestResponseBean();
		client.get("http://localhost:49999", r1);
		assertTrue("Got: " + r1.getBody(), r1.getBody().contains("<h1>repos</h1>"));
		assertEquals("text/html;charset=UTF-8", r1.getHeaders().getContentType());
		
		server.stop();
	}

	@Test
	public void testHead() throws Exception {
		Server server = new Server(49999); // TOOD random retry like UnitHttpServer
		server.setHandler(new HelloHandler("repos", "restclient"));
		server.setStopAtShutdown(true);
		server.start();
		
		RestHeadClient client = new RestClientHc();
		RestResponseBean r1 = new RestResponseBean();
		ResponseHeaders head = client.head("http://localhost:49999");
		assertNotNull(head);
		assertEquals(200, head.getStatus());
		assertEquals("26", head.get("Content-Length").get(0));
		
		server.stop();		
	}

}
