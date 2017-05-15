package org.ovirt.engine.core.bll.network;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.di.Injector;

@Singleton
public class ManageNetworksParametersBuilderFactory {

    public ManageNetworksParametersBuilder create(CommandContext commandContext,
            InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        return Injector.get(ManageNetworksParametersBuilderImpl.class);
    }
}
