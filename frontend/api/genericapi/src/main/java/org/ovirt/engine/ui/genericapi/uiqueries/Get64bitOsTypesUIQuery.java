package org.ovirt.engine.ui.genericapi.uiqueries;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;

public class Get64bitOsTypesUIQuery extends UIQueryBase {

    public Get64bitOsTypesUIQuery(UIQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    public void runQuery() {

        ArrayList<VmOsType> x64OsTypes = new ArrayList<VmOsType>();
        for (VmOsType osType : VmOsType.values()) {
            if(osType.getIs64Bit()) {
                x64OsTypes.add(osType);
            }
        }

        returnValue.setReturnValue(x64OsTypes);
    }

}
