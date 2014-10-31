package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(MigrationPolicyUnit.class);

    public MigrationPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    private boolean validateDestinationVdsAddress(VDS srcVds, VDS dstVds) {
        InetAddress dstAddress = null;

        try {
            dstAddress = InetAddress.getByName(dstVds.getHostName());
        } catch (UnknownHostException e) {
            // no clues, better to not guess. Let libvirt sort out the mess,
            // as it already did before.
            return true;
        }

        if (dstAddress != null && dstAddress.isLoopbackAddress()) {
            log.warn("cannot migrate to VDS {}: resolved to loopback address", dstVds.getHostName());
            return false;
        }

        if (srcVds != null) { // on runVm will be null
            InetAddress srcAddress = null;

            if (srcVds.getHostName().equals(dstVds.getHostName())) {
                log.warn("cannot migrate to VDS {}: same hostname as source", dstVds.getHostName());
                return false;
            }

            try {
                srcAddress = InetAddress.getByName(srcVds.getHostName());
            } catch (UnknownHostException e) {
                return true; // same as per dst address
            }

            if (srcAddress != null && dstAddress != null && srcAddress.equals(dstAddress)) {
                log.warn("cannot migrate to VDS {}: resolved address equal to source", dstVds.getHostName());
                return false;
            }
        }

        return true;
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {

        if (vm.getRunOnVds() != null) {
            List<VDS> hostsToRunOn = new ArrayList<>();
            VDS srcVds = getVdsDAO().get(vm.getRunOnVds());

            for (VDS host : hosts) {
                if (host.getId().equals(vm.getRunOnVds())) {
                    log.debug("Vm '{}' already runs on host '{}', filtering host", vm.getName(), host.getName());
                    messages.addMessage(host.getId(), VdcBllMessages.VAR__DETAIL__SAME_HOST.toString());
                    continue;
                }

                if (!validateDestinationVdsAddress(srcVds, host)) {
                    log.debug("Host '{}' resolves to the same machine as host '{}', filtering out", srcVds.getName(), host.getName());
                    messages.addMessage(host.getId(), VdcBllMessages.VAR__DETAIL__SAME_HOST.toString());
                    continue;
                }

                hostsToRunOn.add(host);
            }

            return hostsToRunOn;
        }

        return hosts;
    }

    protected VdsDAO getVdsDAO() {
        return DbFacade.getInstance().getVdsDao();
    }
}
