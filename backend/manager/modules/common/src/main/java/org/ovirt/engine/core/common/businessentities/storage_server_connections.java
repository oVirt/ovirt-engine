package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.StringHelper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "storage_server_connections")
@Entity
@Table(name = "storage_server_connections")
public class storage_server_connections implements Serializable {
    private static final long serialVersionUID = 5444293590307760809L;

    public storage_server_connections() {
    }

    public storage_server_connections(String connection,
            String id,
            String iqn,
            String password,
            StorageType storage_type,
            String user_name,
            String port,
            String portal) {
        this.connection = connection;
        this.id = id;
        this.iqn = iqn;
        this.password = password;
        this.storageType = storage_type;
        this.username = user_name;
        this.port = port;
        this.portal = portal;
    }

    @XmlElement(name = "connection")
    @Column(name = "connection", length = 250, nullable = false)
    private String connection;

    public String getconnection() {
        return this.connection;
    }

    public void setconnection(String value) {
        this.connection = value;
    }

    @XmlElement(name = "id")
    @Id
    @Column(name = "id", length = 50)
    private String id;

    public String getid() {
        return this.id;
    }

    public void setid(String value) {
        this.id = value;
    }

    // TODO this field needs to be made unique in the database
    @XmlElement(name = "iqn")
    @Column(name = "iqn", length = 128)
    private String iqn;

    public String getiqn() {
        return this.iqn;
    }

    public void setiqn(String value) {
        this.iqn = value;
    }

    @XmlElement(name = "port")
    @Column(name = "port", length = 50)
    private String port;

    public String getport() {
        return this.port;
    }

    public void setport(String value) {
        this.port = value;
    }

    @XmlElement(name = "portal")
    @Column(name = "portal", length = 50)
    private String portal;

    public String getportal() {
        return this.portal;
    }

    public void setportal(String value) {
        this.portal = value;
    }

    @XmlElement(name = "password")
    @Column(name = "password", length = 50)
    private String password;

    public String getpassword() {
        return this.password;
    }

    public void setpassword(String value) {
        this.password = value;
    }

    @XmlElement(name = "storage_type")
    @Column(name = "storage_type", nullable = false)
    @Enumerated
    private StorageType storageType = StorageType.forValue(0);

    public StorageType getstorage_type() {
        return this.storageType;
    }

    public void setstorage_type(StorageType value) {
        this.storageType = value;
    }

    @XmlElement(name = "user_name")
    @Column(name = "user_name", length = 50)
    private String username;

    public String getuser_name() {
        return this.username;
    }

    public void setuser_name(String value) {
        this.username = value;
    }

    @Override
    public boolean equals(Object obj) {
        boolean returnValue = super.equals(obj);
        if (!returnValue && obj != null && obj instanceof storage_server_connections) {
            returnValue = (!StringHelper.EqOp(getid(), "") && StringHelper.EqOp(getid(),
                    ((storage_server_connections) obj).getid()));
        }
        return returnValue;
    }

    @Override
    public int hashCode() {
        return getid() != null ? getid().hashCode() : 0;
    }

    public static storage_server_connections copyOf(storage_server_connections ssc) {
        // using the constructor since all fields do not need deep copy (string
        // is immutable,
        // and storage_type is an enum
        return new storage_server_connections(ssc.connection, ssc.id, ssc.iqn, ssc.password,
                ssc.storageType, ssc.username, ssc.port, ssc.portal);
    }

}
