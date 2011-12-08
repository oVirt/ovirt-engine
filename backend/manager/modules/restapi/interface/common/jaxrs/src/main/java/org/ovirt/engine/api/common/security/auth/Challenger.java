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

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import org.ovirt.engine.api.common.invocation.Current;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class Challenger implements PreProcessInterceptor {

    private String realm;
    private Scheme scheme;
    private Validator validator;
    private Current current;

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    /**
     * Issue 401 challenge on missing or invalid credentials.
     * May be called further up the call-stack if supplied credentials are
     * found to be invalid.
     *
     * @return ServerResponse containing challenge
     */
    public Response getChallenge() {
        return Response.status(Status.UNAUTHORIZED)
                       .header(HttpHeaders.WWW_AUTHENTICATE, scheme.getChallenge(realm))
                       .build();
    }

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
        ServerResponse response = null;
        HttpHeaders headers = request.getHttpHeaders();
        List<String> auth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || auth.size() == 0) {
            response = challenge();
        } else {
            Principal principal = scheme.decode(headers);
            if (validator == null || validator.validate(principal)) {
                current.set(principal);
                current.set(this);
            } else {
                response = challenge();
            }
        }
        return response;
    }

    /**
     * By default principal validation is lazy, with the assumption that this
     * will be initiated by the resource later on the dispatch path. This method
     * allows subclasses to pursue an alternate strategy based on eager validation.
     *
     * @param principal  the decoded principal
     * @return           true iff dispatch should continue
     */
    protected boolean validate(Principal principal) {
        return true;
    }

    /**
     * Helper method to copy the challenge response
     */
    private ServerResponse challenge() {
        return ServerResponse.copyIfNotServerResponse(getChallenge());
    }
}
