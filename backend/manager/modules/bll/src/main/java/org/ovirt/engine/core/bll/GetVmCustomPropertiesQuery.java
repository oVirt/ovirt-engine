package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetVmCustomPropertiesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetVmCustomPropertiesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String predefinedVMProperties = getPredefinedVMProperties();
        String userdefinedVMProperties = getUserDefinedVMProperties();

        // Constructs a String that contains all the custom properties
        // definitions, with ";" as delimiter
        StringBuilder sb = new StringBuilder(predefinedVMProperties);
        if (!predefinedVMProperties.isEmpty() && !userdefinedVMProperties.isEmpty()) {
            sb.append(";");
        }
        sb.append(userdefinedVMProperties);
        getQueryReturnValue().setReturnValue(sb.toString());
    }

    /**
     * @return The predefined VM properties.
     */
    protected String getPredefinedVMProperties() {
        return Config.<String> GetValue(ConfigValues.PredefinedVMProperties, "3.0");
    }

    /**
     * @return The user-defined VM properties.
     */
    protected String getUserDefinedVMProperties() {
        return Config.<String> GetValue(ConfigValues.UserDefinedVMProperties, "3.0");
    }
}
