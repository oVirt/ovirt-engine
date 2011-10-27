package org.ovirt.engine.core.common.users;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.interfaces.*;

//using VdcDAL.AdBroker;
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdcUser", namespace = "http://service.engine.ovirt.org")
public class VdcUser implements IVdcUser, Serializable {
    private static final long serialVersionUID = -5689096270467866486L;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "UserId")
    private Guid mUserId = new Guid();
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "UserName")
    private String mUserName;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Password")
    private String mPassword;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "DomainControler")
    private String mDomainControler;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Browser")
    private String mBrowser;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // @XmlElement(name="GroupNames")
    private String mGroupNames;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "FirstName")
    private String mFirstName;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "SurName")
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

    public String getBrowser() {
        return mBrowser;
    }

    public void setBrowser(String value) {
        mBrowser = value;
    }

    public String getGroupNames() {
        return mGroupNames;
    }

    public void setGroupNames(String value) {
        mGroupNames = value;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String value) {
        mFirstName = value;
    }

    public String getSurName() {
        return mSurName;
    }

    public void setSurName(String value) {
        mSurName = value;
    }

}
