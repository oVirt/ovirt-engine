/*
* Copyright (c) 2010 Red Hat, Inc.
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

import java.text.MessageFormat;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.codec.binary.Base64;


public class BasicAuthorizationScheme implements Scheme {

    private static final String SCHEME = "Basic";
    private static final String CHALLENGE_TEMPLATE = SCHEME + " realm=\"{0}\"";

    private static String USER_PASS_SEPARATOR = ":";
    private static char UPN_USER_DOMAIN_SEPARATOR = '@';
    private static String LEGACY_USER_DOMAIN_SEPARATOR = "\\";

    @Override
    public String getName() {
        return SCHEME;
    }

    @Override
    public String getChallenge(String realm) {
        return MessageFormat.format(CHALLENGE_TEMPLATE, realm);
    }

    @Override
    public Principal decode(HttpHeaders headers) {
        Principal principal = null;
        if (headers != null) {
            List<String> auth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && auth.size() > 0) {
                String credentials = auth.get(0);
                if (credentials.trim().startsWith(SCHEME)) {
                    principal = decode(credentials);
                }
            }
        }
        return principal;
    }

    private Principal decode(String credentials) {
        Principal principal = null;
        try {
            credentials = credentials.trim().substring(SCHEME.length()).trim();
            String userPass = new String(Base64.decodeBase64(credentials));
            String[] creds = userPass.split(USER_PASS_SEPARATOR, 2);
            if (creds != null && creds.length == 2) {
                principal = parse(creds[0], creds[1], getSeparator(creds[0]));
            }
        } catch (Exception e) {
            // let principal remain null
        }
        return principal;
    }

    private int getSeparator(String qualified) {
        return qualified.indexOf(UPN_USER_DOMAIN_SEPARATOR) != -1
               ? qualified.indexOf(UPN_USER_DOMAIN_SEPARATOR)
               : qualified.indexOf(LEGACY_USER_DOMAIN_SEPARATOR);
    }

    private Principal parse(String qualified, String password, int index) {
        Principal principal = null;
        if (index != -1) {
            String user = null, domain = null;
            if (qualified.charAt(index) == UPN_USER_DOMAIN_SEPARATOR) {
                // UPN format: user@domain
                user = qualified.substring(0, index);
                domain = qualified.substring(index + 1);
            } else {
                // legacy format: domain\\user
                domain = qualified.substring(0, index);
                user = qualified.substring(index + 1);
            }
            principal = new Principal(user, password, domain);
        } else {
            principal = new Principal(qualified, password);
        }
        return principal;
    }
}
