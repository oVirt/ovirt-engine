package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.compat.Guid;

public class Bookmark implements Queryable, Serializable {

    private static final long serialVersionUID = 8177640907822845847L;

    private Guid id;

    @Size(max = BusinessEntitiesDefinitions.BOOKMARK_NAME_SIZE)
    private String name;

    @Size(min = 1, max = BusinessEntitiesDefinitions.BOOKMARK_VALUE_SIZE)
    private String value;


    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                value
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Bookmark)) {
            return false;
        }
        Bookmark other = (Bookmark) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(value, other.value);
    }


    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }
}
