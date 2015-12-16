package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;

/**
 * Base class for Network Item Models
 *
 * @param <T>
 *            The Item status enum type
 */
public abstract class NetworkItemModel<T extends Enum<T>> extends ListModel<LogicalNetworkModel> implements Comparable<NetworkItemModel<T>> {

    private final HostSetupNetworksModel setupModel;

    private String error = null;

    /**
     * Create a Network item for the specified Setup Model
     */
    public NetworkItemModel(HostSetupNetworksModel setupModel) {
        this.setupModel = setupModel;
    }

    @Override
    public int compareTo(NetworkItemModel<T> o) {
        return LexoNumericComparator.comp(getName(), o.getName());
    }

    public String getError() {
        return error;
    }

    public abstract String getName();

    /**
     * Get the Network Setup Model
     */
    public HostSetupNetworksModel getSetupModel() {
        return setupModel;
    }

    public abstract T getStatus();

    public boolean hasError() {
        return error != null && error.length() > 0;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        String name = getClass().getName();
        T status = getStatus();
        return name.substring(name.lastIndexOf(".") + 1) + " [name=" + getName() + ", status=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + (status == null ? "?" : status.name()) //$NON-NLS-1$
                + ", error=" + hasError() //$NON-NLS-1$
                + "]"; //$NON-NLS-1$
    }

    /**
     * Edit the Network Item
     */
    public void edit() {
        setupModel.onEdit(this);

    }

    public abstract String getType();

    /**
     * Queries whether this item could contain several networks (e.g. interface, label).
     *
     * @return true iff it might contain a subcollection of networks.
     */
    public boolean aggregatesNetworks() {
        return false;
    }

    private String culpritNetwork;

    /**
     * If this item was the part of a null operation including a batch of networks, the culprit network is one of those
     * that caused the operation to fail, the first encountered of the following: unmanaged, out of sync, one of several
     * non-VLAN networks, VM network when VLAN networks exist.
     *
     * @return the name of the network at fault, or null if there isn't one.
     */
    public String getCulpritNetwork() {
        return culpritNetwork;
    }

    public void setCulpritNetwork(String culpritNetwork) {
        this.culpritNetwork = culpritNetwork;
    }

}
