package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.IscsiBondValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class EditIscsiBondCommand <T extends EditIscsiBondParameters> extends BaseIscsiBondCommand<T> {

    private IscsiBond existingIscsiBond;

    private List<String> addedConnections = new ArrayList<>();
    private List<Guid> addedNetworks = new ArrayList<>();
    private Set<Guid> removedNetworks = new HashSet<>();

    public EditIscsiBondCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public EditIscsiBondCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        IscsiBondValidator validator = new IscsiBondValidator();

        if (!validate(validator.isIscsiBondExist(getExistingIscsiBond()))) {
            return false;
        }

        if (isNameChanged() && !validate(validator.iscsiBondWithTheSameNameExistInDataCenter(getIscsiBond()))) {
            return false;
        }

        return validate(validator.validateAddedLogicalNetworks(getIscsiBond(), getExistingIscsiBond())) &&
                validate(validator.validateAddedStorageConnections(getIscsiBond(), getExistingIscsiBond()));
    }

    @Override
    protected void executeCommand() {

        TransactionSupport.executeInNewTransaction(() -> {
            if (isNameChanged() || isDescriptionChanged()) {
                getDbFacade().getIscsiBondDao().update(getIscsiBond());
            }

            removedNetworks = updateNetworksIds();
            updateConnectionsIds();

            return null;
        });

        if (!addedConnections.isEmpty() || !addedNetworks.isEmpty()) {
            connectAllHostsToStorage(getIscsiBond().getStorageConnectionIds());
        }

        if (!removedNetworks.isEmpty()) {
            addAuditLogForRemovedNetowrks(removedNetworks);
        }
        setSucceeded(true);
    }

    private void addAuditLogForRemovedNetowrks(Set<Guid> removedNetworks) {
        List<String> networkNames = new ArrayList<>();
        for (Guid networkId : removedNetworks) {
            Network network = getDbFacade().getNetworkDao().get(networkId);
            networkNames.add(network.getName());
        }
        addCustomValue("NetworkNames", StringUtils.join(networkNames, ","));
        addCustomValue("IscsiBondName", getIscsiBond().getName());
        auditLogDirector.log(this, AuditLogType.USER_ISCSI_BOND_HOST_RESTART_WARNING);
    }

    private Set<Guid> updateNetworksIds() {
        Set<Guid> beforeChangeNetworkIds = new HashSet<>(getExistingIscsiBond().getNetworkIds());

        for (Guid networkId : getIscsiBond().getNetworkIds()) {
            if (!beforeChangeNetworkIds.remove(networkId)) {
                addedNetworks.add(networkId);
                getDbFacade().getIscsiBondDao().addNetworkToIscsiBond(getExistingIscsiBond().getId(), networkId);
            }
        }

        for (Guid networkId : beforeChangeNetworkIds) {
            getDbFacade().getIscsiBondDao().removeNetworkFromIscsiBond(getExistingIscsiBond().getId(), networkId);
        }
        return beforeChangeNetworkIds;
    }

    private void updateConnectionsIds() {
        Set<String> beforeChangeConnectionIds = new HashSet<>(getExistingIscsiBond().getStorageConnectionIds());

        for (String connectionId : getIscsiBond().getStorageConnectionIds()) {
            if (!beforeChangeConnectionIds.remove(connectionId)) {
                addedConnections.add(connectionId);
                getDbFacade().getIscsiBondDao().addStorageConnectionToIscsiBond(getExistingIscsiBond().getId(), connectionId);
            }
        }

        for (String connectionId : beforeChangeConnectionIds) {
            getDbFacade().getIscsiBondDao().removeStorageConnectionFromIscsiBond(getExistingIscsiBond().getId(), connectionId);
        }
    }

    private IscsiBond getExistingIscsiBond() {
        if (existingIscsiBond != null) {
            return existingIscsiBond;
        }

        existingIscsiBond = getDbFacade().getIscsiBondDao().get(getParameters().getIscsiBond().getId());
        if (existingIscsiBond != null) {
            existingIscsiBond.setNetworkIds(getDbFacade().getIscsiBondDao()
                    .getNetworkIdsByIscsiBondId(existingIscsiBond.getId()));
            existingIscsiBond.setStorageConnectionIds(getDbFacade().getIscsiBondDao()
                    .getStorageConnectionIdsByIscsiBondId(existingIscsiBond.getId()));
        }

        return existingIscsiBond;
    }

    @Override
    protected IscsiBond getIscsiBond() {
        return getParameters().getIscsiBond();
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (encounterConnectionProblems) {
                return AuditLogType.ISCSI_BOND_EDIT_SUCCESS_WITH_WARNING;
            }
            return AuditLogType.ISCSI_BOND_EDIT_SUCCESS;
        }
        return AuditLogType.ISCSI_BOND_EDIT_FAILED;
    }

    @Override
    public Guid getStoragePoolId() {
        return (getExistingIscsiBond() != null) ? getExistingIscsiBond().getStoragePoolId() : null;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__ISCSI_BOND);
    }

    private boolean isNameChanged() {
        return !StringUtils.equals(getIscsiBondName(), getExistingIscsiBond().getName());
    }

    private boolean isDescriptionChanged() {
        return !StringUtils.equals(getIscsiBond().getDescription(), getExistingIscsiBond().getDescription());
    }
}
