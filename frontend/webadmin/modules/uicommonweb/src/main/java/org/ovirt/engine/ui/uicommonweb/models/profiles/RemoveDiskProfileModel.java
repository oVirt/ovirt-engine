package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveDiskProfileModel extends RemoveProfileModel<DiskProfile> {

    public RemoveDiskProfileModel(ListModel sourceListModel, List<DiskProfile> profiles) {
        super(sourceListModel, profiles);
        setHelpTag(HelpTag.remove_disk_profile);
        setTitle(ConstantsManager.getInstance().getConstants().removeDiskProfileTitle());
        setHashName("remove_disk_prfoile"); //$NON-NLS-1$
    }

    @Override
    protected ActionType getRemoveActionType() {
        return ActionType.RemoveDiskProfile;
    }

    @Override
    protected ActionParametersBase getRemoveProfileParams(DiskProfile profile) {
        return new DiskProfileParameters(profile);
    }

}
