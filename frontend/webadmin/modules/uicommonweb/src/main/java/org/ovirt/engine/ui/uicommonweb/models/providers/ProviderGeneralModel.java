package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("deprecation")
public class ProviderGeneralModel extends EntityModel<Provider> {

    private static final String PROPERTY_NAME = "Name"; //$NON-NLS-1$
    private static final String PROPERTY_TYPE = "Type"; //$NON-NLS-1$
    private static final String PROPERTY_DESCRIPTION = "Description"; //$NON-NLS-1$
    private static final String PROPERTY_URL = "Url"; //$NON-NLS-1$

    private String name;
    private ProviderType type;
    private String description;
    private String url;

    public ProviderGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$
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
        Provider provider = getEntity();

        setName(provider.getName());
        setType(provider.getType());
        setDescription(provider.getDescription());
        setUrl(provider.getUrl());
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!Objects.equals(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs(PROPERTY_NAME));
        }
    }

    public ProviderType getType() {
        return type;
    }

    public void setType(ProviderType value) {
        if (value != type) {
            type = value;
            onPropertyChanged(new PropertyChangedEventArgs(PROPERTY_TYPE));
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        if (!Objects.equals(description, value)) {
            description = value;
            onPropertyChanged(new PropertyChangedEventArgs(PROPERTY_DESCRIPTION));
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        if (!Objects.equals(url, value)) {
            url = value;
            onPropertyChanged(new PropertyChangedEventArgs(PROPERTY_URL));
        }
    }

}
