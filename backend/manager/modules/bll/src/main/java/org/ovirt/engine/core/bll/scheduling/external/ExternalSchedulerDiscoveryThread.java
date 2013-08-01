package org.ovirt.engine.core.bll.scheduling.external;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;
import org.ovirt.engine.core.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ExternalSchedulerDiscoveryThread extends Thread {

    private final static Log log = LogFactory.getLog(ExternalSchedulerDiscoveryThread.class);

    @Override
    public void run() {
        ExternalSchedulerDiscoveryResult discoveryResult = ExternalSchedulerFactory.getInstance().runDiscover();
        if (discoveryResult != null) {
            updateDB(discoveryResult);
            log.info("PolicyUnits updated");
        } else {
            log.warn("Discovery returned empty result, not updating policyunits");
        }
    }

    private void updateDB(ExternalSchedulerDiscoveryResult discoveryResult) {
        List<PolicyUnit> allPolicyUnits = getPolicyUnitDao().getAll();
        List<PolicyUnit> foundInBoth = new LinkedList<PolicyUnit>();
        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getFilters()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.Filter);
            if (found != null) {
                foundInBoth.add(found);
            }
        }
        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getScores()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.Weight);
            if (found != null) {
                foundInBoth.add(found);
            }
        }
        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getBalance()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.LoadBalancing);
            if (found != null) {
                foundInBoth.add(found);
            }
        }

        allPolicyUnits.removeAll(foundInBoth);
        // found in the db but not found in discovery, mark as such
        markExternalPoliciesAsDisabled(allPolicyUnits);

        SchedulingManager.getInstance().reloadPolicyUnits();
    }

    private void markExternalPoliciesAsDisabled(List<PolicyUnit> units) {
        for (PolicyUnit policyUnit : units) {
            if (!policyUnit.isInternal()) {
                policyUnit.setEnabled(false);
                getPolicyUnitDao().update(policyUnit);
            }
        }
    }

    private PolicyUnit compareToDB(List<PolicyUnit> dbEnteries,
            ExternalSchedulerDiscoveryUnit discoveryUnit,
            PolicyUnitType type) {
        for (PolicyUnit policyUnit : dbEnteries) {
            if (policyUnit.isInternal()) {
                continue;
            }

            if (policyUnit.getPolicyUnitType() != type) {
                continue;
            }

            if (!policyUnit.getName().equals(discoveryUnit.getName())) {
                continue;
            }

            try{
                Map<String, String> discoveryPropMap = SimpleCustomPropertiesUtil.getInstance().convertProperties(discoveryUnit.getRegex());
                if (!policyUnit.getParameterRegExMap().equals(discoveryPropMap)) {
                    sendToDb(discoveryUnit, true, type);
                }
            } catch (Exception e) {
                // TODO: handle exception? log?
            }

            // TODO: when policy unit description is merged, compare it as well

            return policyUnit;

        }
        sendToDb(discoveryUnit, false, type);
        return null;
    }

    private void sendToDb(ExternalSchedulerDiscoveryUnit discovery, boolean isUpdate, PolicyUnitType type) {
        PolicyUnit policy = createFromDiscoveryUnit(discovery, type);
        if (isUpdate) {
            getPolicyUnitDao().update(policy);
        } else {
            policy.setId(Guid.newGuid());
            getPolicyUnitDao().save(policy);
        }
    }

    private PolicyUnit createFromDiscoveryUnit(ExternalSchedulerDiscoveryUnit discoveryUnit, PolicyUnitType type) {
        PolicyUnit policy = new PolicyUnit();
        policy.setInternal(false);
        policy.setName(discoveryUnit.getName());
        policy.setPolicyUnitType(type);
        if(!StringUtils.isBlank(discoveryUnit.getRegex())) {
            policy.setParameterRegExMap(SimpleCustomPropertiesUtil.getInstance()
                    .convertProperties(discoveryUnit.getRegex()));
        } else {
            policy.setParameterRegExMap(new HashMap<String, String>());
        }
        // TODO: when policy unit description is merged, set it
        return policy;
    }

    public PolicyUnitDao getPolicyUnitDao() {
        return DbFacade.getInstance().getPolicyUnitDao();
    }
}
