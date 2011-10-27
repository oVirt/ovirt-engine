/*
* Copyright © 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.security.auth;

/**
 * Encapsulates user identity.
 */
public class Principal {

    public static final Principal NONE = new Principal(null, null, null);

    private String user;
    private String secret;
    private String domain;

    public Principal(String user, String secret) {
        this(user, secret, null);
    }

    public Principal(String user, String secret, String domain) {
        this.user = user;
        this.secret = secret;
        this.domain = domain;
    }

    public String getUser() {
        return user;
    }

    public String getSecret() {
        return secret;
    }

    public String getDomain() {
        return domain;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Principal))
            return false;
        Principal auth = (Principal)o;
        if ((domain == null) ? auth.domain != null : !domain.equals(auth.domain))
            return false;
        if ((user == null) ? auth.user != null : !user.equals(auth.user))
            return false;
        if ((secret == null) ? auth.secret != null : !secret.equals(auth.secret))
            return false;
        return true;
    }

    @Override public int hashCode() {
        int result = 17;
        if (domain != null)
            result = 31 * result + domain.hashCode();
        if (user != null)
            result = 31 * result + user.hashCode();
        if (secret != null)
            result = 31 * result + secret.hashCode();
        return result;
    }
}
