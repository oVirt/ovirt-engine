package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetDeviceCustomPropertiesParameters;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;

/**
 * Query returning all map of all existing properties with their string regex to validate values for specified cluster
 * version and device type
 */
public class GetDeviceCustomPropertiesQuery<P extends GetDeviceCustomPropertiesParameters>
        extends QueriesCommandBase<P> {

    /**
     * {@inheritDoc}
     */
    public GetDeviceCustomPropertiesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DevicePropertiesUtils.getInstance()
                .getDeviceProperties(getParameters().getVersion(), getParameters().getDeviceType()));
    }
}
