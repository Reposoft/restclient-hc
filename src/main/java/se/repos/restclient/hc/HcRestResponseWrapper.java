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
