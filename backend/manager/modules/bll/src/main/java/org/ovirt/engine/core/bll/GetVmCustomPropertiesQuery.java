package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils;

public class GetVmCustomPropertiesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    private static final String Version3_0 = "3.0";

    public GetVmCustomPropertiesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(VmPropertiesUtils.getInstance().getAllVmProperties());
    }

    /**
     * @return The predefined VM properties.
     */
    protected String getPredefinedVMProperties() {
        return Config.<String> GetValue(ConfigValues.PredefinedVMProperties, Version3_0);
    }

    /**
     * @return The user-defined VM properties.
     */
    protected String getUserDefinedVMProperties() {
        return Config.<String> GetValue(ConfigValues.UserDefinedVMProperties, Version3_0);
    }

    /**
     * @return The other method version
     */
    protected Version getVersion() {
        return new Version(Version3_0);
    }

}
