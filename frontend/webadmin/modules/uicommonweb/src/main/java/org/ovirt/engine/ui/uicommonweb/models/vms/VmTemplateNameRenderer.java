package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class VmTemplateNameRenderer {

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    public String render(VM vm) {
        if (vm.getOriginalTemplateName() == null) {
            // fallback for VMs created before adding the support for original template
            if (!Guid.Empty.equals(vm.getVmtGuid())) {
                // it is a thin provisioned VM, just the original template is not filled
                return messages.vmTemplateWithThinProvisioning(vm.getVmtName());
            }

            // no information if it was based on original template or a blank template - falling back show Blank as it was before 3.4
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
