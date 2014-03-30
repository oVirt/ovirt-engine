package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.AgentConfiguration;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.QpidConfiguration;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.InterfaceMappingsValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NeutronAgentModel extends EntityModel {

    private ListModel<String> pluginType = new ListModel<String>();

    private EntityModel<Boolean> pluginConfigurationAvailable = new EntityModel<Boolean>();
    private EntityModel<String> interfaceMappingsLabel = new EntityModel<String>();
    private EntityModel<String> interfaceMappingsExplanation = new EntityModel<String>();
    private EntityModel<String> interfaceMappings = new EntityModel<String>();
    private EntityModel<String> qpidHost = new EntityModel<String>();
    private EntityModel<String> qpidPort = new EntityModel<String>();
    private EntityModel<String> qpidUsername = new EntityModel<String>();
    private EntityModel<String> qpidPassword = new EntityModel<String>();

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

    public EntityModel<String> getQpidHost() {
        return qpidHost;
    }

    public EntityModel<String> getQpidPort() {
        return qpidPort;
    }

    public EntityModel<String> getQpidUsername() {
        return qpidUsername;
    }

    public EntityModel<String> getQpidPassword() {
        return qpidPassword;
    }

    public NeutronAgentModel() {
        getPluginType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
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
        getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("IsAvailable".equals(((PropertyChangedEventArgs) args).propertyName)) { //$NON-NLS-1$
                    boolean value = getIsAvailable();
                    getPluginType().setIsAvailable(value);
                    isPluginConfigurationAvailable().setEntity(value
                            && !NeutronPluginTranslator.isDisplayStringCustom(getPluginType().getSelectedItem()));
                }
            }
        });

        getPluginType().setItems(NeutronPluginTranslator.getPresetDisplayStrings());
        getPluginType().setSelectedItem(""); //$NON-NLS-1$
        getInterfaceMappingsLabel().setEntity(ConstantsManager.getInstance().getConstants().interfaceMappings());
        getInterfaceMappingsExplanation().setEntity(ConstantsManager.getInstance()
                .getConstants()
                .interfaceMappingsExplanation());
    }

    public boolean validate() {
        if (getIsAvailable()) {
            getPluginType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
            getInterfaceMappings().validateEntity(new IValidation[] { new InterfaceMappingsValidation() });
            getQpidHost().validateEntity(new IValidation[] { new HostAddressValidation(true) });
            getQpidPort().validateEntity(new IValidation[] { new IntegerValidation(BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
                    BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT) });

            setIsValid(getPluginType().getIsValid() && getInterfaceMappings().getIsValid()
                    && getQpidHost().getIsValid() && getQpidPort().getIsValid());
        }
        return getIsValid();
    }

    public void init(Provider<OpenstackNetworkProviderProperties> provider) {
        OpenstackNetworkProviderProperties properties = provider.getAdditionalProperties();
        String pluginName = (properties == null) ? "" : properties.getPluginType(); //$NON-NLS-1$
        getPluginType().setSelectedItem(NeutronPluginTranslator.getDisplayStringForPluginName(pluginName));

        if (properties != null) {
            AgentConfiguration agentConfiguration = properties.getAgentConfiguration();
            if (agentConfiguration != null) {
                getInterfaceMappings().setEntity(agentConfiguration.getNetworkMappings());

                QpidConfiguration qpidConfiguration = agentConfiguration.getQpidConfiguration();
                if (qpidConfiguration != null) {
                    getQpidHost().setEntity(qpidConfiguration.getAddress());
                    Integer port = qpidConfiguration.getPort();
                    getQpidPort().setEntity(port == null ? null : Integer.toString(port));
                    getQpidUsername().setEntity(qpidConfiguration.getUsername());
                    getQpidPassword().setEntity(qpidConfiguration.getPassword());
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

            QpidConfiguration qpidConfiguration = agentConfiguration.getQpidConfiguration();
            if (qpidConfiguration == null) {
                qpidConfiguration = new QpidConfiguration();
                agentConfiguration.setQpidConfiguration(qpidConfiguration);
            }
            qpidConfiguration.setAddress(getQpidHost().getEntity());
            String port = getQpidPort().getEntity();
            qpidConfiguration.setPort(port == null ? null : Integer.valueOf(port));
            qpidConfiguration.setUsername(getQpidUsername().getEntity());
            qpidConfiguration.setPassword(getQpidPassword().getEntity());
        }
    }

}
