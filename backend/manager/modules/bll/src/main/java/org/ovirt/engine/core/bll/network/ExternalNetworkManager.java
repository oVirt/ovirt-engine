package org.ovirt.engine.core.bll.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.provider.ProviderDao;

/**
 * Utility class to help manage external networks, such as deallocate NICs.
 */
public class ExternalNetworkManager {

    private VmNic nic;

    private Network network;

    @Inject
    private NetworkHelper networkHelper;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    /**
     * Create a manager for the specific vNIC.
     *
     * @param nic
     *            The vNIC to create a manager for.
     */
    ExternalNetworkManager(VmNic nic) {
        this.nic = nic;
    }

    /**
     * Create a manager for the specific vNIC with the given network.
     *
     * @param nic
     *            The vNIC to create a manager for.
     * @param network
     *            The network to manage.
     */
    ExternalNetworkManager(VmNic nic, Network network) {
        this.nic = nic;
        this.network = network;
    }

    private Network getNetwork() {
        if (network == null) {
            network = networkHelper.getNetworkByVnicProfileId(nic.getVnicProfileId());
        }

        return network;
    }

    /**
     * Deallocate the vNIC from the external network, if it's attached to a network and the network is indeed an
     * external network (otherwise, nothing is done).
     */
    public void deallocateIfExternal() {
        if (getNetwork() != null && getNetwork().isExternal()) {
            Provider<?> provider = providerDao.get(getNetwork().getProvidedBy().getProviderId());
            NetworkProviderProxy providerProxy = providerProxyFactory.create(provider);

            try {
                providerProxy.deallocate(nic);
            } catch (EngineException e) {
                AuditLogable removePortFailureEvent = new AuditLogableImpl();
                removePortFailureEvent.addCustomValue("NicName", nic.getName());
                removePortFailureEvent.addCustomValue("NicId", nic.getId().toString());
                removePortFailureEvent.addCustomValue("ProviderName", provider.getName());
                auditLogDirector.log(removePortFailureEvent, AuditLogType.REMOVE_PORT_FROM_EXTERNAL_PROVIDER_FAILED);
            }
        }
    }

}
