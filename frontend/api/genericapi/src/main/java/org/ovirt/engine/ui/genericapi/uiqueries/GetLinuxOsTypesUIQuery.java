package org.ovirt.engine.ui.genericapi.uiqueries;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;

public class GetLinuxOsTypesUIQuery extends UIQueryBase {

    public GetLinuxOsTypesUIQuery(UIQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    public void runQuery() {

        ArrayList<VmOsType> linuxOsTypes = new ArrayList<VmOsType>();
        for (VmOsType osType : VmOsType.values()) {
            if(osType.isLinux()) {
                linuxOsTypes.add(osType);
            }
        }

        returnValue.setReturnValue(linuxOsTypes);
    }

}
