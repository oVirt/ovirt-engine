package org.ovirt.engine.core.bll.network.macpool;

import java.util.function.Predicate;

import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MacAddressGlobalUsageTester implements Predicate<String> {
    private static final Logger log = LoggerFactory.getLogger(MacAddressGlobalUsageTester.class);
    private final boolean allowDuplicates;
    private final VmNicDao vmNicDao;

    MacAddressGlobalUsageTester(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        vmNicDao = Injector.get(VmNicDao.class);
    }

    @Override
    public boolean test(String macAddress) {
        if (!allowDuplicates && !vmNicDao.getPluggedForMac(macAddress).isEmpty()) {
            log.debug("Attempted to allocate mac {} but it is already in use on a plugged interface", macAddress);
            return true;
        }
        return false;
    }
}
