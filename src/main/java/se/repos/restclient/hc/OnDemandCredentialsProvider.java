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

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.restclient.RestAuthentication;

/**
 * Made for repos-authproxy setups, where credentials may differ in
 * every thread/request and some services don't require authentication.
 */
public class OnDemandCredentialsProvider implements CredentialsProvider {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private RestAuthentication auth;

	/**
	 * @param auth Delivers the credentials for every attempt, 
	 *  note that the "root" argument will always start with "http:"
	 *  because HC does not provide the protocol
	 */
	public OnDemandCredentialsProvider(RestAuthentication auth) {
		this.auth = auth;
	}

	@Override
	public Credentials getCredentials(AuthScope scope) {
		String host = "http://" + scope.getHost(); // AuthScope doesn't tell us the protocol
		if (scope.getPort() > 0) {
			host = host + ":" + scope.getPort();
		}
		String user = auth.getUsername(host, null, scope.getRealm());
		if (user == null) {
			return null;
		}
		logger.debug("Authenticating REST call as user {} ({} realm {})", new Object[]{user, host, scope.getRealm()});
		return new UsernamePasswordCredentials(
				user, 
				auth.getPassword(host, null, scope.getRealm(), user));
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("This credentials impl can not be cleared");
	}
	
	@Override
	public void setCredentials(AuthScope scope, Credentials credentials) {
		throw new UnsupportedOperationException("This credentials impl is immutable, backed by " + auth.getClass().getSimpleName());
	}

}
