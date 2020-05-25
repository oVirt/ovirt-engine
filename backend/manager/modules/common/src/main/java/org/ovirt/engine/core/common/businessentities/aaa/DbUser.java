package org.ovirt.engine.core.common.businessentities.aaa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

public class DbUser implements Queryable, Nameable {
    private static final long serialVersionUID = 7052102138405696755L;

    /**
     * This is the identifier assigned by the engine to this user for internal
     * use only.
     */
    private Guid id;

    /**
     * This is the identifier assigned by the external directory to this user.
     */
    private String externalId;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_DOMAIN_SIZE)
    private String domain;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_NAMESPACE_SIZE)
    private String namespace;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_LOGIN_NAME_SIZE)
    private String loginName;

    @Size(max = BusinessEntitiesDefinitions.USER_FIRST_NAME_SIZE)
    @NotNull
    private String firstName;

    @Size(max = BusinessEntitiesDefinitions.USER_LAST_NAME_SIZE)
    @NotNull
    private String lastName;

    @Size(max = BusinessEntitiesDefinitions.USER_DEPARTMENT_SIZE)
    @NotNull
    private String department;

    @Size(max = BusinessEntitiesDefinitions.USER_EMAIL_SIZE)
    private String email;

    @Size(max = BusinessEntitiesDefinitions.USER_NOTE_SIZE)
    private String note;

    /**
     * Expect (key,value) pairs where
     * value is a string containing valid JSON node
     */
    private Map<String, String> userOptions;

    /**
     * GUI flag only. Do not use for internal logic. The sole purpose of
     * calculating this field is for the GUI user to understand who is admin in
     * a snap on the user grid.
     */
    private boolean isAdmin;

    private Collection<String> groupNames;

    /**
     * Comma delimited list of group identifiers.
     */
    private Collection<Guid> groupIds;

    public DbUser() {
        this((DbUser)null);
    }

    public DbUser(DirectoryUser directoryUser) {
        externalId = directoryUser.getId();
        domain = directoryUser.getDirectoryName();
        namespace = directoryUser.getNamespace();
        loginName = directoryUser.getPrincipal() != null ? directoryUser.getPrincipal() : directoryUser.getName();
        this.setFirstName(directoryUser.getFirstName());
        this.setLastName(lastName = directoryUser.getLastName());
        this.setDepartment(directoryUser.getDepartment());
        email = directoryUser.getEmail();
        note = "";
        groupNames = new HashSet<>();
        for (DirectoryGroup directoryGroup : directoryUser.getGroups()) {
            groupNames.add(directoryGroup.getName());
        }
        userOptions = Collections.emptyMap();
    }

    public DbUser(DbUser dbUser) {
        if (dbUser == null) {
            loginName = "";
            firstName = "";
            lastName = "";
            department = "";
            groupNames = Collections.emptyList();
            groupIds = Collections.emptyList();
            note = "";
            userOptions = Collections.emptyMap();
        } else {
            id = dbUser.getId();
            externalId = dbUser.getExternalId();
            domain = dbUser.getDomain();
            namespace = dbUser.getNamespace();
            loginName = dbUser.getLoginName();
            setFirstName(firstName = dbUser.getFirstName());
            setLastName(lastName = dbUser.getLastName());
            setDepartment(dbUser.getDepartment());
            email = dbUser.getEmail();
            note = dbUser.getNote();
            groupIds = new ArrayList<>(dbUser.getGroupIds());
            groupNames = new ArrayList<>(dbUser.getGroupNames());
            isAdmin = dbUser.isAdmin();
            setUserOptions(dbUser.getUserOptions());
        }
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String value) {
        namespace = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        domain = value;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String value) {
        loginName = value;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String value) {
        firstName = value == null ? "" : value;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String value) {
        lastName = value == null ? "" : value;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String value) {
        department = value == null ? "" : value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        email = value;
    }

    public Collection<String> getGroupNames() {
        return new ArrayList<>(groupNames);
    }

    public void setGroupNames(Collection<String> value) {
        groupNames = new HashSet<>(value);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String value) {
        note = value;
    }

    public boolean isGroup() {
        return loginName == null || loginName.trim().isEmpty();
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public void setAdmin(boolean value) {
        isAdmin = value;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setGroupIds(Collection<Guid> groupIds) {
        this.groupIds = new HashSet<>(groupIds);
    }

    public Map<String, String> getUserOptions() {
        return Optional.ofNullable(userOptions)
                .<Map<String, String>> map(HashMap::new)
                .orElse(Collections.emptyMap());
    }

    public void setUserOptions(Map<String, String> userOptions) {
        this.userOptions = Optional.ofNullable(userOptions)
                .<Map<String, String>> map(HashMap::new)
                .orElse(Collections.emptyMap());
    }

    public Collection<Guid> getGroupIds() {
        if (groupIds == null) {
            groupIds = Collections.emptyList();
        }
        return new ArrayList<>(groupIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                externalId,
                department,
                domain,
                namespace,
                email,
                isAdmin,
                firstName,
                note,
                lastName,
                loginName,
                userOptions
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DbUser)) {
            return false;
        }
        DbUser other = (DbUser) obj;
        return Objects.equals(externalId, other.externalId)
                && Objects.equals(department, other.department)
                && Objects.equals(domain, other.domain)
                && Objects.equals(namespace, other.namespace)
                && Objects.equals(email, other.email)
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(note, other.note)
                && Objects.equals(lastName, other.lastName)
                && Objects.equals(loginName, other.loginName)
                && Objects.equals(isAdmin, other.isAdmin)
                && Objects.equals(userOptions, other.userOptions);

    }

    @Override
    public String getName() {
        String loginName = getLoginName();
        if (loginName == null || loginName.isEmpty()) {
            return getFirstName();
        }
        return loginName;
    }

}
