package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class OvfEntityData implements IVdcQueryable {
    private static final long serialVersionUID = 3376648147702972152L;

    private Guid entityId;
    private String entityName;
    private VmEntityType entityType;
    private ArchitectureType architecture;
    private Version lowestCompVersion;
    private Guid storageDomainId;
    private String ovfData;
    private String ovfExtraData;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((architecture == null) ? 0 : architecture.hashCode());
        result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
        result = prime * result + ((entityName == null) ? 0 : entityName.hashCode());
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + ((lowestCompVersion == null) ? 0 : lowestCompVersion.hashCode());
        result = prime * result + ((ovfData == null) ? 0 : ovfData.hashCode());
        result = prime * result + ((ovfExtraData == null) ? 0 : ovfExtraData.hashCode());
        result = prime * result + ((storageDomainId == null) ? 0 : storageDomainId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OvfEntityData other = (OvfEntityData) obj;
        if (architecture != other.architecture)
            return false;
        if (entityId == null) {
            if (other.entityId != null)
                return false;
        } else if (!entityId.equals(other.entityId))
            return false;
        if (entityName == null) {
            if (other.entityName != null)
                return false;
        } else if (!entityName.equals(other.entityName))
            return false;
        if (entityType != other.entityType)
            return false;
        if (lowestCompVersion == null) {
            if (other.lowestCompVersion != null)
                return false;
        } else if (!lowestCompVersion.equals(other.lowestCompVersion))
            return false;
        if (ovfData == null) {
            if (other.ovfData != null)
                return false;
        } else if (!ovfData.equals(other.ovfData))
            return false;
        if (ovfExtraData == null) {
            if (other.ovfExtraData != null)
                return false;
        } else if (!ovfExtraData.equals(other.ovfExtraData))
            return false;
        if (storageDomainId == null) {
            if (other.storageDomainId != null)
                return false;
        } else if (!storageDomainId.equals(other.storageDomainId))
            return false;
        return true;
    }

}
