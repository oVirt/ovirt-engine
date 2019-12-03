/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.json;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JSONProvider extends JacksonJsonProvider {
    // The version of the API supported by this provider:
    public static final String SUPPORTED_VERSION = "4";

    public JSONProvider() {
        super();
        setMapper(CustomObjectMapper.get());
    }

}
