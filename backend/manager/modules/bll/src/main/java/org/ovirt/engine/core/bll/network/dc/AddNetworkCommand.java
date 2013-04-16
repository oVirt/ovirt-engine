package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AddNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    public AddNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getNetwork().setId(Guid.NewGuid());
        getNetworkDAO().save(getNetwork());
        NetworkHelper.addPermissions(getCurrentUser().getUserId(), getNetwork().getId(), getParameters().isPublicUse());
        getReturnValue().setActionReturnValue(getNetwork().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }

    @Override
    protected boolean canDoAction() {
        NetworkValidator validator = new NetworkValidator(getNetwork());
        return validate(validator.dataCenterExists())
                && validate(validator.vmNetworkSetCorrectly())
                && validate(validator.stpForVmNetworkOnly())
                && validate(validator.mtuValid())
                && validate(validator.networkPrefixValid())
                && validate(validator.networkNameNotUsed())
                && validate(validator.vlanIdNotUsed());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_NETWORK : AuditLogType.NETWORK_ADD_NETWORK_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId() == null ? null
                : getStoragePoolId().getValue(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }
}
