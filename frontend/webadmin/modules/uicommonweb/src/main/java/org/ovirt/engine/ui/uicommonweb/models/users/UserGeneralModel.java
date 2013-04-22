package org.ovirt.engine.ui.uicommonweb.models.users;

import org.ovirt.engine.core.common.businessentities.LdapRefStatus;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class UserGeneralModel extends EntityModel
{
    public UserGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    private String domain;

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String value)
    {
        if (!StringHelper.stringsEqual(domain, value))
        {
            domain = value;
            onPropertyChanged(new PropertyChangedEventArgs("Domain")); //$NON-NLS-1$
        }
    }

    private String email;

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String value)
    {
        if (!StringHelper.stringsEqual(email, value))
        {
            email = value;
            onPropertyChanged(new PropertyChangedEventArgs("Email")); //$NON-NLS-1$
        }
    }

    private LdapRefStatus status;

    public LdapRefStatus getStatus()
    {
        return status;
    }

    public void setStatus(LdapRefStatus value)
    {
        if (status != value)
        {
            status = value;
            onPropertyChanged(new PropertyChangedEventArgs("Status")); //$NON-NLS-1$
        }
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            UpdateProperties();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        UpdateProperties();
    }

    private void UpdateProperties()
    {
        DbUser user = (DbUser) getEntity();

        setDomain(user.getdomain());
        setEmail(user.getemail());
        setStatus(user.getAdStatus());

    }
}
