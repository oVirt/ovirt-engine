/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class V3JsonProvider extends JacksonJsonProvider {
    // The version of the API supported by this provider:
    public static final String SUPPORTED_VERSION = "3";

    public V3JsonProvider() {
        super();
        setMapper(V3CustomObjectMapper.get());
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (!SUPPORTED_VERSION.equals(CurrentManager.get().getVersion())) {
            return false;
        }
        return super.isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (!SUPPORTED_VERSION.equals(CurrentManager.get().getVersion())) {
            return false;
        }
        return super.isWriteable(type, genericType, annotations, mediaType);
    }
}
