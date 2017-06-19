package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewCpuProfileModel extends CpuProfileBaseModel {

    public NewCpuProfileModel(IModel sourceModel,
            Guid dcId) {
        super(sourceModel, dcId, null, ActionType.AddCpuProfile);
        setTitle(ConstantsManager.getInstance().getConstants().cpuProfileTitle());
        setHelpTag(HelpTag.new_cpu_profile);
        setHashName("new_cpu_profile"); //$NON-NLS-1$
    }

    public NewCpuProfileModel() {
        this(null, null);
    }

}
