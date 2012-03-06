package se.repos.restclient.hc;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;

import se.repos.restclient.RestAuthentication;

public class OnDemandCredentialsProvider implements CredentialsProvider {

	private RestAuthentication auth;

	public OnDemandCredentialsProvider(RestAuthentication auth) {
		this.auth = auth;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Credentials getCredentials(AuthScope scope) {
		// TODO verify realm etc?
		String user = auth.getUsername(null, null, null);
		if (user == null) {
			return null;
		}
		return new UsernamePasswordCredentials(
				user, 
				auth.getPassword(null, null, null, null));
	}

	@Override
	public void setCredentials(AuthScope scope, Credentials credentials) {
		throw new UnsupportedOperationException();
	}

}
