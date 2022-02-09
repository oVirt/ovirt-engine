package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.utils.vdshooks.VdsHooksParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "aae8c702-4756-4934-b84c-8daf59efc134",
        name = "Host-hooks",
        description = "Runs VMs only on hosts with a hooks required by VM's configuration",
        type = PolicyUnitType.FILTER)
public class HostHooksFilterPolicyUnit extends PolicyUnitImpl {

    private static final String SAP_AGENT_REQUIRED_HOOK = "50_vhostmd";

    private static final Logger log = LoggerFactory.getLogger(HostHooksFilterPolicyUnit.class);

    public HostHooksFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {

        if (!isSapAgent(vm)) {
            return hosts;
        }

        List<VDS> hostsToRunOn = new ArrayList<>();

        for (VDS host : hosts) {
            Set<String> scripts = VdsHooksParser.parseScriptNames(host.getHooksStr());
            if (scripts.contains(SAP_AGENT_REQUIRED_HOOK)) {
                hostsToRunOn.add(host);
                log.debug("Host {} wasn't filtered out as it contains required VDS hook for sap_agent VMs ({})",
                        host.getName(),
                        SAP_AGENT_REQUIRED_HOOK);
            } else {
                log.debug("Host {} was filtered out as it doesn't contain required VDS hook for sap_agent VMs ({})",
                        host.getName(),
                        SAP_AGENT_REQUIRED_HOOK);

                messages.addMessage(host.getId(),
                        EngineMessage.VAR__DETAIL__MISSING_VDS_HOOK.toString());
                messages.addMessage(host.getId(), String.format("$vdsHook %1$s", SAP_AGENT_REQUIRED_HOOK));
            }
        }
        return hostsToRunOn;
    }

    private boolean isSapAgent(VM vm) {
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();
        Map<String, String> customProperties = util.convertProperties(vm.getCustomProperties());

        String sapAgent = customProperties.get("sap_agent");
        try {
            return Boolean.parseBoolean(sapAgent);
        } catch (Exception e) {
            return false;
        }
    }
}
