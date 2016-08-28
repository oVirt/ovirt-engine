package org.ovirt.engine.core.bll.host.util;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.utils.linq.Function;

@Singleton
public class HostIdToLoggableNameFunction implements Function<Guid, String> {
    private final VdsStaticDao vdsStaticDao;

    @Inject
    HostIdToLoggableNameFunction(VdsStaticDao vdsStaticDao) {
        this.vdsStaticDao = Objects.requireNonNull(vdsStaticDao);
    }

    @Override
    public String eval(Guid hostId) {
        final VdsStatic host = vdsStaticDao.get(hostId);

        final Object result;
        if (host != null) {
            if (StringUtils.isEmpty(host.getName())) {
                result = hostId;
            } else {
                result = host.getName();
            }
        } else {
            result = hostId;
        }

        return String.valueOf(result);
    }
}
