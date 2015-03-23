package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class HostDevice implements BusinessEntity<HostDeviceId> {

    private Guid hostId;
    private String deviceName;
    private String parentDeviceName;
    private String capability;
    private Integer iommuGroup;
    private String productName;
    private String productId;
    private String vendorName;
    private String vendorId;
    private String parentPhysicalFunction;
    private Integer totalVirtualFunctions;
    private String networkInterfaceName;
    private Guid vmId;

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setCapability(String capability) {
        this.capability = capability;
    }

    public String getCapability() {
        return capability;
    }

    public void setIommuGroup(Integer iommuGroup) {
        this.iommuGroup = iommuGroup;
    }

    public Integer getIommuGroup() {
        return iommuGroup;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setParentDeviceName(String parentDeviceName) {
        this.parentDeviceName = parentDeviceName;
    }

    public String getParentDeviceName() {
        return parentDeviceName;
    }

    public String getParentPhysicalFunction() {
        return parentPhysicalFunction;
    }

    public void setParentPhysicalFunction(String parentPhysicalFunction) {
        this.parentPhysicalFunction = parentPhysicalFunction;
    }

    public String getNetworkInterfaceName() {
        return networkInterfaceName;
    }

    public void setNetworkInterfaceName(String networkInterfaceName) {
        this.networkInterfaceName = networkInterfaceName;
    }

    public Integer getTotalVirtualFunctions() {
        return totalVirtualFunctions;
    }

    public void setTotalVirtualFunctions(Integer totalVirtualFunctions) {
        this.totalVirtualFunctions = totalVirtualFunctions;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public Guid getVmId() {
        return vmId;
    }

    @Override
    public HostDeviceId getId() {
        return new HostDeviceId(hostId, deviceName);
    }

    @Override
    public void setId(HostDeviceId id) {
        setHostId(id.getHostId());
        setDeviceName(id.getDeviceName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HostDevice other = (HostDevice) obj;
        return ObjectUtils.objectsEqual(getId(), other.getId()) &&
                ObjectUtils.objectsEqual(parentDeviceName, other.parentDeviceName) &&
                ObjectUtils.objectsEqual(capability, other.capability) &&
                ObjectUtils.objectsEqual(iommuGroup, other.iommuGroup) &&
                ObjectUtils.objectsEqual(productName, other.productName) &&
                ObjectUtils.objectsEqual(productId, other.productId) &&
                ObjectUtils.objectsEqual(vendorName, other.vendorName) &&
                ObjectUtils.objectsEqual(vendorId, other.vendorId) &&
                ObjectUtils.objectsEqual(parentPhysicalFunction, other.parentPhysicalFunction) &&
                ObjectUtils.objectsEqual(totalVirtualFunctions, other.totalVirtualFunctions) &&
                ObjectUtils.objectsEqual(networkInterfaceName, other.networkInterfaceName) &&
                ObjectUtils.objectsEqual(vmId, other.vmId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getId().hashCode();
        result = prime * result + (parentDeviceName == null ? 0 : parentDeviceName.hashCode());
        result = prime * result + (capability == null ? 0 : capability.hashCode());
        result = prime * result + (iommuGroup == null ? 0 : iommuGroup.hashCode());
        result = prime * result + (productName == null ? 0 : productName.hashCode());
        result = prime * result + (productId == null ? 0 : productId.hashCode());
        result = prime * result + (vendorName == null ? 0 : vendorName.hashCode());
        result = prime * result + (vendorId == null ? 0 : vendorId.hashCode());
        result = prime * result + (parentPhysicalFunction == null ? 0 : parentPhysicalFunction.hashCode());
        result = prime * result + (totalVirtualFunctions == null ? 0 : totalVirtualFunctions.hashCode());
        result = prime * result + (networkInterfaceName == null ? 0 : networkInterfaceName.hashCode());
        result = prime * result + (vmId == null ? 0 :  vmId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format("HostDevice{hostId=%s, deviceName='%s', parentDeviceName='%s', capability='%s', iommuGroup=%d, "
                +
                "productName='%s', productId='%s', vendorName='%s', vendorId='%s', parentPhysicalFunction='%s', totalVirtualFunctions=%s, networkInterfaceName='%s', vmId=%s}",
                hostId,
                deviceName,
                parentDeviceName,
                capability,
                iommuGroup,
                productName,
                productId,
                vendorName,
                vendorId,
                parentPhysicalFunction,
                totalVirtualFunctions,
                networkInterfaceName,
                vmId);
    }
}
