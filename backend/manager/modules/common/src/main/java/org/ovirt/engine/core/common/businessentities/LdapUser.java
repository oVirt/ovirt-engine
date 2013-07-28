package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class LdapUser extends IVdcQueryable implements Serializable {
    // TODO - LocalAdministrative permissions ??? up to Miki to decide what it
    // is
    // TODO - Note ??? (Miki)
    // TODO - DesktopDevice ?? (Miki)

    private static final long serialVersionUID = 6800096193162766377L;
    private Guid mUserId = Guid.Empty;
    private String mUserName;
    private String mPassword;
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
        mUserId = Guid.Empty;
        mGroups = new HashMap<String, LdapGroup>();
    }

    public LdapUser(DbUser dbUser) {
        setUserId(dbUser.getId());
        setUserName(dbUser.getLoginName());
        setName(dbUser.getFirstName());
        setSurName(dbUser.getLastName());
        setDepartment(dbUser.getDepartment());
        setDomainControler(dbUser.getDomain());
        setEmail(dbUser.getEmail());
        mGroups = new HashMap<String, LdapGroup>();
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String value) {
        mUserName = value;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String value) {
        mPassword = value;
    }

    public Guid getUserId() {
        return mUserId;
    }

    public void setUserId(Guid value) {
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

    @Override
    public Object getQueryableId() {
        return getUserId();
    }

    public String getGroupIds() {
        String groupIds = "";
        if (!mGroups.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Iterator<LdapGroup> iterator = mGroups.values().iterator(); iterator.hasNext();) {
                sb.append(iterator.next().getid().toString());
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            groupIds = sb.toString();
        }
        return groupIds;
    }

}
