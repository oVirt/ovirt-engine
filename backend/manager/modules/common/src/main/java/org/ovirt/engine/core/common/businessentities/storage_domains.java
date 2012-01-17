package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "storage_domains")
public class storage_domains extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = -6162192446628804305L;

    public storage_domains() {
        _staticData = new storage_domain_static();
        _dynamicData = new storage_domain_dynamic();
        setStoragePoolIsoMapData(new storage_pool_iso_map());
    }

    public storage_domains(Guid id, String storage, String storage_name, Guid storage_pool_id,
            Integer available_disk_size, Integer used_disk_size, StorageDomainStatus status, String storage_pool_name,
            int storage_pool_type, int storage_type) {
        _staticData = new storage_domain_static();
        _dynamicData = new storage_domain_dynamic();
        setStoragePoolIsoMapData(new storage_pool_iso_map());
        this.setid(id);
        this.setstorage(storage);
        this.setstorage_name(storage_name);
        this.setstorage_pool_id(storage_pool_id);
        this.setavailable_disk_size(available_disk_size);
        this.setused_disk_size(used_disk_size);
        this.setstatus(status);
        this.setstorage_pool_name(storage_pool_name);
        this.setstorage_type(StorageType.forValue(storage_pool_type));
        this.setstorage_domain_type(StorageDomainType.forValue(storage_type));
    }

    private Set<VdcBllErrors> alerts;

    /**
     * @return the alerts
     */
    public Set<VdcBllErrors> getAlerts() {
        return alerts;
    }

    /**
     * @param alerts the alerts to set
     */
    public void setAlerts(Set<VdcBllErrors> alerts) {
        this.alerts = alerts;
    }

    @XmlElement(name = "StoragePoolIsoMapData")
    private storage_pool_iso_map privateStoragePoolIsoMapData;

    public storage_pool_iso_map getStoragePoolIsoMapData() {
        return privateStoragePoolIsoMapData;
    }

    public void setStoragePoolIsoMapData(storage_pool_iso_map value) {
        privateStoragePoolIsoMapData = value;
    }

    @XmlElement(name = "StorageStaticData")
    private storage_domain_static _staticData;

    public storage_domain_static getStorageStaticData() {
        return _staticData;
    }

    public void setStorageStaticData(storage_domain_static value) {
        _staticData = value;
    }

    @XmlElement(name = "StorageDynamicData")
    private storage_domain_dynamic _dynamicData;

    public storage_domain_dynamic getStorageDynamicData() {
        return _dynamicData;
    }

    public void setStorageDynamicData(storage_domain_dynamic value) {
        _dynamicData = value;
    }

    public Guid getid() {
        return this.getStorageStaticData().getId();
    }

    public void setid(Guid value) {
        getStorageStaticData().setId(value);
        getStorageDynamicData().setId(value);
        getStoragePoolIsoMapData().setstorage_id(value);
    }

    public String getstorage() {
        return getStorageStaticData().getstorage();
    }

    public void setstorage(String value) {
        getStorageStaticData().setstorage(value);
    }

    public String getstorage_name() {
        return getStorageStaticData().getstorage_name();
    }

    public void setstorage_name(String value) {
        if (!StringHelper.EqOp(getStorageStaticData().getstorage_name(), value)) {
            getStorageStaticData().setstorage_name(value);
            OnPropertyChanged(new PropertyChangedEventArgs("storage_name"));
        }
    }

    public NGuid getstorage_pool_id() {
        return getStoragePoolIsoMapData().getstorage_pool_id();
    }

    public void setstorage_pool_id(NGuid value) {
        getStoragePoolIsoMapData().setstorage_pool_id(value);
    }

    public Integer getavailable_disk_size() {
        return getStorageDynamicData().getavailable_disk_size();
    }

    public void setavailable_disk_size(Integer value) {
        getStorageDynamicData().setavailable_disk_size(value);
        OnPropertyChanged(new PropertyChangedEventArgs("available_disk_size"));
        UpdateTotalDiskSize();
        UpdateOverCommitPercent();
    }

    private void UpdateOverCommitPercent() {
        setstorage_domain_over_commit_percent(getavailable_disk_size() != null && getavailable_disk_size() > 0 ? getcommitted_disk_size()
                * 100 / getavailable_disk_size()
                : 0);
    }

    private int _storage_domain_over_commit_percent;

    public int getstorage_domain_over_commit_percent() {
        return _storage_domain_over_commit_percent;
    }

    public void setstorage_domain_over_commit_percent(int value) {
        _storage_domain_over_commit_percent = value;
        OnPropertyChanged(new PropertyChangedEventArgs("storage_domain_over_commit_percent"));
    }

    private int _committedDiskSize;

    @XmlElement
    public int getcommitted_disk_size() {
        return _committedDiskSize;
    }

    public void setcommitted_disk_size(int value) {
        _committedDiskSize = value;
        UpdateOverCommitPercent();
    }

    public Integer getused_disk_size() {
        return getStorageDynamicData().getused_disk_size();
    }

    public void setused_disk_size(Integer value) {

        getStorageDynamicData().setused_disk_size(value);
        OnPropertyChanged(new PropertyChangedEventArgs("used_disk_size"));
        UpdateTotalDiskSize();
    }

    private void UpdateTotalDiskSize() {
        Integer available = getStorageDynamicData().getavailable_disk_size();
        Integer used = getStorageDynamicData().getused_disk_size();

        if (available != null && used != null) {
            setTotalDiskSize(available + used);
        } else {
            setTotalDiskSize(0); // GREGM prevents NPEs
        }
    }

    private Integer totalDiskSize = 0; // GREGM prevents NPEs

    public Integer getTotalDiskSize() {
        UpdateTotalDiskSize();
        return totalDiskSize;
    }

    public void setTotalDiskSize(Integer value) {
        value = (value == null) ? 0 : value;
        if (!totalDiskSize.equals(value)) {
            totalDiskSize = value;
            OnPropertyChanged(new PropertyChangedEventArgs("TotalDiskSize"));
        }
    }

    public StorageDomainStatus getstatus() {
        return getStoragePoolIsoMapData().getstatus();
    }

    public void setstatus(StorageDomainStatus value) {
        StorageDomainStatus curStatus = getStoragePoolIsoMapData().getstatus();
        if (curStatus == null || curStatus != value) {
            getStoragePoolIsoMapData().setstatus(value);
            OnPropertyChanged(new PropertyChangedEventArgs("status"));
        }
    }

    public StorageDomainOwnerType getowner() {
        return getStoragePoolIsoMapData().getowner();
    }

    public void setowner(StorageDomainOwnerType value) {
        getStoragePoolIsoMapData().setowner(value);
    }

    @XmlElement(name = "storage_pool_name")
    private String storage_pool_nameField;

    public String getstorage_pool_name() {
        return storage_pool_nameField;
    }

    public void setstorage_pool_name(String value) {
        storage_pool_nameField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("storage_name"));
    }

    @XmlElement
    public StorageType getstorage_type() {
        return getStorageStaticData().getstorage_type();
    }

    public void setstorage_type(StorageType value) {
        getStorageStaticData().setstorage_type(value);
    }

    private StorageDomainSharedStatus _storageDomainSharedStatus = StorageDomainSharedStatus.forValue(0);

    @XmlElement
    public StorageDomainSharedStatus getstorage_domain_shared_status() {
        return _storageDomainSharedStatus;
    }

    public void setstorage_domain_shared_status(StorageDomainSharedStatus value) {
        if (_storageDomainSharedStatus != value) {
            _storageDomainSharedStatus = value;
            OnPropertyChanged(new PropertyChangedEventArgs("storage_domain_shared_status"));
        }
    }

    public StorageDomainType getstorage_domain_type() {
        return getStorageStaticData().getstorage_domain_type();
    }

    public void setstorage_domain_type(StorageDomainType value) {
        getStorageStaticData().setstorage_domain_type(value);
        OnPropertyChanged(new PropertyChangedEventArgs("storage_domain_type"));
    }

    public StorageFormatType getStorageFormat() {
        return getStorageStaticData().getStorageFormat();
    }

    public void setStorageFormat(StorageFormatType value) {
        getStorageStaticData().setStorageFormat(value);
    }

    public Object getQueryableId() {
        return StringFormat.format("%1$s_%2$s", getid().toString(), getstorage_pool_id() != null ? getstorage_pool_id()
                .getValue().toString() : Guid.Empty.toString());
    }

    private static final java.util.ArrayList<String> _spProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "storage_name", "status", "used_disk_size", "available_disk_size",
                    "committed_disk_size", "storage_domain_type", "storage_domain_shared_status", "storage" }));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _spProperties;
    }

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _committedDiskSize;
        result = prime * result
                + ((_dynamicData == null) ? 0 : _dynamicData.hashCode());
        result = prime * result
                + ((_staticData == null) ? 0 : _staticData.hashCode());
        result = prime
                * result
                + ((_storageDomainSharedStatus == null) ? 0
                        : _storageDomainSharedStatus.hashCode());
        result = prime * result + _storage_domain_over_commit_percent;
        result = prime
                * result
                + ((privateStoragePoolIsoMapData == null) ? 0
                        : privateStoragePoolIsoMapData.hashCode());
        result = prime * result
                + ((totalDiskSize == null) ? 0 : totalDiskSize.hashCode());
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
        storage_domains other = (storage_domains) obj;
        if (_committedDiskSize != other._committedDiskSize)
            return false;
        if (_storageDomainSharedStatus != other._storageDomainSharedStatus)
            return false;
        if (_storage_domain_over_commit_percent != other._storage_domain_over_commit_percent)
            return false;
        if (totalDiskSize == null) {
            if (other.totalDiskSize != null)
                return false;
        } else if (!totalDiskSize.equals(other.totalDiskSize))
            return false;
        return true;
    }

}
