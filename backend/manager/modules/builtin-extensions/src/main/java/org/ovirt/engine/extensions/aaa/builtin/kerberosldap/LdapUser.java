package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.StringHelper;

public class LdapUser extends IVdcQueryable implements Serializable {
    // TODO - LocalAdministrative permissions ??? up to Miki to decide what it
    // is
    // TODO - Note ??? (Miki)
    // TODO - DesktopDevice ?? (Miki)

    private static final long serialVersionUID = 6800096193162766377L;
    private String mNamespace;
    private String mUserId;
    private String mUserName;
    private String mDomainControler;
    private String mName;
    private String mSurName;
    private String mDepartment;
    private String mTitle;
    private String mEmail;
    private boolean _passwordExpired;
    private List<String> memberof;

    private Map<String, LdapGroup> mGroups;

    public LdapUser() {
        mGroups = new HashMap<String, LdapGroup>();
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
        mGroups = new HashMap<String, LdapGroup>();
    }

    public String getNamespace() {
        return mNamespace;
    }

    public void setNamespace(String value) {
        mNamespace = value;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String value) {
        mUserName = value;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String value) {
        mUserId = value;
    }

    public String getDomainControler() {
        return mDomainControler;
    }

    public void setDomainControler(String value) {
        mDomainControler = value;
    }

    public String getName() {
        return mName;
    }

    public void setName(String value) {
        mName = value;
    }

    public String getSurName() {
        return mSurName;
    }

    public void setSurName(String value) {
        mSurName = value;
    }

    public String getDepartment() {
        return mDepartment;
    }

    public void setDepartment(String value) {
        mDepartment = value;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String value) {
        mTitle = value;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String value) {
        mEmail = value;
    }

    public boolean getPasswordExpired() {
        return _passwordExpired;
    }

    public void setPasswordExpired(boolean value) {
        _passwordExpired = value;
    }

    public Map<String, LdapGroup> getGroups() {
        return mGroups;
    }

    public void setGroups(HashMap<String, LdapGroup> value) {
        mGroups = value;
    }

    public String getGroup() {
        String[] gArr = mGroups.keySet().toArray(new String[mGroups.size()]);
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
        for (LdapGroup group : mGroups.values()) {
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
