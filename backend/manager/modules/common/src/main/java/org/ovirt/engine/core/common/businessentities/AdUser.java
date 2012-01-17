package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AdUser")
public class AdUser extends IVdcQueryable implements Serializable {
    // TODO - LocalAdministrative permissions ??? up to Miki to decide what it
    // is
    // TODO - Note ??? (Miki)
    // TODO - DesktopDevice ?? (Miki)

    private static final long serialVersionUID = 6800096193162766377L;
    private Guid mUserId = new Guid();
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

    private Map<String, ad_groups> mGroups;

    public AdUser() {
        mUserId = Guid.Empty;
        mGroups = new java.util.HashMap<String, ad_groups>();
    }

    public AdUser(DbUser dbUser) {
        setUserId(dbUser.getuser_id());
        setUserName(dbUser.getusername());
        setName(dbUser.getname());
        setSurName(dbUser.getsurname());
        setDepartment(dbUser.getdepartment());
        setDomainControler(dbUser.getdomain());
        setEmail(dbUser.getemail());
        mGroups = new java.util.HashMap<String, ad_groups>();
    }
    public AdUser(String userName, String password, Guid userId, String domainControler) {
        mUserName = userName;
        mPassword = password;
        mUserId = userId;
        mDomainControler = domainControler;
        mGroups = new java.util.HashMap<String, ad_groups>();
    }

    @XmlElement(name = "UserName")
    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String value) {
        mUserName = value;
    }

    @XmlElement(name = "Password")
    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String value) {
        mPassword = value;
    }

    @XmlElement(name = "UserId")
    public Guid getUserId() {
        return mUserId;
    }

    public void setUserId(Guid value) {
        mUserId = value;
    }

    @XmlElement(name = "DomainControler")
    public String getDomainControler() {
        return mDomainControler;
    }

    public void setDomainControler(String value) {
        mDomainControler = value;
    }

    @XmlElement(name = "Name")
    public String getName() {
        return mName;
    }

    public void setName(String value) {
        mName = value;
    }

    @XmlElement(name = "SurName")
    public String getSurName() {
        return mSurName;
    }

    public void setSurName(String value) {
        mSurName = value;
    }

    @XmlElement(name = "Department")
    public String getDepartment() {
        return mDepartment;
    }

    public void setDepartment(String value) {
        mDepartment = value;
    }

    @XmlElement(name = "Title")
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String value) {
        mTitle = value;
    }

    @XmlElement(name = "Email")
    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String value) {
        mEmail = value;
    }

    @XmlElement(name = "PasswordExpired")
    public boolean getPasswordExpired() {
        return _passwordExpired;
    }

    public void setPasswordExpired(boolean value) {
        _passwordExpired = value;
    }

    public java.util.Map<String, ad_groups> getGroups() {
        return mGroups;
    }

    public void setGroups(java.util.HashMap<String, ad_groups> value) {
        mGroups = value;
    }

    @XmlElement(name = "Group")
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

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return null;
    }

    public String getGroupIds() {
        String groupIds = "";
        if (!mGroups.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Iterator<ad_groups> iterator = mGroups.values().iterator(); iterator.hasNext();) {
                sb.append(iterator.next().getid().toString());
                if (iterator.hasNext()) { sb.append(","); }
            }
            groupIds = sb.toString();
        }
        return groupIds;
    }

}
