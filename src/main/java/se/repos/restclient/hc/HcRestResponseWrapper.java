package se.repos.restclient.hc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestResponse;

public class HcRestResponseWrapper implements ResponseHandler<Integer> {

	private RestResponse response;
	private HcResponseHeaders headers;
	private String errorBody;

	protected HcRestResponseWrapper(RestResponse restclientResponse) {
		this.response = restclientResponse;
	}

	@Override
	public Integer handleResponse(HttpResponse httpResponse)
			throws ClientProtocolException, IOException {
		int status = httpResponse.getStatusLine().getStatusCode();
		this.headers = new HcResponseHeaders(httpResponse);
		HttpEntity entity = httpResponse.getEntity();
		if (status == 200) {
			entity.writeTo(response.getResponseStream(headers));
		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			entity.writeTo(out);
			this.errorBody = out.toString();
		}
		return status;
	}
	
	/**
	 * @return for use from client to create status error instance
	 */
	ResponseHeaders getErrorHeaders() {
		return headers;
	}
	
	/**
	 * @return for use from client to create status error instance
	 */
	String getErrorBody() {
		return errorBody;
	}
	
}
