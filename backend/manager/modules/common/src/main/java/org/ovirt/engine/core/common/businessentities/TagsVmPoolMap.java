package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

@Entity
@Table(name = "tags_vm_pool_map")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class TagsVmPoolMap implements Serializable {
    private static final long serialVersionUID = 1110697686039279639L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "tagId", column = @Column(name = "tag_id")),
            @AttributeOverride(name = "vmPoolId", column = @Column(name = "vm_pool_id")) })
    private TagsVmPoolMapId id = new TagsVmPoolMapId();

    @Column(name = "_create_date")
    private Date created = new Date();

    public TagsVmPoolMap() {
    }

    public TagsVmPoolMap(Guid tag_id, NGuid vm_pool_id) {
        this.id.tagId = tag_id;
        this.id.vmPoolId = vm_pool_id;
    }

    public Guid gettag_id() {
        return this.id.tagId;
    }

    public void settag_id(Guid value) {
        this.id.tagId = value;
    }

    public NGuid getvm_pool_id() {
        return this.id.vmPoolId;
    }

    public void setvm_pool_id(Guid value) {
        this.id.vmPoolId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((created == null) ? 0 : created.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        TagsVmPoolMap other = (TagsVmPoolMap) obj;
        if (created == null) {
            if (other.created != null)
                return false;
        } else if (!created.equals(other.created))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
