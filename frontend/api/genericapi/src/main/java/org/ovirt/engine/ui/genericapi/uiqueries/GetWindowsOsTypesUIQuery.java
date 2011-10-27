package org.ovirt.engine.ui.genericapi.uiqueries;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;

public class GetWindowsOsTypesUIQuery extends UIQueryBase {

    public GetWindowsOsTypesUIQuery(UIQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    public void runQuery() {

        ArrayList<VmOsType> windowsOsTypes = new ArrayList<VmOsType>();
        for (VmOsType osType : VmOsType.values()) {
            if(osType.isWindows()) {
                windowsOsTypes.add(osType);
            }
        }

        returnValue.setReturnValue(windowsOsTypes);
    }

}
