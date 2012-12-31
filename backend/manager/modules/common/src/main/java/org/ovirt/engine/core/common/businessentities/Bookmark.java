package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "bookmarks")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class Bookmark extends IVdcQueryable implements Serializable {
    private static final long serialVersionUID = 8177640907822845847L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "bookmark_id")
    @Type(type = "guid")
    private Guid id;

    @Size(max = BusinessEntitiesDefinitions.BOOKMARK_NAME_SIZE)
    @Column(name = "bookmark_name")
    private String name;

    @Size(min = 1, max = BusinessEntitiesDefinitions.BOOKMARK_VALUE_SIZE)
    @Column(name = "bookmark_value")
    private String value;


    @Transient
    public String getbookmark_name() {
        return name;
    }

    @Transient
    public String getbookmark_value() {
        return value;
    }

    public void setbookmark_value(String value) {
        this.value = value;
    }


    public void setbookmark_name(String name) {
        this.name = name;
    }


    public Bookmark() {
    }

    public Bookmark(String bookmark_name, String bookmark_value) {
        this.name = bookmark_name;
        this.value = bookmark_value;

    }

    public Bookmark(String bookmark_name, String bookmark_value, Guid bookmark_id) {
        this.name = bookmark_name;
        this.value = bookmark_value;
        this.id = bookmark_id;
    }

    @Override
    public int hashCode() {
        int hash = this.name.hashCode() * 7;
        hash += this.value.hashCode() * 7;
        hash += this.id.hashCode() * 7;

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Bookmark that = (Bookmark) obj;
        boolean result = this.id.equals(that.id);
        result &= this.name.equals(that.name);
        result &= this.value.equals(that.value);

        return result;
    }


    public Guid getbookmark_id() {
        return id;
    }

    public void setbookmark_id(Guid id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getbookmark_id();
    }
}
