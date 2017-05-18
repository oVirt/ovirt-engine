package org.ovirt.engine.ui.uicommonweb.models.dnsconfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class DnsConfigurationModel extends EntityModel<DnsResolverConfiguration> {

    protected static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private final EntityModel<Boolean> shouldSetDnsConfiguration = new EntityModel<>();
    private final ListModel<NameServerModel> nameServerModelListModel = new ListModel<>();

    public EntityModel<Boolean> getShouldSetDnsConfiguration() {
        return shouldSetDnsConfiguration;
    }

    public ListModel<NameServerModel> getNameServerModelListModel() {
        return nameServerModelListModel;
    }

    public DnsConfigurationModel() {
        listenToSetDnsConfigurationCheckboxChanges();

        getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("IsChangable".equals(args.propertyName)) { //$NON-NLS-1$
                    boolean value = getIsChangable();
                    shouldSetDnsConfiguration.setIsChangeable(value);
                    nameServerModelListModel.setIsChangeable(value);
                }
            }
        });
    }

    public void init() {
        boolean shouldSetDnsConfiguration = shouldSetDnsConfiguration();
        this.shouldSetDnsConfiguration.setEntity(shouldSetDnsConfiguration);

        List<NameServerModel> nameServerModels = new ArrayList<>();
        if (shouldSetDnsConfiguration) {
            for (NameServer nameServer : getEntity().getNameServers()) {
                nameServerModels.add(new NameServerModel(nameServer.getAddress()));
            }

        }
        nameServerModelListModel.setItems(nameServerModels);
    }

    @Override
    public void setEntity(DnsResolverConfiguration value) {
        super.setEntity(value);
        init();
    }

    private void listenToSetDnsConfigurationCheckboxChanges() {
        Event<EventArgs> entityChangedEvent = shouldSetDnsConfiguration.getEntityChangedEvent();

        entityChangedEvent.addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                nameServerModelListModel.setIsChangeable(shouldSetDnsConfiguration.getEntity());
            }
        });
    }

    private boolean shouldSetDnsConfiguration() {
        DnsResolverConfiguration entity = getEntity();
        if (entity == null) {
            return false;
        }
        List<NameServer> nameServers = entity.getNameServers();
        return nameServers != null && !nameServers.isEmpty();
    }

    public DnsResolverConfiguration flush() {
        Boolean shouldSetDnsConfiguration = this.shouldSetDnsConfiguration.getEntity();
        if (!shouldSetDnsConfiguration) {
            return null;
        }

        List<NameServer> nameServers = new ArrayList<>();
        for (NameServerModel nameServerModel : this.nameServerModelListModel.getItems()) {
            nameServers.add(new NameServer(nameServerModel.flush()));
        }

        DnsResolverConfiguration result = new DnsResolverConfiguration();
        result.setNameServers(nameServers);

        if (getEntity() != null) {
            result.setId(getEntity().getId());
        }

        return result;
    }

    public boolean validate() {
        if (!getShouldSetDnsConfiguration().getEntity()) {
            setShouldSetDnsConfigurationValidity(true);
            setIsValid(true);
            return true;
        }

        Collection<NameServerModel> items = this.nameServerModelListModel.getItems();
        int numberOfAddresses = items.size();

        boolean atLeastOneAddress = numberOfAddresses > 0;
        boolean exceedingNumberOfAddresses =
                numberOfAddresses > BusinessEntitiesDefinitions.MAX_SUPPORTED_DNS_CONFIGURATIONS;
        boolean isValid = atLeastOneAddress && !exceedingNumberOfAddresses && validateNameserverAddresses(items);

        setShouldSetDnsConfigurationValidity(atLeastOneAddress);
        setIsValid(isValid);
        return isValid;
    }

    private void setShouldSetDnsConfigurationValidity(boolean valid) {
        List<String> invalidityReasons = getShouldSetDnsConfiguration().getInvalidityReasons();
        if (!valid) {
            invalidityReasons.add(constants.atLeastOneDnsServerHasToBeConfigured());
        }
        getShouldSetDnsConfiguration().setIsValid(valid);
    }

    private boolean validateNameserverAddresses(Collection<NameServerModel> items) {
        boolean isValid = true;
        for (NameServerModel nameServerModel : items) {
            if (!nameServerModel.validate()) {
                 isValid = false;
            }
        }
        return isValid;
    }
}
