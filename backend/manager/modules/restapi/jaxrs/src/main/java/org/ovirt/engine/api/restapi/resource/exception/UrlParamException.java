package org.ovirt.engine.api.restapi.resource.exception;

import javax.ws.rs.core.Response.Status;

import org.ovirt.engine.api.restapi.resource.BaseBackendResource;
import org.ovirt.engine.api.restapi.resource.BaseBackendResource.WebFaultException;

public class UrlParamException extends WebFaultException {

    private static final long serialVersionUID = -3881302325152395060L;

    public UrlParamException(BaseBackendResource resource, Exception cause, String detail) {
        resource.super(cause, detail, Status.BAD_REQUEST);
    }
}
