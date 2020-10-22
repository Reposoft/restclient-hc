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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;
import se.repos.restclient.base.RestClientUrlBase;

/**
 * Still experimental RestClient implementation using Apache HTTP Components.
 * Configures a new client for every request.
 */
public class RestClientHc extends RestClientUrlBase implements RestClient {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private RestAuthentication auth;

	@Inject
	public RestClientHc(
			@Named("config:se.repos.restclient.serverRootUrl") String serverRootUrl, 
			RestAuthentication auth) {
		super(serverRootUrl);
		this.auth = auth;
		logger.info("RestClient configured with root url {} and authentication {}", serverRootUrl, auth);
	}
	
	@Override
	public void get(URL url, RestResponse response) throws IOException {
		try (CloseableHttpClient httpclient = HttpClientBuilder.create()
				.setDefaultCredentialsProvider(new OnDemandCredentialsProvider(auth))
				.build()) {
			URI uri = toUri(url);
			HttpGet httpget = new HttpGet(uri);
			HcRestResponseWrapper responseHandler = new HcRestResponseWrapper(response);
			int status = httpclient.execute(httpget, responseHandler);
			if (status != 200) {
				throw new HttpStatusError(uri.toString(),
						responseHandler.getErrorHeaders(), responseHandler.getErrorBody());
			}
		}
	}

	@Override
	public ResponseHeaders head(URL url) throws IOException {
		try (CloseableHttpClient httpclient = HttpClientBuilder.create()
				.setDefaultCredentialsProvider(new OnDemandCredentialsProvider(auth))
				.setRedirectStrategy(new RedirectStrategy() {
					@Override
					public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
						return false;
					}

					@Override
					public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
						return null;
					}
				})
				.build()) {
			HttpHead httphead = new HttpHead(toUri(url));
			return httpclient.execute(httphead, (ResponseHandler<ResponseHeaders>) HcResponseHeaders::new);
		}
	}
	
	private URI toUri(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			// this is probably not possible if the serverRootUrl to constructor
			//  is valid, so the constructor might need validation
			throw new RuntimeException("Rest client got invalid URL: " + url, e);
		}
	}
	
}
