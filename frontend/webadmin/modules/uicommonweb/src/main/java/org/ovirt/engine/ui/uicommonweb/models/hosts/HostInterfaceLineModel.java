package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostInterfaceLineModel extends Model {

    private VdsNetworkInterface privateInterface;

    public VdsNetworkInterface getInterface() {
        return privateInterface;
    }

    public void setInterface(VdsNetworkInterface value) {
        privateInterface = value;
    }

    private boolean isBonded;

    public boolean getIsBonded() {
        return isBonded;
    }

    public void setIsBonded(boolean value) {
        if (isBonded != value) {
            isBonded = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsBonded")); //$NON-NLS-1$
        }
    }

    private String bondName;

    public String getBondName() {
        return bondName;
    }

    public void setBondName(String value) {
        if (!ObjectUtils.objectsEqual(bondName, value)) {
            bondName = value;
            onPropertyChanged(new PropertyChangedEventArgs("BondName")); //$NON-NLS-1$
        }
    }

    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        if (!ObjectUtils.objectsEqual(address, value)) {
            address = value;
            onPropertyChanged(new PropertyChangedEventArgs("Address")); //$NON-NLS-1$
        }
    }

    private ArrayList<HostInterface> interfaces;

    public ArrayList<HostInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(ArrayList<HostInterface> value) {
        if (interfaces != value) {
            interfaces = value;
            onPropertyChanged(new PropertyChangedEventArgs("Interfaces")); //$NON-NLS-1$
        }
    }

    private ArrayList<HostVLan> vLans;

    public ArrayList<HostVLan> getVLans() {
        return vLans;
    }

    public void setVLans(ArrayList<HostVLan> value) {
        if (vLans != value) {
            vLans = value;
            onPropertyChanged(new PropertyChangedEventArgs("VLans")); //$NON-NLS-1$
        }
    }

    public int getVlanSize() {
        return vLans == null ? 0 : vLans.size();
    }

    private String networkName;

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String value) {
        if (!ObjectUtils.objectsEqual(networkName, value)) {
            networkName = value;
            onPropertyChanged(new PropertyChangedEventArgs("NetworkName")); //$NON-NLS-1$
        }
    }

    private boolean isManagement;

    public boolean getIsManagement() {
        return isManagement;
    }

    public void setIsManagement(boolean value) {
        if (isManagement != value) {
            isManagement = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsManagement")); //$NON-NLS-1$
        }
    }

}
