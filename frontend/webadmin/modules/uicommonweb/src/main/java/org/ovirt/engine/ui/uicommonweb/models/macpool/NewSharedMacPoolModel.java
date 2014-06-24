package org.ovirt.engine.ui.uicommonweb.models.macpool;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewSharedMacPoolModel extends SharedMacPoolModel {

    public NewSharedMacPoolModel(Model sourceModel) {
        super(sourceModel, VdcActionType.AddMacPool);
        sourceModel.setTitle(ConstantsManager.getInstance().getConstants().newSharedMacPoolTitle());
        sourceModel.setHashName("new_shared_mac_pool"); //$NON-NLS-1$
        sourceModel.setHelpTag(HelpTag.new_shared_mac_pool);
    }

}
