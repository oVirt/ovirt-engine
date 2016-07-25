package org.ovirt.engine.core.bll.host.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;

@Singleton
public class HostIdToLoggableNameFunction implements Function<Guid, String> {
    private final VdsStaticDao vdsStaticDao;

    @Inject
    HostIdToLoggableNameFunction(VdsStaticDao vdsStaticDao) {
        this.vdsStaticDao = Objects.requireNonNull(vdsStaticDao);
    }

    @Override
    public String apply(Guid hostId) {
        final Optional<VdsStatic> nullableHost = Optional.ofNullable(vdsStaticDao.get(hostId));
        final Optional<String> nullableHostName = nullableHost.map(VdsStatic::getName);
        return nullableHostName.orElseGet(() -> String.valueOf(hostId));
    }
}
