package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class NonePolicyUnit extends PolicyUnitImpl {

    public NonePolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<Pair<Guid, Integer>> score(List<VDS> hosts, Map<String, Object> parameters) {
        List<Pair<Guid, Integer>> list = new ArrayList<Pair<Guid, Integer>>();
        for (VDS host : hosts) {
            list.add(new Pair<Guid, Integer>(host.getId(), 1));
        }
        return list;
    }

    @Override
    public Pair<List<Guid>, Guid> balance(VDSGroup cluster,
            List<VDS> hosts,
            Map<String, String> parameters,
            ArrayList<String> messages) {
        return null;
    }
}
