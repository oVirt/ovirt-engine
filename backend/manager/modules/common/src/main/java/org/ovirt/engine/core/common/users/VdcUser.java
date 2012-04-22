package org.ovirt.engine.core.common.users;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Serializable;

public class VdcUser implements IVdcUser, Serializable {
    private static final long serialVersionUID = -5689096270467866486L;

    private Guid mUserId = new Guid();
    private String mUserName;
    private String mPassword;
    private String mDomainControler;
    private String mBrowser;
    private String mGroupNames;
    private String mFirstName;
    private String mSurName;

    public VdcUser(AdUser adUser) {
        mUserName = adUser.getUserName();
        mUserId = adUser.getUserId();
        mPassword = adUser.getPassword();
        mDomainControler = adUser.getDomainControler();
        mGroupNames = adUser.getGroup();
        mFirstName = adUser.getName();
        mSurName = adUser.getSurName();
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

}
