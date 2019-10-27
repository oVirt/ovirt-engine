package org.ovirt.engine.core.bll.provider;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.host.provider.foreman.ForemanHostProviderProxy;
import org.ovirt.engine.core.bll.provider.cluster.KubevirtProviderProxy;
import org.ovirt.engine.core.bll.provider.network.UnmanagedNetworkProviderProxy;
import org.ovirt.engine.core.bll.provider.network.openstack.ExternalNetworkProviderProxy;
import org.ovirt.engine.core.bll.provider.network.openstack.OpenstackNetworkProviderProxy;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.bll.provider.vms.KVMVmProviderProxy;
import org.ovirt.engine.core.bll.provider.vms.VmwareVmProviderProxy;
import org.ovirt.engine.core.bll.provider.vms.XENVmProviderProxy;
import org.ovirt.engine.core.common.businessentities.KVMVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.XENVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.di.Injector;

/**
 * The provider proxy factory can create a provider proxy according to the provider definition.
 */
@Singleton
public class ProviderProxyFactory {
    /**
     * Create the proxy used to communicate with the given provider.
     *
     * @param provider
     *            The provider to create the proxy for.
     * @return The proxy for communicating with the provider
     */
    @SuppressWarnings("unchecked")
    public <P extends ProviderProxy<?>> P create(Provider<?> provider) {
        switch (provider.getType()) {
        case EXTERNAL_NETWORK:
            if (provider.getIsUnmanaged()) {
                return (P) new UnmanagedNetworkProviderProxy((Provider<OpenstackNetworkProviderProperties>) provider);
            }
            return (P) new ExternalNetworkProviderProxy((Provider<OpenstackNetworkProviderProperties>) provider);

        case FOREMAN:
            return (P) new ForemanHostProviderProxy(provider);

        case OPENSTACK_NETWORK:
            return (P) new OpenstackNetworkProviderProxy((Provider<OpenstackNetworkProviderProperties>) provider);

        case OPENSTACK_IMAGE:
            return (P) new OpenStackImageProviderProxy((Provider<OpenStackImageProviderProperties>) provider);

        case OPENSTACK_VOLUME:
            return (P) new OpenStackVolumeProviderProxy((Provider<OpenStackVolumeProviderProperties>) provider);

        case VMWARE:
            return (P) Injector.injectMembers(new VmwareVmProviderProxy((Provider<VmwareVmProviderProperties>) provider));

        case KVM:
            return (P) Injector.injectMembers(new KVMVmProviderProxy((Provider<KVMVmProviderProperties>) provider));

        case XEN:
            return (P) Injector.injectMembers(new XENVmProviderProxy((Provider<XENVmProviderProperties>) provider));

        case KUBEVIRT:
            return (P) Injector.injectMembers(new KubevirtProviderProxy((Provider<KubevirtProviderProperties>) provider));

        default:
            return null;
        }
    }
}
