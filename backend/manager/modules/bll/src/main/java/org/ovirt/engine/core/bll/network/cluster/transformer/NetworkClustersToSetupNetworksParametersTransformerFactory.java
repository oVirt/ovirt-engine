package org.ovirt.engine.core.bll.network.cluster.transformer;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.ManageNetworksParametersBuilderFactory;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

@Singleton
public class NetworkClustersToSetupNetworksParametersTransformerFactory {

    private final NetworkDao networkDao;
    private final InterfaceDao interfaceDao;
    private final VdsStaticDao vdsStaticDao;
    private final NetworkClusterDao networkClusterDao;
    private final NetworkAttachmentDao networkAttachmentDao;
    private final ManageNetworksParametersBuilderFactory manageNetworksParametersBuilderFactory;
    private final NetworkImplementationDetailsUtils networkImplementationDetailsUtils;

    @Inject
    NetworkClustersToSetupNetworksParametersTransformerFactory(
            NetworkDao networkDao,
            InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao,
            NetworkImplementationDetailsUtils networkImplementationDetailsUtils,
            ManageNetworksParametersBuilderFactory manageNetworksParametersBuilderFactory) {
        Objects.requireNonNull(networkDao, "networkDao cannot be null");
        Objects.requireNonNull(interfaceDao, "interfaceDao cannot be null");
        Objects.requireNonNull(manageNetworksParametersBuilderFactory,
                "manageLabeledNetworksParametersBuilderFactory cannot be null");
        Objects.requireNonNull(networkImplementationDetailsUtils, "networkImplementationDetailsUtils cannot be null");


        this.networkDao = networkDao;
        this.interfaceDao = interfaceDao;
        this.vdsStaticDao = vdsStaticDao;
        this.networkClusterDao = networkClusterDao;
        this.networkAttachmentDao = networkAttachmentDao;
        this.manageNetworksParametersBuilderFactory = manageNetworksParametersBuilderFactory;
        this.networkImplementationDetailsUtils = networkImplementationDetailsUtils;
    }

    public NetworkClustersToSetupNetworksParametersTransformer createNetworkClustersToSetupNetworksParametersTransformer(
            CommandContext commandContext) {
        return new NetworkClustersToSetupNetworksParametersTransformerImpl(
                networkDao,
                interfaceDao,
                vdsStaticDao,
                networkClusterDao,
                networkAttachmentDao,
                manageNetworksParametersBuilderFactory,
                networkImplementationDetailsUtils,
                commandContext);
    }
}
