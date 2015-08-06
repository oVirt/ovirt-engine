package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;

public class ImportNetworkData {

    private List<String> networkNames;
    private String selectedNetworkName;
    private List<VnicProfileView> networkProfiles;
    private List<VnicProfileView> filteredNetworkProfiles;
    private VnicProfileView selectedNetworkProfile;

    public ImportNetworkData(List<VnicProfileView> networkProfiles) {
        this.networkProfiles = networkProfiles;
        Set<String> networkNames = new HashSet<>();
        for (VnicProfileView networkProfile : networkProfiles) {
            networkNames.add(networkProfile.getNetworkName());
        }
        setNetworkNames(new ArrayList<>(networkNames));
    }

    public List<VnicProfileView> getFilteredNetworkProfiles() {
        return filteredNetworkProfiles;
    }

    public void setSelectedNetworkProfile(String networkProfileName) {
        for (VnicProfileView profile : networkProfiles) {
            if (profile.getName().equals(networkProfileName)) {
                setSelectedNetworkProfile(profile);
                break;
            }
        }
    }

    public VnicProfileView getSelectedNetworkProfile() {
        if (selectedNetworkProfile != null) {
            return selectedNetworkProfile;
        }
        return !filteredNetworkProfiles.isEmpty() ? filteredNetworkProfiles.get(0) : null;
    }

    private void setSelectedNetworkProfile(VnicProfileView networkProfile) {
        selectedNetworkProfile = networkProfile;
    }

    public List<String> getNetworkNames() {
        return networkNames;
    }

    private void setNetworkNames(List<String> networkNames) {
        this.networkNames = networkNames;
        if (!networkNames.isEmpty()) {
            setSelectedNetworkName(networkNames.get(0));
        }
    }

    public void setSelectedNetworkName(String networkName) {
        selectedNetworkName = networkName;
        filteredNetworkProfiles = new ArrayList<>();
        for (VnicProfileView profile : networkProfiles) {
            if (profile.getNetworkName().equals(networkName)) {
                filteredNetworkProfiles.add(profile);
            }
        }
    }

    public String getSelectedNetworkName() {
        return selectedNetworkName;
    }
}
