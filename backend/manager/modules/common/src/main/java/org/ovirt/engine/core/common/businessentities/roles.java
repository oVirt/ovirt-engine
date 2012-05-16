package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;

@Entity
@Table(name = "roles")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class roles extends IVdcQueryable implements INotifyPropertyChanged, BusinessEntity<Guid> {
    private static final long serialVersionUID = 1487620954798772886L;
    private static final ArrayList<String> _event_subscriberProperties = new ArrayList<String>(
            Arrays.asList(new String[] { "id", "name", "description", "is_readonly" }));

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "Id")
    @Type(type = "guid")
    private Guid id = new Guid();
    @Column(name = "is_readonly", nullable = false)
    private boolean readOnly;

    @NotNull(message = "VALIDATION.ROLES.NAME.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.ROLE_NAME_SIZE, message = "VALIDATION.ROLES.NAME.MAX", groups = {
            CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "VALIDATION.ROLES.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Column(name = "name", length = BusinessEntitiesDefinitions.ROLE_NAME_SIZE, nullable = false)
    private String name;

    /**
     * MLA addition - distinct admin roles from user roles. Mainly used to prevent user from gaining admin permissions
     */
    @Column(name = "role_type", nullable = false)
    private RoleType type;

    public roles() {
    }

    // TODO add type to the constructor? - depends on future code changes by
    // Omer
    public roles(String description, Guid id, String name) {
        this.description = description;
        this.id = id;
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (readOnly ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        roles other = (roles) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (readOnly != other.readOnly)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return _event_subscriberProperties;
    }

    public String getdescription() {
        return this.description;
    }

    public boolean getis_readonly() {
        return this.readOnly;
    }

    public String getname() {
        return this.name;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public void setdescription(String value) {
        this.description = value;
        OnPropertyChanged(new PropertyChangedEventArgs("description"));
    }

    public void setis_readonly(boolean value) {
        this.readOnly = value;
        OnPropertyChanged(new PropertyChangedEventArgs("is_readonly"));
    }

    public void setname(String value) {
        this.name = value;
        OnPropertyChanged(new PropertyChangedEventArgs("name"));
    }

    public void setType(RoleType type) {
        this.type = type;
    }

    public RoleType getType() {
        return type;
    }

    // TODO -This method and its reference code are being tested with the GWT
    // UI.
    // Remove this only when its surely not needed by them.
    protected void OnPropertyChanged(@SuppressWarnings("unused") PropertyChangedEventArgs e) {
        // purposely empty
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }
}
