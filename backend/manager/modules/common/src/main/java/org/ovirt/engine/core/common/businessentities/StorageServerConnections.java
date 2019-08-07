package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnections implements BusinessEntity<String> {
    private static final long serialVersionUID = 5444293590307760809L;

    public static final String DEFAULT_TPGT = "1";

    public StorageServerConnections() {
        storageType = StorageType.UNKNOWN;
    }

    public StorageServerConnections(String connection,
            String id,
            String iqn,
            String password,
            StorageType storageType,
            String userName,
            String port,
            String portal,
            String vfsType,
            String mountOptions,
            NfsVersion nfsVersion,
            Short nfsRetrans,
            Short nfsTimeo,
            Guid glusterVolumeId) {
        setConnection(connection);
        setId(id);
        setIqn(iqn);
        setPassword(password);
        setStorageType(storageType);
        setUserName(userName);
        setPort(port);
        setPortal(portal);
        setVfsType(vfsType);
        setMountOptions(mountOptions);
        setNfsVersion(nfsVersion);
        setNfsRetrans(nfsRetrans);
        setNfsTimeo(nfsTimeo);
        setGlusterVolumeId(glusterVolumeId);
    }

    public StorageServerConnections(String connection,
            String id,
            String iqn,
            String password,
            StorageType storageType,
            String userName,
            String port,
            String portal) {
        this(connection, id, iqn, password, storageType, userName, port, portal, null, null, null, null, null, null);
    }

    private String connection;

    public String getConnection() {
        return this.connection;
    }

    public void setConnection(String value) {
        this.connection = value;
    }

    private String id;

    public String getId() {
        return this.id;
    }

    public void setId(String value) {
        this.id = value;
    }

    // TODO this field needs to be made unique in the database
    private String iqn;

    public String getIqn() {
        return this.iqn;
    }

    public void setIqn(String value) {
        this.iqn = getStringValueToSet(value);
    }

    private String port;

    public String getPort() {
        return this.port;
    }

    public void setPort(String value) {
        this.port = getStringValueToSet(value);
    }

    private String portal = DEFAULT_TPGT;

    public String getPortal() {
        return this.portal;
    }

    public void setPortal(String value) {
        this.portal = getStringValueToSet(value);
    }

    private String password;

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String value) {
        this.password = getStringValueToSet(value);
    }

    private StorageType storageType;

    public StorageType getStorageType() {
        return this.storageType;
    }

    public void setStorageType(StorageType value) {
        this.storageType = value;
    }

    private String username;

    public String getUserName() {
        return this.username;
    }

    public void setUserName(String value) {
        this.username = getStringValueToSet(value);
    }

    private String mountOptions;

    public void setMountOptions(String mountOptions) {
        this.mountOptions = mountOptions;
    }

    public String getMountOptions() {
        return mountOptions;
    }

    private String vfsType;

    public void setVfsType(String vfsType) {
        this.vfsType = vfsType;
    }

    public String getVfsType() {
        return vfsType;
    }

    private NfsVersion nfsVersion;

    public NfsVersion getNfsVersion() {
        return nfsVersion;
    }

    public void setNfsVersion(NfsVersion nfsVersion) {
        this.nfsVersion = nfsVersion;
    }

    @Min(value = 1, message = "VALIDATION_STORAGE_CONNECTION_NFS_TIMEO")
    @Max(value = 6000, message = "VALIDATION_STORAGE_CONNECTION_NFS_TIMEO")
    private Short nfsTimeo;

    public Short getNfsTimeo() {
        return nfsTimeo;
    }

    public void setNfsTimeo(Short nfsTimeo) {
        this.nfsTimeo = nfsTimeo;
    }

    @Min(value = 0, message = "VALIDATION_STORAGE_CONNECTION_NFS_RETRANS")
    @Max(value = Short.MAX_VALUE, message = "VALIDATION_STORAGE_CONNECTION_NFS_RETRANS")
    private Short nfsRetrans;

    private String iface;

    public String getIface() {
        return iface;
    }

    public void setIface(String iface) {
        this.iface = iface;
    }

    private String netIfaceName;

    public String getNetIfaceName() {
        return netIfaceName;
    }

    public void setNetIfaceName(String netIfaceName) {
        this.netIfaceName = netIfaceName;
    }

    private Guid glusterVolumeId;

    public Guid getGlusterVolumeId() {
        return glusterVolumeId;
    }

    public void setGlusterVolumeId(Guid glusterVolumeId) {
        this.glusterVolumeId = glusterVolumeId;
    }

    @Override
    public boolean equals(Object obj) {
        boolean returnValue = super.equals(obj);
        if (!returnValue && obj != null && obj instanceof StorageServerConnections) {
            returnValue =
                    getId() != null && !getId().isEmpty() && getId().equals(((StorageServerConnections) obj).getId());
        }
        return returnValue;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public static StorageServerConnections copyOf(StorageServerConnections ssc) {
        // using the constructor since all fields do not need deep copy (string
        // is immutable, and storageType and nfsVersion are enums
        return new StorageServerConnections(ssc.connection,
                ssc.id,
                ssc.iqn,
                ssc.password,
                ssc.storageType,
                ssc.username,
                ssc.port,
                ssc.portal,
                ssc.vfsType,
                ssc.mountOptions,
                ssc.nfsVersion,
                ssc.nfsRetrans,
                ssc.nfsTimeo,
                ssc.glusterVolumeId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("connection", getConnection())
                .append("iqn", getIqn())
                .append("vfsType", getVfsType())
                .append("mountOptions", getMountOptions())
                .append("nfsVersion", getNfsVersion())
                .append("nfsRetrans", getNfsRetrans())
                .append("nfsTimeo", getNfsTimeo())
                .append("iface", getIface())
                .append("netIfaceName", getNetIfaceName())
                .build();
    }

    public Short getNfsRetrans() {
        return nfsRetrans;
    }

    public void setNfsRetrans(Short nfsRetrans) {
        this.nfsRetrans = nfsRetrans;
    }

    private static String getStringValueToSet(String value) {
        if (value != null && value.isEmpty()) {
            return null;
        }

        return value;
    }
}
