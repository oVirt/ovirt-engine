package org.ovirt.engine.core.bll.host.provider;

import java.util.List;

import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.common.businessentities.VDS;

public interface HostProviderProxy extends ProviderProxy {

    List<VDS> getAll();
    List<VDS> getFiltered(String filter);

}
