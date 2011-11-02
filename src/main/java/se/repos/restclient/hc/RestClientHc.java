package se.repos.restclient.hc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;
import se.repos.restclient.base.RestClientMultiHostBase;

/**
 * Still experimental RestClient implementation using Apache HTTP Components.
 * Configures a new client for every request.
 */
public class RestClientHc extends RestClientMultiHostBase implements RestClient {

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
	public void get(URL url, RestResponse response) throws IOException,
			HttpStatusError {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        configureAuthPreemptive(httpclient);
        URI uri = toUri(url);
        HttpGet httpget = new HttpGet(uri);
        HcRestResponseWrapper responseHandler = new HcRestResponseWrapper(response);
        int status = httpclient.execute(httpget, responseHandler);
        httpclient.getConnectionManager().shutdown();
        if (status != 200) {
        	throw new HttpStatusError(uri.toURL(), 
        			responseHandler.getErrorHeaders(), responseHandler.getErrorBody());
        }
	}

	@Override
	public ResponseHeaders head(URL url) throws IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpClientParams.setRedirecting(httpclient.getParams(), false);
		configureAuthPreemptive(httpclient);
		HttpHead httphead = new HttpHead(toUri(url));
		
        ResponseHeaders head = httpclient.execute(httphead, new ResponseHandler<ResponseHeaders>() {
    		@Override
    		public ResponseHeaders handleResponse(HttpResponse response)
    				throws ClientProtocolException, IOException {
    			return new HcResponseHeaders(response);
    		}
    	});
        httpclient.getConnectionManager().shutdown();
        return head;
	}

	/**
	 * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html#d4e1023
	 */
	private void configureAuthPreemptive(DefaultHttpClient httpclient) {
		if (this.auth == null) {
			return;
		}
		httpclient.getCredentialsProvider().setCredentials(
			AuthScope.ANY, // TODO restrict to host from constructor 
			new UsernamePasswordCredentials(
					auth.getUsername(null, null, null), 
					auth.getPassword(null, null, null, null)));
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
