package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code VmTypeTemplate}.
 */
public class VmTypeColumn extends WebAdminImageResourceColumn<VM> {

    @Override
    public ImageResource getValue(VM vm) {
        if (vm.getVmPoolId() == null) {
            switch (vm.getVmType()) {
            case Desktop:
                return getApplicationResources().desktopImage();
            case Server:
                return getApplicationResources().serverImage();
            default:
                return getApplicationResources().questionMarkImage();
            }
        } else {
            return getApplicationResources().manyDesktopsImage();
        }
    }

}
