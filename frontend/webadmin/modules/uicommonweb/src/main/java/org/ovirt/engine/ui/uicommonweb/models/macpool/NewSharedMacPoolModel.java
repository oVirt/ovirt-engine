package org.ovirt.engine.ui.uicommonweb.models.macpool;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewSharedMacPoolModel extends SharedMacPoolModel {

    public NewSharedMacPoolModel(Model sourceModel) {
        super(sourceModel, ActionType.AddMacPool);
        setTitle(ConstantsManager.getInstance().getConstants().newSharedMacPoolTitle());
        setHashName("new_shared_mac_pool"); //$NON-NLS-1$
        setHelpTag(HelpTag.new_shared_mac_pool);
    }

    public static class ClosingWithSetConfirmWindow extends NewSharedMacPoolModel {
        public ClosingWithSetConfirmWindow(Model sourceModel) {
            super(sourceModel);
        }

        @Override
        protected void cancel() {
            sourceModel.setConfirmWindow(null);
        }
    }

}
