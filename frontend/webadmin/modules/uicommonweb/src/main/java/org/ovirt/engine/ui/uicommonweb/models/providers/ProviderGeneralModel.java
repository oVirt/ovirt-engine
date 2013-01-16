package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class ProviderGeneralModel extends EntityModel
{
    private String name;

    public ProviderGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (super.getEntity() != null)
        {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    private void updateProperties() {
        Provider provider = (Provider) getEntity();

        setName(provider.getName());

    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!StringHelper.stringsEqual(name, value))
        {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }
}
