package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.CustomPropertiesForVdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class UserConfiguredNetworkData {
    private final List<NetworkAttachment> networkAttachments;
    private final List<VdsNetworkInterface> nics;
    private final CustomPropertiesForVdsNetworkInterface customPropertiesForVdsNetworkInterface;

    /**
     * This flag, as a workaround, modifies behavior of {@code HostNetworkAttachmentsPersister}. It should be removed
     * along with {@link #createForSetupNetworksCommand(List, CustomPropertiesForVdsNetworkInterface)} factory
     * method during {@code SetupNetworksCommand} demise, planned for 4.0
     */
    private final boolean legacyMode;

    public UserConfiguredNetworkData() {
        this(Collections.<NetworkAttachment>emptyList(), Collections.<VdsNetworkInterface>emptyList());
    }

    public UserConfiguredNetworkData(List<NetworkAttachment> networkAttachments, List<VdsNetworkInterface> nics) {
        this(networkAttachments, nics, null, false);
    }

    private UserConfiguredNetworkData(List<NetworkAttachment> networkAttachments,
            List<VdsNetworkInterface> nics,
            CustomPropertiesForVdsNetworkInterface customPropertiesForVdsNetworkInterface,
            boolean legacyMode) {
        this.networkAttachments = networkAttachments;
        this.nics = nics;
        this.customPropertiesForVdsNetworkInterface = customPropertiesForVdsNetworkInterface;
        this.legacyMode = legacyMode;
    }

    /**
     * @param nics nics to update
     * @param customPropertiesForVdsNetworkInterface custom properties for given nics.
     * @return UserConfiguredNetworkData instance which instructs {@code HostNetworkAttachmentsPersister} to synchronize
     * Ip configuration, custom properties, and other actions related to legacy {@code SetupNetworksCommand}.
     * @deprecated This method is intended only to fix problems with {@code SetupNetworksCommand} calling
     * {@code HostNetworkTopologyPersister}.
     */
    @Deprecated
    public static UserConfiguredNetworkData createForSetupNetworksCommand(List<VdsNetworkInterface> nics,
            CustomPropertiesForVdsNetworkInterface customPropertiesForVdsNetworkInterface) {
        return new UserConfiguredNetworkData(Collections.<NetworkAttachment> emptyList(), nics, customPropertiesForVdsNetworkInterface, true);
    }

    public List<NetworkAttachment> getNetworkAttachments() {
        return networkAttachments;
    }

    public List<VdsNetworkInterface> getNics() {
        return nics;
    }

    public CustomPropertiesForVdsNetworkInterface getCustomPropertiesForVdsNetworkInterface() {
        return customPropertiesForVdsNetworkInterface;
    }

    public boolean isLegacyMode() {
        return legacyMode;
    }
}
