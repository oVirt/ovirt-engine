package org.ovirt.engine.core.common.businessentities.storage;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    private List<StorageServerConnections> _lunConnections;

    private int deviceSize;

    private int pvSize;

    private Long discardMaxSize;

    private Long peCount;

    private Long peAllocatedCount;

    private String vendorName;

    private Map<String, Boolean> pathsDictionary;

    private Map<String, Integer> pathsCapacity;

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
        return Objects.hash(
                id,
                _lunConnections,
                lunMapping,
                physicalVolumeId,
                deviceSize,
                pvSize,
                discardMaxSize,
                lunType,
                pathsDictionary,
                pathsCapacity,
                vendorName,
                productId,
                serial,
                vendorId,
                volumeGroupId,
                status,
                diskId,
                diskAlias,
                storageDomainId,
                storageDomainName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LUNs)) {
            return false;
        }
        LUNs other = (LUNs) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(_lunConnections, other._lunConnections)
                && Objects.equals(lunMapping, other.lunMapping)
                && Objects.equals(physicalVolumeId, other.physicalVolumeId)
                && deviceSize == other.deviceSize
                && pvSize == other.pvSize
                && Objects.equals(discardMaxSize, other.discardMaxSize)
                && lunType == other.lunType
                && Objects.equals(pathsDictionary, other.pathsDictionary)
                && Objects.equals(pathsCapacity, other.pathsCapacity)
                && Objects.equals(vendorName, other.vendorName)
                && Objects.equals(productId, other.productId)
                && Objects.equals(serial, other.serial)
                && Objects.equals(vendorId, other.vendorId)
                && Objects.equals(volumeGroupId, other.volumeGroupId)
                && Objects.equals(status, other.status)
                && Objects.equals(diskId, other.diskId)
                && Objects.equals(diskAlias, other.diskAlias)
                && Objects.equals(storageDomainId, other.storageDomainId)
                && Objects.equals(storageDomainName, other.storageDomainName);
    }

    public String getLUNId() {
        return this.id;
    }

    public void setLUNId(String value) {
        this.id = value;
    }

    public String getPhysicalVolumeId() {
        return this.physicalVolumeId;
    }

    public void setPhysicalVolumeId(String value) {
        this.physicalVolumeId = value;
    }

    public String getVolumeGroupId() {
        return this.volumeGroupId;
    }

    public void setVolumeGroupId(String value) {
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

    public List<StorageServerConnections> getLunConnections() {
        return _lunConnections;
    }

    public void setLunConnections(List<StorageServerConnections> value) {
        _lunConnections = value;
    }

    public int getDeviceSize() {
        return deviceSize;
    }

    public void setDeviceSize(int value) {
        deviceSize = value;
    }

    public int getPvSize() {
        return pvSize;
    }

    public void setPvSize(int value) {
        pvSize = value;
    }

    public Long getPeCount() {
        return peCount;
    }

    public void setPeCount(Long peCount) {
        this.peCount = peCount;
    }

    public Long getPeAllocatedCount() {
        return peAllocatedCount;
    }

    public void setPeAllocatedCount(Long peAllocatedCount) {
        this.peAllocatedCount = peAllocatedCount;
    }

    public Long getDiscardMaxSize() {
        return discardMaxSize;
    }

    public void setDiscardMaxSize(Long discardMaxSize) {
        this.discardMaxSize = discardMaxSize;
    }

    @JsonIgnore
    public boolean supportsDiscard() {
        return getDiscardMaxSize() != null && getDiscardMaxSize() > 0;
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
        return getPathsDictionary() == null ? 0 : getPathsDictionary().size();
    }

    public Map<String, Boolean> getPathsDictionary() {
        return pathsDictionary;
    }

    public void setPathsDictionary(Map<String, Boolean> value) {
        pathsDictionary = value;
    }


    public Map<String, Integer> getPathsCapacity() {
        return pathsCapacity;
    }

    public void setPathsCapacity(Map<String, Integer> value) {
        pathsCapacity = value;
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
        return ToStringBuilder.forInstance(this)
                .append("id", getLUNId())
                .append("physicalVolumeId", getPhysicalVolumeId())
                .append("volumeGroupId", getVolumeGroupId())
                .append("serial", getSerial())
                .append("lunMapping", getLunMapping())
                .append("vendorId", getVendorId())
                .append("productId", getProductId())
                .append("lunConnections", getLunConnections())
                .append("deviceSize", getDeviceSize())
                .append("pvSize", getPvSize())
                .append("peCount", getPeCount())
                .append("peAllocatedCount", getPeAllocatedCount())
                .append("vendorName", getVendorName())
                .append("pathsDictionary", getPathsDictionary())
                .append("pathsCapacity", getPathsCapacity())
                .append("lunType", getLunType())
                .append("status", getStatus())
                .append("diskId", getDiskId())
                .append("diskAlias", getDiskAlias())
                .append("storageDomainId", getStorageDomainId())
                .append("storageDomainName", getStorageDomainName())
                .append("discardMaxSize", getDiscardMaxSize())
                .build();
    }

    @Override
    public String getId() {
        return getLUNId();
    }

    @Override
    public void setId(String id) {
        setLUNId(id);
    }
}
