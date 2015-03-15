package org.ovirt.engine.core.common.businessentities.storage;

import java.util.ArrayList;
import java.util.HashMap;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class LUNs implements BusinessEntity<String> {
    private static final long serialVersionUID = 3026455643639610091L;

    @Size(min = 1, max = BusinessEntitiesDefinitions.LUN_ID)
    private String id;

    // TODO rename the column
    @Size(max = BusinessEntitiesDefinitions.LUN_PHYSICAL_VOLUME_ID)
    private String physicalVolumeId;

    @Size(max = BusinessEntitiesDefinitions.LUN_VOLUME_GROUP_ID)
    private String volumeGroupId;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String serial;

    private Integer lunMapping;

    @Size(max = BusinessEntitiesDefinitions.LUN_VENDOR_ID)
    private String vendorId;

    @Size(max = BusinessEntitiesDefinitions.LUN_PRODUCT_ID)
    private String productId;

    private ArrayList<StorageServerConnections> _lunConnections;

    private int deviceSize;

    private String vendorName;

    private HashMap<String, Boolean> pathsDictionary;

    private StorageType lunType;

    /**
     * LUN's status
     */
    private LunStatus status;

    /**
     * Disk ID - using Guid since diskId is nullable
     */
    private Guid diskId;

    private String diskAlias;

    /**
     * Storage Domain ID - using storageDomainId since diskId is nullable
     */
    private Guid storageDomainId;

    private String storageDomainName;

    public LUNs() {
        lunType = StorageType.UNKNOWN;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((_lunConnections == null) ? 0 : _lunConnections.hashCode());
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LUNs other = (LUNs) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(_lunConnections, other._lunConnections)
                && ObjectUtils.objectsEqual(lunMapping, other.lunMapping)
                && ObjectUtils.objectsEqual(physicalVolumeId, other.physicalVolumeId)
                && deviceSize == other.deviceSize
                && lunType == other.lunType
                && ObjectUtils.objectsEqual(pathsDictionary, other.pathsDictionary)
                && ObjectUtils.objectsEqual(vendorName, other.vendorName)
                && ObjectUtils.objectsEqual(productId, other.productId)
                && ObjectUtils.objectsEqual(serial, other.serial)
                && ObjectUtils.objectsEqual(vendorId, other.vendorId)
                && ObjectUtils.objectsEqual(volumeGroupId, other.volumeGroupId)
                && ObjectUtils.objectsEqual(status, other.status)
                && ObjectUtils.objectsEqual(diskId, other.diskId)
                && ObjectUtils.objectsEqual(diskAlias, other.diskAlias)
                && ObjectUtils.objectsEqual(storageDomainId, other.storageDomainId)
                && ObjectUtils.objectsEqual(storageDomainName, other.storageDomainName));
    }

    public String getLUN_id() {
        return this.id;
    }

    public void setLUN_id(String value) {
        this.id = value;
    }

    public String getphysical_volume_id() {
        return this.physicalVolumeId;
    }

    public void setphysical_volume_id(String value) {
        this.physicalVolumeId = value;
    }

    public String getvolume_group_id() {
        return this.volumeGroupId;
    }

    public void setvolume_group_id(String value) {
        this.volumeGroupId = value;
    }

    public String getSerial() {
        return this.serial;
    }

    public void setSerial(String value) {
        this.serial = value;
    }

    public Integer getLunMapping() {
        return this.lunMapping;
    }

    public void setLunMapping(Integer value) {
        this.lunMapping = value;
    }

    public String getVendorId() {
        return this.vendorId;
    }

    public void setVendorId(String value) {
        this.vendorId = value;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String value) {
        this.productId = value;
    }

    public ArrayList<StorageServerConnections> getLunConnections() {
        return _lunConnections;
    }

    public void setLunConnections(ArrayList<StorageServerConnections> value) {
        _lunConnections = value;
    }

    public int getDeviceSize() {
        return deviceSize;
    }

    public void setDeviceSize(int value) {
        deviceSize = value;
    }

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

    public HashMap<String, Boolean> getPathsDictionary() {
        return pathsDictionary;
    }

    public void setPathsDictionary(HashMap<String, Boolean> value) {
        pathsDictionary = value;
    }

    public StorageType getLunType() {
        return lunType;
    }

    public void setLunType(StorageType value) {
        lunType = value;
    }

    public LunStatus getStatus() {
        return status;
    }

    public void setStatus(LunStatus value) {
        status = value;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid value) {
        diskId = value;
    }

    public String getDiskAlias() {
        return diskAlias;
    }

    public void setDiskAlias(String value) {
        diskAlias = value;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        storageDomainId = value;
    }

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LUNs [id=")
                .append(getLUN_id())
                .append(", physicalVolumeId=")
                .append(getphysical_volume_id())
                .append(", volumeGroupId=")
                .append(getvolume_group_id())
                .append(", serial=")
                .append(getSerial())
                .append(", lunMapping=")
                .append(getLunMapping())
                .append(", vendorId=")
                .append(getVendorId())
                .append(", productId=")
                .append(getProductId())
                .append(", _lunConnections=")
                .append(getLunConnections())
                .append(", deviceSize=")
                .append(getDeviceSize())
                .append(", vendorName=")
                .append(getVendorName())
                .append(", pathsDictionary=")
                .append(getPathsDictionary())
                .append(", lunType=")
                .append(getLunType())
                .append(", status=")
                .append(getStatus())
                .append(", diskId=")
                .append(getDiskId())
                .append(", diskAlias=")
                .append(getDiskAlias())
                .append(", storageDomainId=")
                .append(getStorageDomainId())
                .append(", storageDomainName=")
                .append(getStorageDomainName())
                .append("]");
        return builder.toString();
    }

    @Override
    public String getId() {
        return getLUN_id();
    }

    @Override
    public void setId(String id) {
        setLUN_id(id);
    }
}
