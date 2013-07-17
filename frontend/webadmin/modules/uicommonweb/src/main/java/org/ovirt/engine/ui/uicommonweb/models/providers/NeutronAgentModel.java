package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.AgentConfiguration;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.QpidConfiguration;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.InterfaceMappingsValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class NeutronAgentModel extends EntityModel {

    private static final String QPID_PORT_DEFAULT = "5672"; //$NON-NLS-1$

    private final ListModel type;
    private final ListModel pluginType;

    private EntityModel interfaceMappingsLabel = new EntityModel();
    private EntityModel interfaceMappingsExplanation = new EntityModel();
    private EntityModel interfaceMappings = new EntityModel();
    private EntityModel qpidHost = new EntityModel();
    private EntityModel qpidPort = new EntityModel();
    private EntityModel qpidUsername = new EntityModel();
    private EntityModel qpidPassword = new EntityModel();

    public ListModel getType() {
        return type;
    }

    public ListModel getPluginType() {
        return pluginType;
    }

    public EntityModel getInterfaceMappingsLabel() {
        return interfaceMappingsLabel;
    }

    public EntityModel getInterfaceMappingsExplanation() {
        return interfaceMappingsExplanation;
    }

    public EntityModel getInterfaceMappings() {
        return interfaceMappings;
    }

    public EntityModel getQpidHost() {
        return qpidHost;
    }

    public EntityModel getQpidPort() {
        return qpidPort;
    }

    public EntityModel getQpidUsername() {
        return qpidUsername;
    }

    public EntityModel getQpidPassword() {
        return qpidPassword;
    }

    public NeutronAgentModel(final ListModel type, final ListModel pluginType) {
        this.type = type;
        this.pluginType = pluginType;

        getType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateAvailability();
            }
        });
        getPluginType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateAvailability();
                String displayString = getPluginDisplayString();
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

        getInterfaceMappingsLabel().setEntity(ConstantsManager.getInstance().getConstants().interfaceMappings());
        getInterfaceMappingsExplanation().setEntity(ConstantsManager.getInstance()
                .getConstants()
                .interfaceMappingsExplanation());
        getQpidPort().setEntity(QPID_PORT_DEFAULT);
    }

    private void updateAvailability() {
        boolean providerNeutron = getType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK;
        getPluginType().setIsAvailable(providerNeutron);
        setIsAvailable(providerNeutron
                && !NeutronPluginTranslator.isDisplayStringCustom(getPluginDisplayString()));
    }

    private String getPluginDisplayString() {
        String res = (String) getPluginType().getSelectedItem();
        return (res == null) ? new String() : res;
    }

    public boolean validate() {
        if (getIsAvailable()) {
            getInterfaceMappings().validateEntity(new IValidation[] { new InterfaceMappingsValidation() });
            getQpidHost().validateEntity(new IValidation[] { new HostAddressValidation(true) });
            getQpidPort().validateEntity(new IValidation[] { new IntegerValidation(BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
                    BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT) });
            setIsValid(getInterfaceMappings().getIsValid() && getQpidHost().getIsValid() && getQpidPort().getIsValid());
        }
        return getIsValid();
    }

    public void init(AgentConfiguration agentConfiguration) {
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

    public AgentConfiguration flush() {
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        agentConfiguration.setNetworkMappings((String) getInterfaceMappings().getEntity());

        QpidConfiguration qpidConfiguration = new QpidConfiguration();
        agentConfiguration.setQpidConfiguration(qpidConfiguration);
        qpidConfiguration.setAddress((String) getQpidHost().getEntity());
        String port = (String) getQpidPort().getEntity();
        qpidConfiguration.setPort(port == null ? null : Integer.valueOf(port));
        qpidConfiguration.setUsername((String) getQpidUsername().getEntity());
        qpidConfiguration.setPassword((String) getQpidPassword().getEntity());

        return agentConfiguration;
    }

}
