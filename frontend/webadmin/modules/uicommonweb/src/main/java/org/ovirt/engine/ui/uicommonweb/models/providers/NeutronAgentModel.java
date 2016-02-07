package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkPluginType;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.AgentConfiguration;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.BrokerType;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.MessagingConfiguration;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.InterfaceMappingsValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NeutronAgentModel extends EntityModel {

    private ListModel<String> pluginType = new ListModel<>();

    private EntityModel<Boolean> pluginConfigurationAvailable = new EntityModel<>();
    private EntityModel<String> interfaceMappingsLabel = new EntityModel<>();
    private EntityModel<String> interfaceMappingsExplanation = new EntityModel<>();
    private EntityModel<String> interfaceMappings = new EntityModel<>();
    private ListModel<BrokerType> brokerType = new ListModel<>();
    private EntityModel<String> messagingServer = new EntityModel<>();
    private EntityModel<String> messagingServerPort = new EntityModel<>();
    private EntityModel<String> messagingServerUsername = new EntityModel<>();
    private EntityModel<String> messagingServerPassword = new EntityModel<>();

    public ListModel<String> getPluginType() {
        return pluginType;
    }

    public EntityModel<Boolean> isPluginConfigurationAvailable() {
        return pluginConfigurationAvailable;
    }

    public EntityModel<String> getInterfaceMappingsLabel() {
        return interfaceMappingsLabel;
    }

    public EntityModel<String> getInterfaceMappingsExplanation() {
        return interfaceMappingsExplanation;
    }

    public EntityModel<String> getInterfaceMappings() {
        return interfaceMappings;
    }

    public ListModel<BrokerType> getBrokerType() {
        return brokerType;
    }

    public EntityModel<String> getMessagingServer() {
        return messagingServer;
    }

    public EntityModel<String> getMessagingServerPort() {
        return messagingServerPort;
    }

    public EntityModel<String> getMessagingServerUsername() {
        return messagingServerUsername;
    }

    public EntityModel<String> getMessagingServerPassword() {
        return messagingServerPassword;
    }

    public NeutronAgentModel() {
        getPluginType().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                String displayString = getPluginType().getSelectedItem();
                isPluginConfigurationAvailable().setEntity(!NeutronPluginTranslator.isDisplayStringCustom(displayString));
                if (!NeutronPluginTranslator.isDisplayStringCustom(displayString)) {
                    switch(NeutronPluginTranslator.getPluginTypeForDisplayString(displayString)) {
                        case OPEN_VSWITCH:
                        getInterfaceMappingsLabel().setEntity(ConstantsManager.getInstance()
                                .getConstants()
                                .bridgeMappings());
                        getInterfaceMappingsExplanation().setEntity(ConstantsManager.getInstance()
                                .getConstants()
                                .bridgeMappingsExplanation());
                            break;
                        case LINUX_BRIDGE:
                        default:
                        getInterfaceMappingsLabel().setEntity(ConstantsManager.getInstance()
                                .getConstants()
                                .interfaceMappings());
                        getInterfaceMappingsExplanation().setEntity(ConstantsManager.getInstance()
                                .getConstants()
                                .interfaceMappingsExplanation());
                    }
                }
            }
        });
        getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                    boolean value = getIsAvailable();
                    getPluginType().setIsAvailable(value);
                    isPluginConfigurationAvailable().setEntity(value
                            && !NeutronPluginTranslator.isDisplayStringCustom(getPluginType().getSelectedItem()));
                }
            }
        });

        getPluginType().setItems(NeutronPluginTranslator.getPresetDisplayStrings());
        getPluginType().setSelectedItem(getDefaultPluginTypeString());
        getInterfaceMappingsLabel().setEntity(ConstantsManager.getInstance().getConstants().interfaceMappings());
        getInterfaceMappingsExplanation().setEntity(ConstantsManager.getInstance()
                .getConstants()
                .interfaceMappingsExplanation());
        getBrokerType().setItems(Arrays.asList(BrokerType.values()));
        getBrokerType().setSelectedItem(BrokerType.RABBIT_MQ);
    }

    public boolean validate() {
        if (getIsAvailable()) {
            getPluginType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
            getBrokerType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
            getInterfaceMappings().validateEntity(new IValidation[] { new InterfaceMappingsValidation() });
            getMessagingServer().validateEntity(new IValidation[] { new HostAddressValidation(true, true) });
            getMessagingServerPort().validateEntity(new IValidation[] { new IntegerValidation(BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
                    BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT) });

            setIsValid(getPluginType().getIsValid() && getInterfaceMappings().getIsValid()
                    && getMessagingServer().getIsValid() && getMessagingServerPort().getIsValid()
                    && getBrokerType().getIsValid());
        }
        return getIsValid();
    }

    public void init(Provider<OpenstackNetworkProviderProperties> provider) {
        OpenstackNetworkProviderProperties properties = provider.getAdditionalProperties();
        String pluginName = (properties == null) ?  getDefaultPluginTypeString() : properties.getPluginType();
        getPluginType().setSelectedItem(NeutronPluginTranslator.getDisplayStringForPluginName(pluginName));

        if (properties != null) {
            AgentConfiguration agentConfiguration = properties.getAgentConfiguration();
            if (agentConfiguration != null) {
                getInterfaceMappings().setEntity(agentConfiguration.getNetworkMappings());

                MessagingConfiguration messagingConfiguration = agentConfiguration.getMessagingConfiguration();
                if (messagingConfiguration != null) {
                    getBrokerType().setSelectedItem(messagingConfiguration.getBrokerType());
                    getMessagingServer().setEntity(messagingConfiguration.getAddress());
                    Integer port = messagingConfiguration.getPort();
                    getMessagingServerPort().setEntity(port == null ? null : Integer.toString(port));
                    getMessagingServerUsername().setEntity(messagingConfiguration.getUsername());
                    getMessagingServerPassword().setEntity(messagingConfiguration.getPassword());
                }
            }
        }
    }

    public void flush(Provider<OpenstackNetworkProviderProperties> provider) {
        OpenstackNetworkProviderProperties properties = provider.getAdditionalProperties();
        if (properties == null) {
            properties = new OpenstackNetworkProviderProperties();
            provider.setAdditionalProperties(properties);
        }
        properties.setPluginType(NeutronPluginTranslator.
                getPluginNameForDisplayString(getPluginType().getSelectedItem()));

        if (!isPluginConfigurationAvailable().getEntity()) {
            properties.setAgentConfiguration(null);
        } else {
            AgentConfiguration agentConfiguration = properties.getAgentConfiguration();
            if (agentConfiguration == null) {
                agentConfiguration = new AgentConfiguration();
                properties.setAgentConfiguration(agentConfiguration);
            }
            agentConfiguration.setNetworkMappings(getInterfaceMappings().getEntity());

            MessagingConfiguration messagingConfiguration = agentConfiguration.getMessagingConfiguration();
            if (messagingConfiguration == null) {
                messagingConfiguration = new MessagingConfiguration();
                agentConfiguration.setMessagingConfiguration(messagingConfiguration);
            }
            messagingConfiguration.setAddress(getMessagingServer().getEntity());
            String port = getMessagingServerPort().getEntity();
            messagingConfiguration.setPort(port == null ? null : Integer.valueOf(port));
            messagingConfiguration.setUsername(getMessagingServerUsername().getEntity());
            messagingConfiguration.setPassword(getMessagingServerPassword().getEntity());
            messagingConfiguration.setBrokerType(getBrokerType().getSelectedItem());
        }
    }

    private String getDefaultPluginTypeString() {
        return EnumTranslator.getInstance().translate(OpenstackNetworkPluginType.OPEN_VSWITCH);
    }

}
