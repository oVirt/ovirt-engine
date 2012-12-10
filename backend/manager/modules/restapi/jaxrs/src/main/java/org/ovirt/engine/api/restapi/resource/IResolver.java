package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.restapi.resource.BaseBackendResource.BackendFailureException;

public interface IResolver<T, Q> {
    public Q resolve(T id) throws BackendFailureException;
}
