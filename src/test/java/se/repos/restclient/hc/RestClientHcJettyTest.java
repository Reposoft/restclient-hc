/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.repos.restclient.hc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.embedded.HelloHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestGetClient;
import se.repos.restclient.RestHeadClient;
import se.repos.restclient.RestResponse;
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
	public void testHeadRedirect() throws Exception {
		Server server = new Server(49999); // TOOD random port retry like UnitHttpServer
		server.setHandler(new HelloHandler("repos", "restclient") {
			@Override
			public void handle(String target, Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				if ("/start".equals(request.getPathInfo().toString())) {
					response.sendRedirect("/start/");
					return;
				}
				super.handle(target, baseRequest, request, response);
			}
		});
		server.setStopAtShutdown(true);
		server.start();
		
		RestClient client = new RestClientHc("http://localhost:49999", null);
		ResponseHeaders head = client.head("/start");
		// solsson: Preventing redirects for HEAD requests, as those might be used to probe for redirects.
		assertEquals("should return the status code, not follow the redirect", 302, head.getStatus());
		
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
		
		RestAuthentication auth = new RestAuthenticationSimple("user", "pwd") {
			@Override
			public String getUsername(String root, String resource, String realm) {
				assertEquals("Should provide root with http", "http://localhost:49999", root);
				assertEquals("Resource can be null", null, resource);
				assertEquals("Should provide the realm", "test", realm);
				return super.getUsername(root, resource, realm);
			}
			@Override
			public String getPassword(String root, String resource,
					String realm, String username) {
				assertEquals("Should provide root with http", "http://localhost:49999", root);
				assertEquals("Should provide the realm", "test", realm);
				assertEquals("Should provide the username", "user", username);
				return super.getPassword(root, resource, realm, username);
			}
		};
		
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
	
	// Generic client API compliance test, same as in RestClientJavaJettyTest
	@Test
	public void testGet401() throws Exception {
		int port = 49999; // TODO random test port
        Server server = new Server(port);
 
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        
        context.addServlet(new ServletHolder(new HttpServlet() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				resp.sendError(401, "Can this be custom?");
			}
		}), "/*");
        
        server.start();
        
        try {
        	RestResponse resp = new RestResponseBean();
        	new RestClientHc("http://localhost:" + port, null).get("/", resp);
        	fail("Expected status error");
        } catch (HttpStatusError e) {
        	assertEquals(401, e.getHttpStatus());
        	ResponseHeaders headers = e.getHeaders();
        	assertNotNull("Should contain HTTP headers sent", headers);
        	assertTrue(headers.size() > 0);
        	assertTrue(e.getResponse().contains("Can this be custom?")); // assuming jetty writes an error page body
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error not handled", e);
		} finally {
        	server.stop();
        }
	}
	
	@Test
	public void testAuthMultiThreaded() throws Exception {
		
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
					response.addHeader("WWW-Authenticate", "Basic realm=\"testMulti\"");
					response.sendError(401);
					return;
				}
				authHeaders.add(authHeader);				
				super.handle(target, baseRequest, request, response);
			}
		});
		
		RestAuthentication auth = mock(RestAuthentication.class);
		when(auth.getUsername(anyString(), anyString(), eq("testMulti")))
			.thenReturn("user1").thenReturn("user2").thenReturn(null).thenReturn("user3");
		when(auth.getPassword(anyString(), anyString(), eq("testMulti"), eq("user1"))).thenReturn("pass1");
		when(auth.getPassword(anyString(), anyString(), eq("testMulti"), eq("user2"))).thenReturn("pass2");
		when(auth.getPassword(anyString(), anyString(), eq("testMulti"), eq("user3"))).thenReturn("pass3");
		when(auth.getPassword(anyString(), anyString(), anyString(), (String) isNull()))
			.thenThrow(new AssertionError("null user means don't authenticate"));

		server.start();
		try {
			RestGetClient get = new RestClientHc("http://localhost:49999", auth);
			get.get("/", new RestResponseBean());
			get.get("/", new RestResponseBean());
			try {
				get.get("/", new RestResponseBean());
				fail("Second request should have been without credentials");
			} catch (HttpStatusError e) {
				assertEquals(401, e.getHeaders().getStatus());
			} catch (Exception e) {
				fail("Should still allow requests that don't attempt preemptive authentication, got " + e);
			}
			get.get("/", new RestResponseBean());
		} finally {
			server.stop();
		}
		
		assertTrue(authHeaders.size() > 2);
		assertEquals("Should get credentials from authentication service",
				"Basic " + Base64.encodeBase64String("user1:pass1".getBytes()).trim(), authHeaders.get(0));
		assertEquals("Should get fresh credentials from authentication service",
				"Basic " + Base64.encodeBase64String("user2:pass2".getBytes()).trim(), authHeaders.get(1));
		assertTrue("Should not attempt authentication when getUsername returns null",
				authHeaders.size() == 3);
		assertEquals("Should get credentials after no auth",
				"Basic " + Base64.encodeBase64String("user3:pass3".getBytes()).trim(), authHeaders.get(2));
	}

}
