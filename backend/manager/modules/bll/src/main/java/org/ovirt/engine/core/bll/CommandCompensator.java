package org.ovirt.engine.core.bll;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.KeyValue;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.TransientCompensationBusinessEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDao;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.StatusAwareDao;
import org.ovirt.engine.core.utils.Deserializer;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandCompensator {
    private static final Logger log = LoggerFactory.getLogger(CommandCompensator.class);

    @Inject
    private DbFacade dbFacade;

    @Inject
    private BusinessEntitySnapshotDao businessEntitySnapshotDao;

    @Inject
    private ObjectCompensation objectCompensation;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    /**
     * Handles compensation in case of uncompleted compensation-aware commands resulted from server failure.
     */
    public void compensate() {
        // get all command snapshot entries
        List<KeyValue> commandSnapshots = businessEntitySnapshotDao.getAllCommands();
        for (KeyValue commandSnapshot : commandSnapshots) {
            if(skipCompensationOnStartup((Guid) commandSnapshot.getKey())) {
                continue;
            }
            // create an instance of the related command by its class name and command id
            try {
                compensate((Guid) commandSnapshot.getKey(), (String) commandSnapshot.getValue(), null);
            } catch (Throwable e) {
                log.error(
                        "Failed to run compensation on startup for Command '{}', Command Id '{}': {}",
                        commandSnapshot.getValue(),
                        commandSnapshot.getKey(),
                        e.getMessage());
                log.error("Exception", e);
            }
            log.info("Running compensation on startup for Command '{}', Command Id '{}'",
                    commandSnapshot.getValue(),
                    commandSnapshot.getKey());
        }
    }

    @SuppressWarnings({ "unchecked", "synthetic-access" })
    public void compensate(Guid commandId, String commandType, CompensationContext compensationContext) {
        TransactionSupport.executeInNewTransaction(() -> {
            Deserializer deserializer =
                    SerializationFactory.getDeserializer();
            List<BusinessEntitySnapshot> entitySnapshots = businessEntitySnapshotDao.getAllForCommandId(commandId);
            log.debug("Command [id={}]: {} compensation data.",
                    commandId,
                    entitySnapshots.isEmpty() ? "No" : "Going over");
            for (BusinessEntitySnapshot snapshot : entitySnapshots) {
                Class<Serializable> snapshotClass =
                        (Class<Serializable>) ReflectionUtils.getClassFor(snapshot.getSnapshotClass());
                Serializable snapshotData = deserializer.deserialize(snapshot.getEntitySnapshot(), snapshotClass);
                log.info("Command [id={}]: Compensating {} of {}; snapshot: {}.",
                        commandId,
                        snapshot.getSnapshotType(),
                        snapshot.getEntityType(),
                        snapshot.getSnapshotType() == BusinessEntitySnapshot.SnapshotType.DELETED_OR_UPDATED_ENTITY
                                ? "id=" + snapshot.getEntityId()
                                : snapshotData.toString());
                Class<BusinessEntity<Serializable>> entityClass =
                        (Class<BusinessEntity<Serializable>>) ReflectionUtils.getClassFor(snapshot.getEntityType());

                switch (snapshot.getSnapshotType()) {
                case CHANGED_STATUS_ONLY:
                    BusinessEntitySnapshot.EntityStatusSnapshot entityStatusSnapshot =
                            (BusinessEntitySnapshot.EntityStatusSnapshot) snapshotData;
                    ((StatusAwareDao<Serializable, Enum<?>>) getDaoForEntity(entityClass)).updateStatus(
                            entityStatusSnapshot.getId(), entityStatusSnapshot.getStatus());
                    break;
                case DELETED_OR_UPDATED_ENTITY:
                    deletedOrUpdateEntity(entityClass, (BusinessEntity<Serializable>) snapshotData);
                    break;
                case UPDATED_ONLY_ENTITY:
                    getDaoForEntity(entityClass).update((BusinessEntity<Serializable>) snapshotData);
                    break;
                case NEW_ENTITY_ID:
                    getDaoForEntity(entityClass).remove(snapshotData);
                    break;
                case TRANSIENT_ENTITY:
                    objectCompensation.compensate(commandType, (TransientCompensationBusinessEntity) snapshotData);
                    break;
                default:
                    throw new IllegalArgumentException(String.format(
                            "Unknown %s value, unable to compensate value %s.",
                            BusinessEntitySnapshot.SnapshotType.class.getName(),
                            snapshot.getSnapshotType()));
                }
            }
            if (compensationContext == null) {
                businessEntitySnapshotDao.removeAllForCommandId(commandId);
            } else {
                compensationContext.afterCompensationCleanup();
            }
            return null;
        });
    }

    /**
     * This method checks if we should compensate the command with the given id on engine startup.
     * @param commandId The command ID we check.
     * @return true if we wish to skip the compensation, false otherwise.
     */
    private boolean skipCompensationOnStartup(Guid commandId) {
        CommandBase<?> cmd = commandCoordinatorUtil.retrieveCommand(commandId);
        return cmd != null && cmd.getParameters().isCompensationPhaseEndCommand()
                && cmd.getActionType() != ActionType.DeactivateStorageDomainWithOvfUpdate
                && commandCoordinatorUtil.getChildCommandIds(commandId)
                .stream()
                .anyMatch(command -> commandCoordinatorUtil.getCommandStatus(command) == CommandStatus.ACTIVE
                        && commandCoordinatorUtil.getCommandExecutionStatus(command) == CommandExecutionStatus.EXECUTED);
    }

    private void deletedOrUpdateEntity(Class<BusinessEntity<Serializable>> entityClass,
            BusinessEntity<Serializable> entitySnapshot) {
        GenericDao<BusinessEntity<Serializable>, Serializable> daoForEntity = getDaoForEntity(entityClass);

        if (daoForEntity.get(entitySnapshot.getId()) == null) {
            daoForEntity.save(entitySnapshot);
        } else {
            daoForEntity.update(entitySnapshot);
        }
    }

    private GenericDao<BusinessEntity<Serializable>, Serializable> getDaoForEntity(
            Class<BusinessEntity<Serializable>> entityClass) {

        return dbFacade.getDaoForEntity(entityClass);
    }
}
