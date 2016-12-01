package org.ovirt.engine.core.aaa;

import java.util.Objects;

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

    public DirectoryUser() {
        super();
        isAdmin = false;
    }

    public DirectoryUser(String directoryName, String namespace, String id, String name, String principal,
            String displayName) {
        super(directoryName, namespace, id, name, displayName);
        isAdmin = false;
        this.principal = principal;
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
        return Objects.hash(
                super.hashCode(),
                firstName,
                lastName,
                title,
                email,
                department
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DirectoryUser)) {
            return false;
        }
        DirectoryUser other = (DirectoryUser) obj;
        return super.equals(obj)
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName)
                && Objects.equals(title, other.title)
                && Objects.equals(email, other.email)
                && Objects.equals(department, other.department);
    }
}
