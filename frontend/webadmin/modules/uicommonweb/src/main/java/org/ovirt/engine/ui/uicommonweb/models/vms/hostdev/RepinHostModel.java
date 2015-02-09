package org.ovirt.engine.ui.uicommonweb.models.vms.hostdev;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RepinHostModel extends ModelWithPinnedHost {

    public RepinHostModel() {
        setTitle(ConstantsManager.getInstance().getConstants().repinHostTitle());
        setHelpTag(HelpTag.repin_host);
        setHashName("add_host_device"); //$NON-NLS-1$
    }

    @Override
    public void init(VM vm) {
        super.init(vm);

        initHosts();
    }
}
