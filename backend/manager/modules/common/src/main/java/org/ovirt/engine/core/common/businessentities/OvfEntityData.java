package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class OvfEntityData implements Queryable {
    private static final long serialVersionUID = 3376648147702972152L;

    private Guid entityId;
    private String entityName;
    private VmEntityType entityType;
    private ArchitectureType architecture;
    private Version lowestCompVersion;
    private Guid storageDomainId;
    private String ovfData;
    private String ovfExtraData;
    private VMStatus status;

    public OvfEntityData(Guid entityId,
            String entityName,
            VmEntityType entityType,
            ArchitectureType architecture,
            Version lowestCompVersion,
            Guid storageDomainId,
            String ovfData,
            String ovfExtraData) {
        super();
        this.entityId = entityId;
        this.entityName = entityName;
        this.entityType = entityType;
        this.architecture = architecture;
        this.lowestCompVersion = lowestCompVersion;
        this.storageDomainId = storageDomainId;
        this.ovfData = ovfData;
        this.ovfExtraData = ovfExtraData;
    }

    public OvfEntityData() {
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }

    @Override
    public Object getQueryableId() {
        return getEntityId();
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public VmEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(VmEntityType entityType) {
        this.entityType = entityType;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public String getOvfData() {
        return ovfData;
    }

    public void setOvfData(String ovfData) {
        this.ovfData = ovfData;
    }

    public String getOvfExtraData() {
        return ovfExtraData;
    }

    public void setOvfExtraData(String ovfExtraData) {
        this.ovfExtraData = ovfExtraData;
    }

    public ArchitectureType getArchitecture() {
        return architecture;
    }

    public void setArchitecture(ArchitectureType architecture) {
        this.architecture = architecture;
    }

    public Version getLowestCompVersion() {
        return lowestCompVersion;
    }

    public void setLowestCompVersion(Version lowestCompVersion) {
        this.lowestCompVersion = lowestCompVersion;
    }

    public VMStatus getStatus() {
        return status;
    }

    public void setStatus(VMStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                architecture,
                entityId,
                entityName,
                entityType,
                lowestCompVersion,
                ovfData,
                ovfExtraData,
                storageDomainId,
                status
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OvfEntityData)) {
            return false;
        }
        OvfEntityData other = (OvfEntityData) obj;
        return architecture == other.architecture
                && Objects.equals(entityId, other.entityId)
                && Objects.equals(entityName, other.entityName)
                && entityType == other.entityType
                && Objects.equals(lowestCompVersion, other.lowestCompVersion)
                && Objects.equals(ovfData, other.ovfData)
                && Objects.equals(ovfExtraData, other.ovfExtraData)
                && Objects.equals(storageDomainId, other.storageDomainId)
                && Objects.equals(status, other.status);
    }

}
