package org.ovirt.engine.core.bll.scheduling.external;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalSchedulerDiscovery {

    private static final Logger log = LoggerFactory.getLogger(ExternalSchedulerDiscovery.class);

    @Inject
    private PolicyUnitDao policyUnitDao;

    private ExternalSchedulerDiscovery() {}

    /**
     * Discover external schedulers and process its policy units. This operation may take time and is recommended to run
     * in a different thread. If we found new policy units we save them to db and expose them for usage.
     *
     * @return {@code true} if new policies where found and saved to db, {@code false} otherwise.
     */
    public boolean discover() {
        ExternalSchedulerDiscoveryResult discoveryResult = ExternalSchedulerFactory.getInstance().runDiscover();
        boolean dbUpdated = false;
        if (discoveryResult != null) {
            updateDB(discoveryResult);
            log.info("PolicyUnits updated");
            dbUpdated = true;
        } else {
            AuditLogableBase loggable = new AuditLogableBase();
            new AuditLogDirector().log(loggable, AuditLogType.FAILED_TO_CONNECT_TO_SCHEDULER_PROXY);
            markAllExternalPoliciesAsDisabled();
            log.warn("Discovery returned empty result, disabled external policy units");
        }
        return dbUpdated;
    }

    private void updateDB(ExternalSchedulerDiscoveryResult discoveryResult) {
        List<PolicyUnit> allPolicyUnits = policyUnitDao.getAll();
        List<PolicyUnit> foundInBoth = new LinkedList<>();
        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getFilters()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.FILTER);
            if (found != null) {
                foundInBoth.add(found);
            }
        }
        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getScores()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.WEIGHT);
            if (found != null) {
                foundInBoth.add(found);
            }
        }
        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getBalance()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.LOAD_BALANCING);
            if (found != null) {
                foundInBoth.add(found);
            }
        }

        allPolicyUnits.removeAll(foundInBoth);
        // found in the db but not found in discovery, mark as such
        markExternalPoliciesAsDisabled(allPolicyUnits);
    }

    private void markExternalPoliciesAsDisabled(List<PolicyUnit> units) {
        for (PolicyUnit policyUnit : units) {
            if (!policyUnit.isInternal()) {
                policyUnit.setEnabled(false);
                policyUnitDao.update(policyUnit);
            }
        }
    }

    public void markAllExternalPoliciesAsDisabled(){
        markExternalPoliciesAsDisabled(policyUnitDao.getAll());
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

            Map<String, String> discoveryPropMap =
                    StringUtils.isEmpty(discoveryUnit.getRegex()) ? new LinkedHashMap<>() :
                    SimpleCustomPropertiesUtil.getInstance().convertProperties(discoveryUnit.getRegex());
            if (!discoveryPropMap.equals(policyUnit.getParameterRegExMap()) ||
                    !discoveryUnit.getDescription().equals(policyUnit.getDescription()) ||
                    !policyUnit.isEnabled()) {
                sendToDb(discoveryUnit, policyUnit.getId(), type);
            }

            return policyUnit;

        }
        sendToDb(discoveryUnit, null, type);
        return null;
    }

    private void sendToDb(ExternalSchedulerDiscoveryUnit discovery, Guid policyUnitId, PolicyUnitType type) {
        PolicyUnit policy = createFromDiscoveryUnit(discovery, type);
        if (policyUnitId != null) {
            policy.setId(policyUnitId);
            policyUnitDao.update(policy);
        } else {
            policy.setId(Guid.newGuid());
            policyUnitDao.save(policy);
        }
    }

    private PolicyUnit createFromDiscoveryUnit(ExternalSchedulerDiscoveryUnit discoveryUnit, PolicyUnitType type) {
        PolicyUnit policy = new PolicyUnit();
        policy.setInternal(false);
        policy.setName(discoveryUnit.getName());
        policy.setPolicyUnitType(type);
        policy.setDescription(discoveryUnit.getDescription());
        if (!StringUtils.isEmpty(discoveryUnit.getRegex())) {
            policy.setParameterRegExMap(SimpleCustomPropertiesUtil.getInstance()
                    .convertProperties(discoveryUnit.getRegex()));
        } else {
            policy.setParameterRegExMap(new LinkedHashMap<>());
        }
        return policy;
    }

}
