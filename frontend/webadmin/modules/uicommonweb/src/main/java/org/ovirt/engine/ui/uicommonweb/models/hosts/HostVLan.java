package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class HostVLan extends Model {

    private VdsNetworkInterface privateInterface;

    public VdsNetworkInterface getInterface() {
        return privateInterface;
    }

    public void setInterface(VdsNetworkInterface value) {
        privateInterface = value;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!Objects.equals(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private String networkName;

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String value) {
        if (!Objects.equals(networkName, value)) {
            networkName = value;
            onPropertyChanged(new PropertyChangedEventArgs("NetworkName")); //$NON-NLS-1$
        }
    }

    private String ipv4Address;

    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String value) {
        ipv4Address = value;
    }

    private String ipv6Address;

    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String value) {
        ipv6Address = value;
    }

}
