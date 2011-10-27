package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code VmStatusTemplate}.
 */
public class VmStatusColumn extends ImageResourceColumn<VM> {

    @Override
    public ImageResource getValue(VM vm) {
        switch (vm.getstatus()) {
        case Up:
            return getApplicationResources().upImage();
        case PoweringUp:
        case RebootInProgress:
            return getApplicationResources().playImage();
        case WaitForLaunch:
        case ImageLocked:
        case MigratingFrom:
        case MigratingTo:
            return getApplicationResources().waitImage();
        case Suspended:
        case Paused:
            return getApplicationResources().pauseImage();
        case Unknown:
            return getApplicationResources().questionMarkImage();
        default:
            return getApplicationResources().stopImage();
        }
    }

}
