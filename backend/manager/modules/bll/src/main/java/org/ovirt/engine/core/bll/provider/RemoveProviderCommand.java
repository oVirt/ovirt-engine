package org.ovirt.engine.core.bll.provider;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class RemoveProviderCommand<P extends ProviderParameters> extends CommandBase<P> {

    private Provider deletedProvider;

    public RemoveProviderCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveProviderCommand(P parameters) {
        super(parameters);
    }

    private Provider getDeletedProvider() {
        if (deletedProvider == null) {
            deletedProvider = getProviderDao().get(getParameters().getProvider().getId());
        }

        return deletedProvider;
    }

    @Override
    protected boolean canDoAction() {
        ProviderValidator validator = new ProviderValidator(getDeletedProvider());
        return validate(validator.providerIsSet());
    }

    @Override
    protected void executeCommand() {
        getProviderDao().remove(getParameters().getProvider().getId());
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__PROVIDER);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(RemoveEntity.class);
        return super.getValidationGroups();
    }

    private ProviderDao getProviderDao() {
        return getDbFacade().getProviderDao();
    }
}
