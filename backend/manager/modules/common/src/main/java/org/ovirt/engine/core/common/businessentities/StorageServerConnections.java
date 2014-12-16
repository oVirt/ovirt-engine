package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class StorageServerConnections implements Serializable {
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
            Short nfsTimeo) {
        setconnection(connection);
        setid(id);
        setiqn(iqn);
        setpassword(password);
        setstorage_type(storageType);
        setuser_name(userName);
        setport(port);
        setportal(portal);
        setVfsType(vfsType);
        setMountOptions(mountOptions);
        setNfsVersion(nfsVersion);
        setNfsRetrans(nfsRetrans);
        setNfsTimeo(nfsTimeo);
    }

    public StorageServerConnections(String connection,
            String id,
            String iqn,
            String password,
            StorageType storageType,
            String userName,
            String port,
            String portal) {
        this(connection, id, iqn, password, storageType, userName, port, portal, null, null, null, null, null);
    }

    private String connection;

    public String getconnection() {
        return this.connection;
    }

    public void setconnection(String value) {
        this.connection = value;
    }

    private String id;

    public String getid() {
        return this.id;
    }

    public void setid(String value) {
        this.id = value;
    }

    // TODO this field needs to be made unique in the database
    private String iqn;

    public String getiqn() {
        return this.iqn;
    }

    public void setiqn(String value) {
        this.iqn = getStringValueToSet(value);
    }

    private String port;

    public String getport() {
        return this.port;
    }

    public void setport(String value) {
        this.port = getStringValueToSet(value);
    }

    private String portal = DEFAULT_TPGT;

    public String getportal() {
        return this.portal;
    }

    public void setportal(String value) {
        this.portal = getStringValueToSet(value);
    }

    private String password;

    public String getpassword() {
        return this.password;
    }

    public void setpassword(String value) {
        this.password = getStringValueToSet(value);
    }

    private StorageType storageType;

    public StorageType getstorage_type() {
        return this.storageType;
    }

    public void setstorage_type(StorageType value) {
        this.storageType = value;
    }

    private String username;

    public String getuser_name() {
        return this.username;
    }

    public void setuser_name(String value) {
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

    @Override
    public boolean equals(Object obj) {
        boolean returnValue = super.equals(obj);
        if (!returnValue && obj != null && obj instanceof StorageServerConnections) {
            returnValue =
                    (getid() != null && !getid().isEmpty() && getid().equals(((StorageServerConnections) obj).getid()));
        }
        return returnValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getid() == null) ? 0 : getid().hashCode());
        return result;
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
                ssc.nfsTimeo);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ id: ");
        sb.append(this.getid());
        sb.append(", connection: ");
        sb.append(this.getconnection());
        sb.append(", iqn: ");
        sb.append(this.getiqn());
        sb.append(", vfsType: ");
        sb.append(this.getVfsType());
        sb.append(", mountOptions: ");
        sb.append(this.getMountOptions());
        sb.append(", nfsVersion: ");
        sb.append(this.getNfsVersion());
        sb.append(", nfsRetrans: ");
        sb.append(this.getNfsRetrans());
        sb.append(", nfsTimeo: ");
        sb.append(this.getNfsTimeo());
        if (getIface() != null) {
            sb.append(", iface: ");
            sb.append(this.getIface());
        }
        sb.append(" };");
        return sb.toString();
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
