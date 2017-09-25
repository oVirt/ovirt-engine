package org.ovirt.engine.api.restapi.resource;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SystemOption;
import org.ovirt.engine.api.resource.SystemOptionResource;
import org.ovirt.engine.api.restapi.types.SystemOptionsMapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetSystemOptionParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;


public class BackendSystemOptionResource extends BackendResource implements SystemOptionResource {

    private static final String VERSION = "version";
    private String id;

    public BackendSystemOptionResource(String id) {
        this.id = id;
    }

    @Override
    public SystemOption get() {
        ConfigValues config;
        try {
            config = ConfigValues.valueOf(id);
        } catch (IllegalArgumentException ex) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        GetSystemOptionParameters parameters = new GetSystemOptionParameters(config);

        String version = ParametersHelper.getParameter(httpHeaders, uriInfo, VERSION);
        if (version != null && !version.isEmpty()) {
            parameters.setOptionVersion(version);
        }

        QueryReturnValue result = runQuery(QueryType.GetSystemOption, parameters);

        if (result.getReturnValue() == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return LinkHelper.addLinks(SystemOptionsMapper.map(result.getReturnValue(), id));
    }
}
