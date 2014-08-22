package org.ovirt.engine.core.aaa;

import org.ovirt.engine.core.common.utils.ObjectUtils;

public class DirectoryUser extends DirectoryEntry {
    private static final long serialVersionUID = -5689096270467866486L;

    // The attributes of the user, as extracted from the underlying directory:
    private String firstName;
    private String lastName;
    private String title;
    private String email;
    private String department;
    private String principal;

    // Flag indicating if this user has the administrator role:
    private boolean isAdmin;

    public DirectoryUser(String directoryName, String namespace, String id, String name) {
        super(directoryName, namespace, id, name);
        isAdmin = false;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (firstName == null? 0: firstName.hashCode());
        result = prime * result + (lastName == null? 0: lastName.hashCode());
        result = prime * result + (title == null? 0: title.hashCode());
        result = prime * result + (email == null? 0: email.hashCode());
        result = prime * result + (department == null? 0: department.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        DirectoryUser other = (DirectoryUser) obj;
        return
            ObjectUtils.objectsEqual(firstName, other.firstName) &&
            ObjectUtils.objectsEqual(lastName, other.lastName) &&
            ObjectUtils.objectsEqual(title, other.title) &&
            ObjectUtils.objectsEqual(email, other.email) &&
            ObjectUtils.objectsEqual(department, other.department);
    }
}
