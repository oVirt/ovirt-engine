package org.ovirt.engine.core.bll;

import javax.enterprise.inject.Produces;

import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;

public class OsRepositoryProducer {

    @Produces
    private OsRepository produce() {
        return SimpleDependencyInjector.getInstance().get(OsRepository.class);
    }

}
