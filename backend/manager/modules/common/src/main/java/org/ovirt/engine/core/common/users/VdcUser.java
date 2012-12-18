package org.ovirt.engine.core.common.users;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.compat.Guid;

public class VdcUser implements IVdcUser, Serializable {
    private static final long serialVersionUID = -5689096270467866486L;

    private Guid mUserId = new Guid();
    private String mUserName;
    private String mPassword;
    private String mDomainControler;
    private String mBrowser;
    private String mGroupNames;
    private String groupIds;

    public String getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    private String mFirstName;
    private String mSurName;
    private boolean mIsAdmin = false;

    public VdcUser(LdapUser ldapUser) {
        this(ldapUser, false);
    }

    public VdcUser(LdapUser ldapUser, boolean isAdmin) {
        mUserName = ldapUser.getUserName();
        mUserId = ldapUser.getUserId();
        mPassword = ldapUser.getPassword();
        mDomainControler = ldapUser.getDomainControler();
        mGroupNames = ldapUser.getGroup();
        mFirstName = ldapUser.getName();
        mSurName = ldapUser.getSurName();
        mIsAdmin = isAdmin;
        groupIds = ldapUser.getGroupIds();
    }

    public VdcUser() {
    }

    public VdcUser(Guid userId, String userName, String domain) {
        mUserId = userId;
        mUserName = userName;
        mDomainControler = domain;
    }

    @Override
    public String getUserName() {
        return mUserName;
    }

    @Override
    public void setUserName(String value) {
        mUserName = value;
    }

    @Override
    public String getPassword() {
        return mPassword;
    }

    @Override
    public void setPassword(String value) {
        mPassword = value;
    }

    @Override
    public Guid getUserId() {
        return mUserId;
    }

    @Override
    public void setUserId(Guid value) {
        mUserId = value;
    }

    @Override
    public String getDomainControler() {
        return mDomainControler;
    }

    @Override
    public void setDomainControler(String value) {
        mDomainControler = value;
    }

    @Override
    public String getBrowser() {
        return mBrowser;
    }

    @Override
    public void setBrowser(String value) {
        mBrowser = value;
    }

    @Override
    public String getGroupNames() {
        return mGroupNames;
    }

    @Override
    public void setGroupNames(String value) {
        mGroupNames = value;
    }

    @Override
    public String getFirstName() {
        return mFirstName;
    }

    @Override
    public void setFirstName(String value) {
        mFirstName = value;
    }

    @Override
    public String getSurName() {
        return mSurName;
    }

    @Override
    public void setSurName(String value) {
        mSurName = value;
    }

    @Override
    public boolean isAdmin() {
        return mIsAdmin;
    }

    @Override
    public void setAdmin(boolean isAdmin) {
        this.mIsAdmin = isAdmin;
    }

    /**
     * return the FQDN of a user in a form of user@domain. if the domain is empty then only return the username
     */
    @Override
    public String getFQN() {
        String domain = getDomainControler();
        String username = getUserName();

        // empty username or a user with empty domain will be returned as is
        if ( (username == null || username.length() == 0) || username.contains("@") || (domain == null || domain.length() == 0)) {
            return username;
        } else {
            return username + "@" + domain;
        }
    }
}
