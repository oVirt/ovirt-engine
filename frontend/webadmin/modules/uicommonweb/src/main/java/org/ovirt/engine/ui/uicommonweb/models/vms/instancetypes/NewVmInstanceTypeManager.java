package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.CustomInstanceType;
import org.ovirt.engine.ui.uicommonweb.models.vms.EditProfileBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.ProfileBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class NewVmInstanceTypeManager extends VmInstanceTypeManager {

    private final ProfileBehavior networkBehavior = new EditProfileBehavior();

    public NewVmInstanceTypeManager(UnitVmModel model) {
        super(model);
    }

    @Override
    protected VmBase getSource() {
        if (!(getModel().getInstanceTypes().getSelectedItem() instanceof CustomInstanceType)) {
            return (VmBase) getModel().getInstanceTypes().getSelectedItem();
        } else {
            return getModel().getTemplateWithVersion().getSelectedItem() == null
                    ? null
                    : getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();
        }
    }

    @Override
    protected void updateBalloon(VmBase vmBase, boolean continueWithNext) {
        if (!isSourceCustomInstanceType()) {
            super.updateBalloon(vmBase, continueWithNext);
        } else if (continueWithNext) {
            updateRngDevice(vmBase);
        }
    }

    @Override
    protected ProfileBehavior getNetworkProfileBehavior() {
        return networkBehavior;
    }

    @Override
    protected void maybeSetSingleQxlPci(VmBase vmBase) {
        // We are setting the default Qxl support for true on new Linux VM with Spice display protocol
        // The default value cannot be set in the template since it will effect REST API as well
        boolean customInstanceTypeUsed = getModel().getInstanceTypes().getSelectedItem() instanceof CustomInstanceType;
        boolean blankTemplateUsed =
                getModel().getTemplateWithVersion().getSelectedItem() != null
                        && getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion()
                                .getId().equals(Guid.Empty);
        if (customInstanceTypeUsed && blankTemplateUsed) {
            maybeSetEntity(getModel().getIsSingleQxlEnabled(), getModel().getIsQxlSupported());
        } else {
            super.maybeSetSingleQxlPci(vmBase);
        }
    }


}
