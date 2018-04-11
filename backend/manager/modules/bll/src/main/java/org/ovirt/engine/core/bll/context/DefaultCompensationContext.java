package org.ovirt.engine.core.bll.context;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.EntityStatusSnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.TransientCompensationBusinessEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.Serializer;

/**
 * Default context used to track entities that are changing during a command's execution and save the changes at each
 * state change to the DB.
 */
public class DefaultCompensationContext extends CompensationContextBase {

    /**
     * A set of all the entities which have been snapshotted ever in this context, since we only want to save the
     * initial snapshot of each entity the command has changed/created.
     */
    private Set<CachedEntityEntry> cachedEntities = new HashSet<>();

    /**
     * All the entities that have been changed/added since the state/command began.
     */
    private List<BusinessEntitySnapshot> entitiesToPersist = new LinkedList<>();

    /**
     * The serializer which is used to convert the entity to a snapshot.
     */
    private Serializer snapshotSerializer;

    /**
     * The Dao which is used to track all the changed business entities.
     */
    private BusinessEntitySnapshotDao businessEntitySnapshotDao;

    /**
     * The id of the command which this context is tracking.
     */
    private Guid commandId;

    /**
     * The type of the command which this context is tracking.
     */
    private String commandType;

    /**
     * @param snapshotSerializer
     *            the snapshotSerializer to set
     */
    public void setSnapshotSerializer(Serializer snapshotSerializer) {
        this.snapshotSerializer = snapshotSerializer;
    }

    /**
     * @param businessEntitySnapshotDao
     *            the businessEntitySnapshotDao to set
     */
    public void setBusinessEntitySnapshotDao(BusinessEntitySnapshotDao businessEntitySnapshotDao) {
        this.businessEntitySnapshotDao = businessEntitySnapshotDao;
    }

    /**
     * @param commandId
     *            the commandId to set
     */
    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
    }

    /**
     * Return the command id for the compensation context
     * @return the command id
     */
    public Guid getCommandId() {
        return commandId;
    }

    /**
     * @param commandType
     *            the commandType to set
     */
    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    @Override
    public void snapshotEntity(BusinessEntity<?> entity) {
        snapshotEntityInMemory(entity, entity, SnapshotType.DELETED_OR_UPDATED_ENTITY);
    }

    @Override
    public void snapshotEntityUpdated(BusinessEntity<?> entity) {
        snapshotEntityInMemory(entity, entity, SnapshotType.UPDATED_ONLY_ENTITY);
    }

    @Override
    public void snapshotNewEntity(BusinessEntity<?> entity) {
        snapshotEntityInMemory(entity, entity.getId(), SnapshotType.NEW_ENTITY_ID);
    }

    @Override
    public <T extends Enum<?>> void  snapshotEntityStatus(BusinessEntityWithStatus<?, T> entity, T status) {
        EntityStatusSnapshot snapshot = new EntityStatusSnapshot();
        snapshot.setId(entity.getId());
        snapshot.setStatus(status);
        snapshotEntityInMemory(entity, snapshot, SnapshotType.CHANGED_STATUS_ONLY);
    }

    @Override
    public void snapshotObject(TransientCompensationBusinessEntity entity) {
        snapshotEntityInMemory(entity, entity, SnapshotType.TRANSIENT_ENTITY);
    }

    @Override
    public <T extends Enum<?>> void snapshotEntityStatus(BusinessEntityWithStatus<?, T> entity) {
        snapshotEntityStatus(entity, entity.getStatus());
    }

    /**
     * Save a snapshot of the entity but only if it is new to this context.
     *
     * @param entity
     *            The entity to save a snapshot of.
     * @param payload
     *            The payload to be serialized and saved.
     * @param snapshotType
     *            The type of snapshot we're taking, so that in compensation we know what is the payload type, and how
     *            to use it to revert the entity state.
     */
    private void snapshotEntityInMemory(BusinessEntity<?> entity, Serializable payload, SnapshotType snapshotType) {
        CachedEntityEntry cachedEntityEntry = new CachedEntityEntry(entity, snapshotType);
        checkEntityForRollback(entity);
        if (!cachedEntities.contains(cachedEntityEntry)) {
            cachedEntities.add(cachedEntityEntry);

            entitiesToPersist.add(createBusinessEntitySnapshot(entity, payload, snapshotType));
        }
    }

    private BusinessEntitySnapshot createBusinessEntitySnapshot(BusinessEntity<?> entity,
            Serializable payload,
            SnapshotType snapshotType) {

        BusinessEntitySnapshot entitySnapshot = new BusinessEntitySnapshot();
        entitySnapshot.setCommandId(commandId);
        entitySnapshot.setCommandType(commandType);
        entitySnapshot.setEntityId(String.valueOf(entity.getId()));
        entitySnapshot.setEntityType(entity.getClass().getName());
        entitySnapshot.setEntitySnapshot((String) snapshotSerializer.serialize(payload));
        entitySnapshot.setSnapshotClass(payload.getClass().getName());
        entitySnapshot.setSnapshotType(snapshotType);
        entitySnapshot.setInsertionOrder(cachedEntities.size());
        return entitySnapshot;
    }

    private void checkEntityForRollback(BusinessEntity<?> entity) {
        if(entity == null) {
            throw new IllegalArgumentException("Can not create snapshot from a null entity");
        }

        @SuppressWarnings("unchecked")
        Class<BusinessEntity<Serializable>> entityClass = (Class<BusinessEntity<Serializable>>) entity.getClass();
        boolean verifyDaoExistence = ! (entity instanceof TransientCompensationBusinessEntity);

        if (verifyDaoExistence) {
            //callMustNotFail
            Injector.get(DbFacade.class).getDaoForEntity(entityClass);
        }
    }

    @Override
    public void stateChanged() {
        for (BusinessEntitySnapshot snapshot : entitiesToPersist) {
            businessEntitySnapshotDao.save(snapshot);
        }

        entitiesToPersist.clear();
    }

    @Override
    public void doAfterCompensationCleanup() {
        doClearCollectedCompensationData();
    }

    @Override
    public void doCleanupCompensationDataAfterSuccessfulCommand() {
        doClearCollectedCompensationData();
    }

    @Override
    public void doClearCollectedCompensationData() {
        businessEntitySnapshotDao.removeAllForCommandId(commandId);
        cachedEntities.clear();
        entitiesToPersist.clear();
    }

    /* -- Inner types -- */

    /**
     * Represents a cached entity which is made of the snapshot type, the entity class and the id, so that we can track
     * which entities have already been recorded and which entities have not.
     */
    private class CachedEntityEntry {

        /**
         * The id of the cached entity.
         */
        private Object id;

        /**
         * The class of the cached entity.
         */
        private Class<?> entityClass;

        /**
         * The type of snapshot that is cached.
         */
        private SnapshotType snapshotType;

        /**
         * Construct a new cached entry for the given entity.
         *
         * @param entity
         *            The entity to construct a cache entry for.
         * @param snapshotType
         *            The type of snapshot.
         */
        public CachedEntityEntry(BusinessEntity<?> entity, SnapshotType snapshotType) {
            super();
            this.id = entity.getId();
            this.entityClass = entity.getClass();
            this.snapshotType = snapshotType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    getOuterType(),
                    entityClass,
                    id,
                    snapshotType
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CachedEntityEntry)) {
                return false;
            }
            CachedEntityEntry other = (CachedEntityEntry) obj;
            return Objects.equals(getOuterType(), other.getOuterType())
                    && Objects.equals(entityClass, other.entityClass)
                    && Objects.equals(id, other.id)
                    && Objects.equals(snapshotType, other.snapshotType);
        }

        private DefaultCompensationContext getOuterType() {
            return DefaultCompensationContext.this;
        }
    }

    @Override
    public void snapshotEntities(Collection<? extends BusinessEntity<?>> entities) {
        if (entities != null) {
            for (BusinessEntity<?> entity : entities) {
                snapshotEntity(entity);
            }
        }
    }

    @Override
    public void snapshotNewEntities(Collection<? extends BusinessEntity<?>> entities) {
        if (entities != null) {
            for (BusinessEntity<?> entity : entities) {
                snapshotNewEntity(entity);
            }
        }
    }

    @Override
    public boolean isCompensationEnabled() {
        return true;
    }
}
