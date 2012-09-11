package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NewPoolNameLengthValidation;

public class NewPoolModelBehavior extends PoolModelBehaviorBase {

    @Override
    protected void setupSelectedTemplate(ListModel model, List<VmTemplate> templates) {
        getModel().getTemplate().setSelectedItem(Linq.<VmTemplate> FirstOrDefault(templates));
    }

    public void Template_SelectedItemChanged() {
        super.Template_SelectedItemChanged();

        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        setupWindowModelFrom(template);

        updateHostPinning(template.getMigrationSupport());
        doChangeDefautlHost(template.getdedicated_vm_for_vds());
    }

    @Override
    protected DisplayType extractDisplayType(VmBase vmBase) {
        if (vmBase instanceof VmTemplate) {
            return ((VmTemplate) vmBase).getdefault_display_type();
        }

        return null;
    }

    @Override
    public boolean Validate() {
        boolean parentValidation = super.Validate();
        if (getModel().getName().getIsValid()) {
            getModel().getName().ValidateEntity(new IValidation[] { new NewPoolNameLengthValidation(
                    (String) getModel().getName().getEntity(),
                    Integer.parseInt((getModel().getNumOfDesktops().getEntity().toString())),
                    (VmOsType) getModel().getOSType().getSelectedItem()
                    ) });

            return getModel().getName().getIsValid() && parentValidation;
        }

        return parentValidation;
    }
}
