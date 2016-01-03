package org.ovirt.engine.core.bll;

import javax.enterprise.inject.Produces;

import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;

public class OsRepositoryProducer {

    @Produces
    private OsRepository produce() {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class);
    }

}
