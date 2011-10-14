package se.repos.restclient.hc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import se.repos.restclient.base.ResponseHeadersReadOnly;

public class HcResponseHeaders extends ResponseHeadersReadOnly {

	private HttpResponse response;

	protected HcResponseHeaders(HttpResponse response) {
		this.response = response;
	}

	@Override
	public String getContentType() {
		return response.getFirstHeader("Content-Type").getValue();
	}

	@Override
	public int getStatus() {
		return response.getStatusLine().getStatusCode();
	}

	@Override
	public int size() {
		return response.getAllHeaders().length;
	}

	@Override
	public List<String> get(Object key) {
		List<String> values = new LinkedList<String>();
		for (Header h : response.getHeaders(key.toString())) {
			values.add(h.getValue());
		}
		return values;
	}	
	
	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException("not implemented");		
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("not implemented");		
	}

	@Override
	public Set<String> keySet() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public Collection<List<String>> values() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public Set<java.util.Map.Entry<String, List<String>>> entrySet() {
		throw new UnsupportedOperationException("not implemented");		
	}

}
