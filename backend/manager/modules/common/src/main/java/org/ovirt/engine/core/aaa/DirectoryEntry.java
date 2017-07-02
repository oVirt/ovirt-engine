package org.ovirt.engine.core.aaa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Queryable;

public class DirectoryEntry implements Queryable {
    private static final long serialVersionUID = -5689096270467866486L;

    /**
     * Reference to the directory where this entry was originated.
     */
    private String directoryName;

    // The values of the attributes:
    private String namespace;
    private String id;
    private String name;
    private String displayName;
    private DirectoryEntryStatus status;

    /**
     * The list of groups this entry belongs to.
     */
    private List<DirectoryGroup> groups;

    public DirectoryEntry() {
        this.status = DirectoryEntryStatus.UNAVAILABLE;
        this.groups = new ArrayList<>(1);
    }

    public DirectoryEntry(String directoryName, String namespace, String id, String name, String displayName) {
        this();
        this.directoryName = directoryName;
        this.namespace = namespace;
        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DirectoryEntryStatus getStatus() {
        return status;
    }

    public void setStatus(DirectoryEntryStatus status) {
        this.status = status;
    }


    public List<DirectoryGroup> getGroups() {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        return groups;
    }

    public void setGroups(List<DirectoryGroup> groups) {
        this.groups = groups;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                status,
                directoryName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DirectoryEntry)) {
            return false;
        }
        DirectoryEntry other = (DirectoryEntry) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(status, other.status)
                && Objects.equals(directoryName, other.directoryName);
    }

    public String toString() {
        return name + "@" + directoryName;
    }
}
