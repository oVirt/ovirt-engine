/**
 *
 */
package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.vdshooks.VdsHooksParser;

/**
 * Query for returning VDS hooks by vds ID The returned object is a map of
 * folder/event names to an inner map of script names to an inner map of
 * property names and values
 */
public class GetVdsHooksByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsDao vdsDao;

    public GetVdsHooksByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {

        VDS vds = vdsDao.get(getParameters().getId());
        Map<String, Object> result = new HashMap<>();
        if (vds != null) {
            result = VdsHooksParser.parseHooks(vds.getHooksStr());
        }
        getQueryReturnValue().setReturnValue(result);
    }
}
