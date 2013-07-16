package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
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
            return getInterfaceMappings().getIsValid() && getQpidHost().getIsValid() && getQpidPort().getIsValid();
        }
        return true;
    }

}
