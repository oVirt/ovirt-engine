package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.queries.GetVmIconsParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmIconDao;

/**
 * Given a list of icon ids it returns icons data in dataurl form
 */
public class GetVmIconsQuery extends QueriesCommandBase<GetVmIconsParameters> {

    @Inject
    private VmIconDao vmIconDao;

    public GetVmIconsQuery(GetVmIconsParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    /**
     * query returned type: {@code Map<Guid, String>} requested icon id -> icon data
     */
    @Override
    protected void executeQueryCommand() {
        Map<Guid, String> result = new HashMap<>();
        for (Guid iconId : getParameters().getIconIds()) {
            final VmIcon vmIcon = vmIconDao.get(iconId);
            result.put(iconId, vmIcon.getDataUrl());
        }

        setReturnValue(result);
    }
}
