package org.ovirt.engine.ui.uicommonweb.models.users;

import org.ovirt.engine.core.common.businessentities.AdRefStatus;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

@SuppressWarnings("unused")
public class UserGeneralModel extends EntityModel
{
    public UserGeneralModel()
    {
        setTitle("General");
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
            OnPropertyChanged(new PropertyChangedEventArgs("Domain"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Email"));
        }
    }

    private AdRefStatus status;

    public AdRefStatus getStatus()
    {
        return status;
    }

    public void setStatus(AdRefStatus value)
    {
        if (status != value)
        {
            status = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Status"));
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
