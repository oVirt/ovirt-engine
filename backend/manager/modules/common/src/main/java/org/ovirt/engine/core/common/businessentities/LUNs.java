package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.compat.NGuid;

public class LUNs implements Serializable {
    private static final long serialVersionUID = 3026455643639610091L;

    public LUNs() {
    }

    public LUNs(String lUN_id, String physical_volume_id, String volume_group_id) {
        this.id = lUN_id;
        this.physicalVolumeId = physical_volume_id;
        this.volumeGroupId = volume_group_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_lunConnections == null) ? 0 : _lunConnections.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((lunMapping == null) ? 0 : lunMapping.hashCode());
        result = prime * result + ((physicalVolumeId == null) ? 0 : physicalVolumeId.hashCode());
        result = prime * result + deviceSize;
        result = prime * result + ((lunType == null) ? 0 : lunType.hashCode());
        result = prime * result + ((pathsDictionary == null) ? 0 : pathsDictionary.hashCode());
        result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
        result = prime * result + ((productId == null) ? 0 : productId.hashCode());
        result = prime * result + ((serial == null) ? 0 : serial.hashCode());
        result = prime * result + ((vendorId == null) ? 0 : vendorId.hashCode());
        result = prime * result + ((volumeGroupId == null) ? 0 : volumeGroupId.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((diskId == null) ? 0 : diskId.hashCode());
        result = prime * result + ((diskAlias == null) ? 0 : diskAlias.hashCode());
        result = prime * result + ((storageDomainId == null) ? 0 : storageDomainId.hashCode());
        result = prime * result + ((storageDomainName == null) ? 0 : storageDomainName.hashCode());
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
        LUNs other = (LUNs) obj;
        if (_lunConnections == null) {
            if (other._lunConnections != null)
                return false;
        } else if (!_lunConnections.equals(other._lunConnections))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lunMapping == null) {
            if (other.lunMapping != null)
                return false;
        } else if (!lunMapping.equals(other.lunMapping))
            return false;
        if (physicalVolumeId == null) {
            if (other.physicalVolumeId != null)
                return false;
        } else if (!physicalVolumeId.equals(other.physicalVolumeId))
            return false;
        if (deviceSize != other.deviceSize)
            return false;
        if (lunType != other.lunType)
            return false;
        if (pathsDictionary == null) {
            if (other.pathsDictionary != null)
                return false;
        } else if (!pathsDictionary.equals(other.pathsDictionary))
            return false;
        if (vendorName == null) {
            if (other.vendorName != null)
                return false;
        } else if (!vendorName.equals(other.vendorName))
            return false;
        if (productId == null) {
            if (other.productId != null)
                return false;
        } else if (!productId.equals(other.productId))
            return false;
        if (serial == null) {
            if (other.serial != null)
                return false;
        } else if (!serial.equals(other.serial))
            return false;
        if (vendorId == null) {
            if (other.vendorId != null)
                return false;
        } else if (!vendorId.equals(other.vendorId))
            return false;
        if (volumeGroupId == null) {
            if (other.volumeGroupId != null)
                return false;
        } else if (!volumeGroupId.equals(other.volumeGroupId))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (diskId == null) {
            if (other.diskId != null)
                return false;
        } else if (!diskId.equals(other.diskId))
            return false;
        if (diskAlias == null) {
            if (other.diskAlias != null)
                return false;
        } else if (!diskAlias.equals(other.diskAlias))
            return false;
        if (storageDomainId == null) {
            if (other.storageDomainId != null)
                return false;
        } else if (!storageDomainId.equals(other.storageDomainId))
            return false;
        if (storageDomainName == null) {
            if (other.storageDomainName != null)
                return false;
        } else if (!storageDomainName.equals(other.storageDomainName))
            return false;
        return true;
    }

    @Size(min = 1, max = BusinessEntitiesDefinitions.LUN_ID)
    private String id;

    public String getLUN_id() {
        return this.id;
    }

    public void setLUN_id(String value) {
        this.id = value;
    }

    // TODO rename the column
    @Size(max = BusinessEntitiesDefinitions.LUN_PHYSICAL_VOLUME_ID)
    private String physicalVolumeId;

    public String getphysical_volume_id() {
        return this.physicalVolumeId;
    }

    public void setphysical_volume_id(String value) {
        this.physicalVolumeId = value;
    }

    @Size(max = BusinessEntitiesDefinitions.LUN_VOLUME_GROUP_ID)
    private String volumeGroupId;

    public String getvolume_group_id() {
        return this.volumeGroupId;
    }

    public void setvolume_group_id(String value) {
        this.volumeGroupId = value;
    }

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String serial;

    public String getSerial() {
        return this.serial;
    }

    public void setSerial(String value) {
        this.serial = value;
    }

    private Integer lunMapping;

    public Integer getLunMapping() {
        return this.lunMapping;
    }

    public void setLunMapping(Integer value) {
        this.lunMapping = value;
    }

    @Size(max = BusinessEntitiesDefinitions.LUN_VENDOR_ID)
    private String vendorId;

    public String getVendorId() {
        return this.vendorId;
    }

    public void setVendorId(String value) {
        this.vendorId = value;
    }

    @Size(max = BusinessEntitiesDefinitions.LUN_PRODUCT_ID)
    private String productId;

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String value) {
        this.productId = value;
    }

    private java.util.ArrayList<StorageServerConnections> _lunConnections;

    public java.util.ArrayList<StorageServerConnections> getLunConnections() {
        return _lunConnections;
    }

    public void setLunConnections(java.util.ArrayList<StorageServerConnections> value) {
        _lunConnections = value;
    }

    private int deviceSize;

    public int getDeviceSize() {
        return deviceSize;
    }

    public void setDeviceSize(int value) {
        deviceSize = value;
    }

    private String vendorName;

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String value) {
        vendorName = value;
    }

    /**
     * Empty setter for CXF compliance, this field is automatically computed.
     */
    @Deprecated
    public void setPathCount(int pathCount) {
    }

    /**
     * @return Count of how many paths this LUN has.
     */
    public int getPathCount() {
        return (getPathsDictionary() == null ? 0 : getPathsDictionary().size());
    }

    private java.util.HashMap<String, Boolean> pathsDictionary;

    public java.util.HashMap<String, Boolean> getPathsDictionary() {
        return pathsDictionary;
    }

    public void setPathsDictionary(java.util.HashMap<String, Boolean> value) {
        pathsDictionary = value;
    }

    private StorageType lunType = StorageType.forValue(0);

    public StorageType getLunType() {
        return lunType;
    }

    public void setLunType(StorageType value) {
        lunType = value;
    }

    /**
     * LUN's status
     */
    private LunStatus status;

    public LunStatus getStatus() {
        return status;
    }

    public void setStatus(LunStatus value) {
        status = value;
    }

    /**
     * Disk ID - using NGuid since diskId is nullable
     */
    private NGuid diskId;

    public NGuid getDiskId() {
        return diskId;
    }

    public void setDiskId(NGuid value) {
        diskId = value;
    }

    private String diskAlias;

    public String getDiskAlias() {
        return diskAlias;
    }

    public void setDiskAlias(String value) {
        diskAlias = value;
    }

    /**
     * Storage Domain ID - using storageDomainId since diskId is nullable
     */
    private NGuid storageDomainId;

    public NGuid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(NGuid value) {
        storageDomainId = value;
    }

    private String storageDomainName;

    public String getStorageDomainName() {
        return storageDomainName;
    }

    public void setStorageDomainName(String value) {
        storageDomainName = value;
    }

    /**
     * @return Whether the LUN is accessible from at least one of the paths.
     */
    public boolean getAccessible() {
        return getPathsDictionary() != null && getPathsDictionary().values().contains(true);
    }

    /**
     * Empty setter for CXF compliance, this field is automatically computed.
     */
    @Deprecated
    public void setAccessible(boolean accessible) {
    }
}
