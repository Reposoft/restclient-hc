package se.repos.restclient.hc;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;
import se.repos.restclient.RestResponseBean;

public class RestClientHc implements RestClient {

	@Override
	public void get(String uri, RestResponse response) throws IOException,
			HttpStatusError {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(uri); 
        ResponseHandler<Integer> responseHandler = new HcRestResponseWrapper(response);
        int status = httpclient.execute(httpget, responseHandler);
        httpclient.getConnectionManager().shutdown(); 
	}

	@Override
	public ResponseHeaders head(String uri) throws IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpHead httphead = new HttpHead(uri);
		
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

}
