package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.XENVmProviderProperties;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class XENPropertiesModel extends ProxyHostPropertiesModel {

    private EntityModel<String> url = new EntityModel<>();
    private ListModel<VDS> proxyHost = new ListModel<>();

    public XENPropertiesModel() {
        getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                url.setIsAvailable(getIsAvailable());
                proxyHost.setIsAvailable(getIsAvailable());
            }
        });
    }

    public EntityModel<String> getUrl() {
        return url;
    }

    public boolean validate() {
        if (!getIsAvailable()) {
            setIsValid(true);
        } else {
            getUrl().validateEntity(new IValidation[] {
                    new NotEmptyValidation(),
                    new LengthValidation(255) });
            setIsValid(getUrl().getIsValid());
        }

        return getIsValid();
    }

    public ListModel<VDS> getProxyHost() {
        return proxyHost;
    }

    XENVmProviderProperties getXENVmProviderProperties(Guid dataCenterId) {
        return new XENVmProviderProperties(
                getUrl().getEntity(),
                dataCenterId,
                getProxyHost().getSelectedItem() != null ? getProxyHost().getSelectedItem().getId() : null);
    }

    public void init(Provider<XENVmProviderProperties> provider) {
        XENVmProviderProperties properties = provider.getAdditionalProperties();

        getUrl().setEntity(properties.getUrl());

        setLastProxyHostId(properties.getProxyHostId());
        setLastStoragePoolId(properties.getStoragePoolId());
    }

}
