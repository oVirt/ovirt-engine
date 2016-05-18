package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents a single entry of compensation business entity snapshot information
 */
public class BusinessEntitySnapshot {

    /**
     * The type of snapshot taken, so that during compensation we can handle the snapshot correctly.
     */
    public enum SnapshotType {
        DELETED_OR_UPDATED_ENTITY,
        NEW_ENTITY_ID,
        CHANGED_STATUS_ONLY,
        UPDATED_ONLY_ENTITY,
        TRANSIENT_ENTITY
    }

    /**
     * Holds the status snapshot for a given entity (identified by the id held).
     */
    public static class EntityStatusSnapshot implements Serializable {
        /**
         * Used by Java's Serialization mechanism.
         */
        private static final long serialVersionUID = 7131902100063632363L;
        private Serializable id;
        private Enum<?> status;

        /**
         * @return the id
         */
        public Serializable getId() {
            return id;
        }

        /**
         * @param id
         *            the id to set
         */
        public void setId(Serializable id) {
            this.id = id;
        }

        /**
         * @return the status
         */
        public Enum<?> getStatus() {
            return status;
        }

        /**
         * @param status
         *            the status to set
         */
        public void setStatus(Enum<?> status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return ToStringBuilder.forInstance(this)
                    .append("id", id)
                    .append("status", status)
                    .build();
        }
    }

    private Guid Id;
    private Guid commandId;
    private String commandType;
    private String entityId;
    private String entityType;
    private String entitySnapshot;
    private String snapshotClass;
    private SnapshotType snapshotType;
    private int insertionOrder;

    public Guid getId() {
        return Id;
    }

    public void setId(Guid id) {
        Id = id;
    }

    public Guid getCommandId() {
        return commandId;
    }

    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntitySnapshot() {
        return entitySnapshot;
    }

    public void setEntitySnapshot(String entitySnapshot) {
        this.entitySnapshot = entitySnapshot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                commandId,
                entityId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BusinessEntitySnapshot)) {
            return false;
        }
        BusinessEntitySnapshot other = (BusinessEntitySnapshot) obj;
        return Objects.equals(commandId, other.commandId)
                && Objects.equals(entityId, other.entityId);
    }

    /**
     * @return the snapshotClass
     */
    public String getSnapshotClass() {
        return snapshotClass;
    }

    /**
     * @param snapshotClass the snapshotClass to set
     */
    public void setSnapshotClass(String snapshotClass) {
        this.snapshotClass = snapshotClass;
    }

    /**
     * @return the snapshotType
     */
    public SnapshotType getSnapshotType() {
        return snapshotType;
    }

    /**
     * @param snapshotType
     *            the snapshotType to set
     */
    public void setSnapshotType(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }

    public int getInsertionOrder() {
        return insertionOrder;
    }

    public void setInsertionOrder(int insertionOrder) {
        this.insertionOrder = insertionOrder;
    }
}
