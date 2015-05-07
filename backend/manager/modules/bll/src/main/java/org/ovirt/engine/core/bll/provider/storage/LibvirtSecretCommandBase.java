package org.ovirt.engine.core.bll.provider.storage;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LibvirtSecretDao;

public abstract class LibvirtSecretCommandBase extends CommandBase<LibvirtSecretParameters> {

    public LibvirtSecretCommandBase(LibvirtSecretParameters parameters) {
        super(parameters);
    }

    protected LibvirtSecretDao getLibvirtSecretDAO() {
        return getDbFacade().getLibvirtSecretDao();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__AUTHENTICATION_KEY);
    }

    @Override
    protected void executeCommand() {
        getReturnValue().setActionReturnValue(getParameters().getLibvirtSecret().getId());
    }

    public String getLibvirtSecretUUID() {
        return getParameters().getLibvirtSecret().getId().toString();
    }
}
