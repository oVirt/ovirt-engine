package org.ovirt.engine.core.utils.lock;

import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

public class LockManagerFactory {

    public static LockManager getLockManager() {
        return EjbUtils.findBean(BeanType.LOCK_MANAGER, BeanProxyType.LOCAL);
    }
}
