package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmInterfacesModifyParameters extends ActionParametersBase implements Serializable {

    public static class VnicWithProfile implements Serializable {

        private static final long serialVersionUID = -1674104171290768694L;

        private VmNetworkInterface networkInterface;
        private VnicProfileView profile;

        public VnicWithProfile() {
        }

        public VnicWithProfile(VmNetworkInterface networkInterface, VnicProfileView profile) {
            this.networkInterface = networkInterface;
            this.profile = profile;
        }

        public VmNetworkInterface getNetworkInterface() {
            return networkInterface;
        }

        public void setNetworkInterface(VmNetworkInterface networkInterface) {
            this.networkInterface = networkInterface;
        }

        public VnicProfileView getProfile() {
            return profile;
        }

        public void setProfile(VnicProfileView profile) {
            this.profile = profile;
        }

    }

    private static final long serialVersionUID = -6815706150385031006L;

    private Guid vmId;
    private int osId;
    private Version compatibilityVersion;
    private Collection<VnicWithProfile> vnicsWithProfiles;
    private boolean addingNewVm;

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public int getOsId() {
        return osId;
    }

    public void setOsId(int osId) {
        this.osId = osId;
    }

    public Version getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(Version compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    public Collection<VnicWithProfile> getVnicsWithProfiles() {
        return vnicsWithProfiles;
    }

    public void setVnicsWithProfiles(Collection<VnicWithProfile> vnicsWithProfiles) {
        this.vnicsWithProfiles = vnicsWithProfiles;
    }

    public boolean isAddingNewVm() {
        return addingNewVm;
    }

    public void setAddingNewVm(boolean addingNewVm) {
        this.addingNewVm = addingNewVm;
    }

}
