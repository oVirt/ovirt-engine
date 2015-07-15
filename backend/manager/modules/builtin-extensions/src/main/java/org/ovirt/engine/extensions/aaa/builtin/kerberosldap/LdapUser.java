package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.StringHelper;

public class LdapUser implements IVdcQueryable {
    // TODO - LocalAdministrative permissions ??? up to Miki to decide what it
    // is
    // TODO - Note ??? (Miki)
    // TODO - DesktopDevice ?? (Miki)

    private static final long serialVersionUID = 6800096193162766377L;
    private String namespace;
    private String userId;
    private String userName;
    private String domainControler;
    private String name;
    private String surName;
    private String department;
    private String title;
    private String email;
    private boolean passwordExpired;
    private List<String> memberof;

    private Map<String, LdapGroup> groups;

    public LdapUser() {
        groups = new HashMap<String, LdapGroup>();
    }

    public LdapUser(DbUser dbUser) {
        setUserId(dbUser.getExternalId());
        setNamespace(dbUser.getNamespace());
        setUserName(dbUser.getLoginName());
        setName(dbUser.getFirstName());
        setSurName(dbUser.getLastName());
        setDepartment(dbUser.getDepartment());
        setDomainControler(dbUser.getDomain());
        setEmail(dbUser.getEmail());
        groups = new HashMap<String, LdapGroup>();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String value) {
        namespace = value;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String value) {
        userName = value;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String value) {
        userId = value;
    }

    public String getDomainControler() {
        return domainControler;
    }

    public void setDomainControler(String value) {
        domainControler = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String value) {
        surName = value;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String value) {
        department = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        email = value;
    }

    public boolean getPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean value) {
        passwordExpired = value;
    }

    public Map<String, LdapGroup> getGroups() {
        return groups;
    }

    public void setGroups(HashMap<String, LdapGroup> value) {
        groups = value;
    }

    public String getGroup() {
        String[] gArr = groups.keySet().toArray(new String[groups.size()]);
        return StringHelper.join(",", gArr);
    }

    public List<String> getMemberof() {
        return memberof;
    }

    public void setMemberof(List<String> memberof) {
        this.memberof = memberof;
    }

    public String getGroupIds() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (LdapGroup group : groups.values()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(group.getid());
        }
        return sb.toString();
    }

    @Override
    public Object getQueryableId() {
        return getUserId();
    }

}
