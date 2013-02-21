package org.ovirt.engine.core.bll.provider;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class UpdateProviderCommand<P extends ProviderParameters> extends CommandBase<P> {

    private Provider oldProvider;

    public UpdateProviderCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateProviderCommand(P parameters) {
        super(parameters);
    }

    private Provider getProvider() {
        return getParameters().getProvider();
    }

    private Provider getOldProvider() {
        if (oldProvider == null) {
            oldProvider = getProviderDao().get(getProvider().getId());
        }

        return oldProvider;
    }

    @Override
    protected boolean canDoAction() {
        ProviderValidator validatorOld = new ProviderValidator(getOldProvider());
        ProviderValidator validatorNew = new ProviderValidator(getProvider());
        return validate(validatorOld.providerIsSet())
                && nameKept(getOldProvider()) || validate(validatorNew.nameAvailable());
    }

    private boolean nameKept(Provider oldProvider) {
        return oldProvider.getName().equals(getProvider().getName());
    }

    @Override
    protected void executeCommand() {
        getProviderDao().update(getProvider());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__PROVIDER);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    private ProviderDao getProviderDao() {
        return getDbFacade().getProviderDao();
    }
}
