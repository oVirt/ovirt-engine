package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.ovf.OvfVmIconDefaultsProvider;

/**
 * It provides mapping of operating systems to their default icons.
 */
public class GetVmIconDefaultsQuery extends QueriesCommandBase<VdcQueryParametersBase> {

    @Inject
    private OvfVmIconDefaultsProvider iconDefaultsProvider;

    public GetVmIconDefaultsQuery(VdcQueryParametersBase parameters) {
        super(parameters);
    }

    /**
     * query return type {@code Map<Integer, Guid>} osId -> iconId
     */
    @Override
    protected void executeQueryCommand() {
        setReturnValue(iconDefaultsProvider.getVmIconDefaults());
    }
}
