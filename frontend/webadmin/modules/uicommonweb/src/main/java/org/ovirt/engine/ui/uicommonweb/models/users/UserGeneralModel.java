package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class UserGeneralModel extends EntityModel<DbUser> {
    public UserGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$
    }

    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        if (!Objects.equals(domain, value)) {
            domain = value;
            onPropertyChanged(new PropertyChangedEventArgs("Domain")); //$NON-NLS-1$
        }
    }

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        if (!Objects.equals(email, value)) {
            email = value;
            onPropertyChanged(new PropertyChangedEventArgs("Email")); //$NON-NLS-1$
        }
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    private void updateProperties() {
        DbUser user = getEntity();

        setDomain(user.getDomain());
        setEmail(user.getEmail());
    }
}
