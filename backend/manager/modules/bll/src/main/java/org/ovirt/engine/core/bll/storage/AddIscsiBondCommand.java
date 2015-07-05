package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.validator.IscsiBondValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.AddIscsiBondParameters;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AddIscsiBondCommand<T extends AddIscsiBondParameters> extends BaseIscsiBondCommand<T> {

    public AddIscsiBondCommand(T parameters) {
        super(parameters);
    }

    public AddIscsiBondCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean canDoAction() {
        if (!FeatureSupported.isIscsiMultipathingSupported(getStoragePool().getCompatibilityVersion())) {
            return failCanDoAction(EngineMessage.ISCSI_BOND_NOT_SUPPORTED);
        }

        IscsiBondValidator validator = new IscsiBondValidator();
        return validate(validator.iscsiBondWithTheSameNameExistInDataCenter(getIscsiBond())) &&
                validate(validator.validateAddedLogicalNetworks(getIscsiBond())) &&
                validate(validator.validateAddedStorageConnections(getIscsiBond()));
    }

    @Override
    protected void executeCommand() {
        final IscsiBond iscsiBond = getIscsiBond();

        iscsiBond.setId(Guid.newGuid());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getDbFacade().getIscsiBondDao().save(iscsiBond);

                for (Guid networkId : iscsiBond.getNetworkIds()) {
                    getDbFacade().getIscsiBondDao().addNetworkToIscsiBond(iscsiBond.getId(), networkId);
                }

                for (String connectionId : iscsiBond.getStorageConnectionIds()) {
                    getDbFacade().getIscsiBondDao().addStorageConnectionToIscsiBond(iscsiBond.getId(), connectionId);
                }

                getReturnValue().setActionReturnValue(iscsiBond.getId());
                return null;
            }
        });

        connectAllHostsToStorage(iscsiBond.getStorageConnectionIds());
        setSucceeded(true);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (encounterConnectionProblems) {
                return AuditLogType.ISCSI_BOND_ADD_SUCCESS_WITH_WARNING;
            }
            return AuditLogType.ISCSI_BOND_ADD_SUCCESS;
        }
        return AuditLogType.ISCSI_BOND_ADD_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__ADD);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__ISCSI_BOND);
    }

    protected IscsiBond getIscsiBond() {
        return getParameters().getIscsiBond();
    }

    @Override
    public Guid getStoragePoolId() {
        return getIscsiBond().getStoragePoolId();
    }
}
