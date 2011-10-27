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

import javax.ws.rs.core.HttpHeaders;


public interface Scheme {
    /**
     * @return the authorization scheme (BASIC or DIGEST)
     */
    String getName();

    /**
     * Generate a challenge.
     *
     * @param realm the target realm
     * @return      the challenge header to return in response to an
     *              unauthorized request
     */
    String getChallenge(String realm);

    /**
     * Decode the auth header and extract a principal (not necessarily
     * fully validated as yet).
     *
     * @return the decoded principal
     */
    Principal decode(HttpHeaders headers);
}
