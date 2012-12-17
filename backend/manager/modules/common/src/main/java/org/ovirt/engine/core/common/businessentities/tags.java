package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringFormat;

@Entity
@Table(name = "tags")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class tags implements Serializable {
    private static final long serialVersionUID = -6566155246916011274L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "tag_id")
    @Type(type = "guid")
    private Guid id;

    @Size(max = BusinessEntitiesDefinitions.TAG_NAME_SIZE)
    @Column(name = "tag_name")
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS_I18N, message = "VALIDATION.TAGS.INVALID_TAG_NAME")
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    @Column(name = "parent_id")
    @Type(type = "guid")
    private NGuid parent;

    @Column(name = "readonly")
    private Boolean readonly;

    @Column(name = "type")
    private TagsType type = TagsType.GeneralTag;

    // TODO this is a hack to get around how the old mappings were done
    // This will be redone in version 3.0 with proper relationship mapping
    @ManyToOne
    @JoinTable(name = "tags_user_group_map", joinColumns = @JoinColumn(name = "tag_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    @Cascade(CascadeType.MERGE)
    private LdapGroup userGroup;

    @ManyToOne
    @JoinTable(name = "tags_user_map", joinColumns = @JoinColumn(name = "tag_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Cascade(CascadeType.MERGE)
    private DbUser user;

    @ManyToOne
    @JoinTable(name = "tags_vds_map", joinColumns = @JoinColumn(name = "tag_id"), inverseJoinColumns = @JoinColumn(
            name = "vds_id"))
    @Cascade(CascadeType.MERGE)
    private VdsStatic vds;

    @ManyToOne
    @JoinTable(name = "tags_vm_map", joinColumns = @JoinColumn(name = "tag_id"), inverseJoinColumns = @JoinColumn(
            name = "vm_id"))
    @Cascade(CascadeType.MERGE)
    private VmStatic vm;

    @ManyToOne
    @JoinTable(name = "tags_vm_pool_map", joinColumns = @JoinColumn(name = "tag_id"), inverseJoinColumns = @JoinColumn(
            name = "vm_pool_id"))
    @Cascade(CascadeType.MERGE)
    private vm_pools vmPool;

    @Transient
    private List<tags> _children;

    public tags() {
        _children = new java.util.ArrayList<tags>();
    }

    public tags(String description, NGuid parent_id, Boolean isReadonly, Guid tag_id, String tag_name) {
        _children = new java.util.ArrayList<tags>();
        this.description = description;
        this.parent = parent_id;
        this.readonly = isReadonly;
        this.id = tag_id;
        this.name = tag_name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_children == null) ? 0 : _children.hashCode());
        result = prime
                * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result
                + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result
                + ((readonly == null) ? 0 : readonly.hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((type == null) ? 0 : type.hashCode());
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
        tags other = (tags) obj;
        if (_children == null) {
            if (other._children != null)
                return false;
        } else if (!_children.equals(other._children))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (readonly == null) {
            if (other.readonly != null)
                return false;
        } else if (!readonly.equals(other.readonly))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
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

    public String getdescription() {
        return this.description;
    }

    public void setdescription(String description) {
        this.description = description;
    }

    public NGuid getparent_id() {
        return this.parent;
    }

    public void setparent_id(NGuid parent) {
        this.parent = parent;
    }

    public Boolean getIsReadonly() {
        return this.readonly;
    }

    public void setIsReadonly(Boolean readOnly) {
        this.readonly = readOnly;
    }

    public Guid gettag_id() {
        return this.id;
    }

    public void settag_id(Guid id) {
        this.id = id;
    }

    public String gettag_name() {
        return this.name;
    }

    public void settag_name(String name) {
        this.name = name;
    }

    public TagsType gettype() {
        return this.type;
    }

    public void settype(TagsType type) {
        this.type = type;
    }

    public List<tags> getChildren() {
        return _children;
    }

    public void setChildren(java.util.List<tags> children) {
        _children = children;
    }

    public StringBuilder GetTagIdAndChildrenIds() {
        StringBuilder builder = new StringBuilder();
        builder.append(StringFormat.format("'%1$s'", gettag_id()));

        for (tags tag : _children) {
            builder.append(StringFormat.format(",%1$s", tag.GetTagIdAndChildrenIds()));
        }
        return builder;
    }

    public StringBuilder GetTagNameAndChildrenNames() {
        StringBuilder builder = new StringBuilder();
        builder.append(StringFormat.format("'%1$s'", gettag_name()));

        for (tags tag : _children) {
            builder.append(StringFormat.format(",%1$s", tag.GetTagNameAndChildrenNames()));
        }
        return builder;
    }

    public void GetTagIdAndChildrenIdsAsList(java.util.HashSet<Guid> tagIds) {
        tagIds.add(gettag_id());
        for (tags tag : _children) {
            tag.GetTagIdAndChildrenIdsAsList(tagIds);
        }
    }

    public void GetTagIdAndChildrenNamesAsList(java.util.HashSet<String> tagIds) {
        tagIds.add(gettag_name());
        for (tags tag : _children) {
            tag.GetTagIdAndChildrenNamesAsList(tagIds);
        }
    }

    public void UpdateTag(tags from) {
        setdescription(from.getdescription());
        settag_name(from.gettag_name());
    }

    public LdapGroup getUserGroup() {
        return userGroup;
    }
}
