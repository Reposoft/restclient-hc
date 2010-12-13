package se.repos.restclient.hc;

import java.io.IOException;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.params.HttpParams;

import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestResponse;
import se.repos.restclient.base.RestResponseWrapper;

public class HcRestResponseWrapper extends RestResponseWrapper implements
		ResponseHandler<Integer> {

	protected HcRestResponseWrapper(RestResponse restclientResponse) {
		super(restclientResponse);
	}

	@Override
	public Integer handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		ResponseHeaders headers = new HcResponseHeaders(response);
		response.getEntity().writeTo(this.getResponseStream(headers));
		return response.getStatusLine().getStatusCode();
	}

}
