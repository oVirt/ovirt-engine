package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveCpuProfileModel extends RemoveProfileModel<CpuProfile> {

    public RemoveCpuProfileModel(ListModel sourceListModel, List<CpuProfile> profiles) {
        super(sourceListModel, profiles);
        setHelpTag(HelpTag.remove_cpu_profile);
        setTitle(ConstantsManager.getInstance().getConstants().removeCpuProfileTitle());
        setHashName("remove_cpu_prfoile"); //$NON-NLS-1$
    }

    @Override
    protected ActionType getRemoveActionType() {
        return ActionType.RemoveCpuProfile;
    }

    @Override
    protected ActionParametersBase getRemoveProfileParams(CpuProfile profile) {
        return new CpuProfileParameters(profile);
    }

}
