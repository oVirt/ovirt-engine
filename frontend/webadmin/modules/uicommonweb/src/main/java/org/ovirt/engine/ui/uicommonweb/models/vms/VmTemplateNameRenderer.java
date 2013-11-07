package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.UIMessages;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VmTemplateNameRenderer {

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    public String render(VM vm) {
        if (vm.getOriginalTemplateName() == null) {
            // fallback for VMs created before adding the support for original template
            return vm.getVmtName();
        } else if (Guid.Empty.equals(vm.getOriginalTemplateGuid())) {
            // created from blank template - don't wrap with the message
            return vm.getOriginalTemplateName();
        } else {
            if (Guid.Empty.equals(vm.getVmtGuid())) {
                return messages.vmTemplateWithCloneProvisioning(vm.getOriginalTemplateName());
            } else {
                return messages.vmTemplateWithThinProvisioning(vm.getOriginalTemplateName());
            }
        }
    }
}
