package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ad_groups")
@Entity
@Table(name = "ad_groups")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class ad_groups extends DbUserBase implements Serializable {
    private static final long serialVersionUID = 6717840754119287059L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "id")
    @Type(type = "guid")
    private Guid id = new Guid();

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private AdRefStatus status = AdRefStatus.Inactive;

    @Column(name = "domain", length = 100, nullable = false)
    private String domain;

    @Transient
    private List<String> memberOf;

    @Transient
    private String distinguishedName;

    public ad_groups() {
        this.status = AdRefStatus.Active;
        id = Guid.Empty;
        name = "";
        distinguishedName = "";
    }

    public ad_groups(Guid id, String name, int status) {
        this.id = id;
        this.name = name;
        this.status = AdRefStatus.forValue(status);
    }

    @XmlElement
    public Guid getid() {
        return this.id;
    }

    public void setid(Guid value) {
        this.id = value;
    }

    @XmlElement
    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        this.name = value;
    }

    @XmlElement
    public AdRefStatus getstatus() {
        return status;
    }

    public void setstatus(AdRefStatus value) {
        status = value;
    }

    public ad_groups(Guid id, String name) {
        this.id = id;
        this.name = name;
    }

    public ad_groups(Guid id, String name, String domain) {
        this(id, name);
        this.domain = domain;
    }

    public ad_groups(Guid id, String name, String domain, String distinguishedName) {
        this(id, name, domain);
        this.distinguishedName = distinguishedName;
    }

    public ad_groups(Guid id, String name, String domain, String distinguishedName, List<String> memberOf) {
        this(id, name, domain, distinguishedName);
        this.setMemberOf(memberOf);
    }

    /**
     * This constructor used only for Inactive groups
     *
     * @param id
     */
    public ad_groups(Guid id) {
        this.id = id;
        status = AdRefStatus.Inactive;
    }

    @XmlElement
    public String getdomain() {
        return domain;
    }

    public void setdomain(String value) {
        domain = value;
    }

    @Override
    public Object getQueryableId() {
        return getid();
    }

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        throw new NotImplementedException();
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setMemberOf(List<String> memberOf) {
        this.memberOf = memberOf;
    }

    public List<String> getMemberOf() {
        return memberOf;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((distinguishedName == null) ? 0 : distinguishedName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((memberOf == null) ? 0 : memberOf.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        ad_groups other = (ad_groups) obj;
        if (distinguishedName == null) {
            if (other.distinguishedName != null)
                return false;
        } else if (!distinguishedName.equals(other.distinguishedName))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        if (memberOf == null) {
            if (other.memberOf != null)
                return false;
        } else if (!memberOf.equals(other.memberOf))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (status != other.status)
            return false;
        return true;
    }
}
