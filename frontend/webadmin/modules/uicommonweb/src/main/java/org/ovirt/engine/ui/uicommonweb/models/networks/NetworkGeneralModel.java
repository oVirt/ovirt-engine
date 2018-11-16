package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NetworkGeneralModel extends EntityModel<NetworkView> {
    private String name;
    private Boolean vmNetwork;
    private Integer vlan;
    private Integer mtu;
    private String description;
    private Guid id;
    private String externalId;
    private String vdsmName;

    public NetworkGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (super.getEntity() != null) {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    private void updateProperties() {
        Network extendedNetwork = getEntity();

        setName(extendedNetwork.getName());
        setId(extendedNetwork.getId());
        setDescription(extendedNetwork.getDescription());
        setVmNetwork(extendedNetwork.isVmNetwork());
        setVlan(extendedNetwork.getVlanId());

        setMtu(extendedNetwork.getMtu());

        setVdsmName(extendedNetwork.getVdsmName());

        if (extendedNetwork.isExternal()) {
            setExternalId(extendedNetwork.getProvidedBy().getExternalId());
        } else {
            setExternalId(null);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!Objects.equals(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    public Boolean getVmNetwork() {
        return vmNetwork;
    }

    public void setVmNetwork(Boolean vmNetwork) {
        this.vmNetwork = vmNetwork;
    }

    public Integer getVlan() {
        return vlan;
    }

    public void setVlan(Integer value) {
        if (vlan == null && value == null) {
            return;
        }
        if (vlan == null || !vlan.equals(value)) {
            vlan = value;
            onPropertyChanged(new PropertyChangedEventArgs("Vlan")); //$NON-NLS-1$
        }
    }

    public Integer getMtu() {
        return mtu;
    }

    public void setMtu(Integer value) {
        if (mtu == null && value == null) {
            return;
        }
        if (mtu == null || !mtu.equals(value)) {
            mtu = value;
            onPropertyChanged(new PropertyChangedEventArgs("Mtu")); //$NON-NLS-1$
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        if (!Objects.equals(description, value)) {
            description = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid value) {
        if ((id == null && value != null) || (id != null && !id.equals(value))) {
            id = value;
            onPropertyChanged(new PropertyChangedEventArgs("Id")); //$NON-NLS-1$
        }
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String value) {
        if (!Objects.equals(externalId, value)) {
            externalId = value;
            onPropertyChanged(new PropertyChangedEventArgs("External Id")); //$NON-NLS-1$
        }
    }

    public String getVdsmName() {
        return vdsmName;
    }

    public void setVdsmName(String value) {
        if (!Objects.equals(vdsmName, value)) {
            vdsmName = value;
            onPropertyChanged(new PropertyChangedEventArgs("Vdsm Name")); //$NON-NLS-1$
        }
    }
}
