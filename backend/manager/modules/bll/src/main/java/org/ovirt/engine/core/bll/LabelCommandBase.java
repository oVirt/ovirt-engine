package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LabelActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LabelDao;

public abstract class LabelCommandBase<T extends LabelActionParametersBase> extends CommandBase<T> {
    private Label label;

    @Inject
    protected LabelDao labelDao;

    protected Label getLabel() {
        if (label == null && getLabelId() != null) {
            label = labelDao.get(getLabelId());
        }
        return label;
    }

    protected Guid getLabelId() {
        return getParameters().getLabelId();
    }

    public String getLabelName() {
        return getLabel() != null ? getLabel().getName() : null;
    }

    public LabelCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.TAG_MANAGEMENT));
    }

    protected void setAuditCustomValues() {
        addCustomValue("labelName", getLabelName());
    }

    @Override
    public void init() {
        setAuditCustomValues();
    }

}
