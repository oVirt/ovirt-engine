package org.ovirt.engine.core.bll.network.vm.mac;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.VM;

public interface ProblematicVmMacsFinder {
    /**
     * Finds problematic MAC addresses that are defined on the VM's vNics.
     *
     * @param vm the VM to be validates.
     * @return collection of problematic MAC addresses that are defined on the VM's vNics.
     */
    Collection<String> findProblematicMacs(VM vm);
}
