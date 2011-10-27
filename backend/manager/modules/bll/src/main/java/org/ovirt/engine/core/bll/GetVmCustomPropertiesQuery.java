/**
 *
 */
package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

/**
 *
 */
public class GetVmCustomPropertiesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    /**
     * @param parameters
     */
    public GetVmCustomPropertiesQuery(P parameters) {
        super(parameters);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.bll.QueriesCommandBase#ExecuteQueryCommand()
     */
    @Override
    protected void executeQueryCommand() {
        String predefinedVMProperties = Config.<String> GetValue(ConfigValues.PredefinedVMProperties, "3.0");
        String userdefinedVMProperties = Config.<String> GetValue(ConfigValues.UserDefinedVMProperties, "3.0");
        // Constructs a String that contains all the custom properties
        // definitions, with ";" as delimiter
        StringBuilder sb = new StringBuilder("");
        sb.append(predefinedVMProperties);
        if (!predefinedVMProperties.isEmpty() && !userdefinedVMProperties.isEmpty()) {
            sb.append(";");
        }
        sb.append(userdefinedVMProperties);
        getQueryReturnValue().setReturnValue(sb.toString());

    }

}
