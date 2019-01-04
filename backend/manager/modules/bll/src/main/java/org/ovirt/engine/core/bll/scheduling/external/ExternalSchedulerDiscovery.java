package org.ovirt.engine.core.bll.scheduling.external;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ExternalSchedulerDiscovery {

    private static final Logger log = LoggerFactory.getLogger(ExternalSchedulerDiscovery.class);

    @Inject
    private PolicyUnitDao policyUnitDao;

    @Inject
    ExternalSchedulerBroker broker;

    @Inject
    private AuditLogDirector auditLogDirector;

    protected ExternalSchedulerDiscovery() {}

    /**
     * Discover external schedulers and process its policy units. This operation may take time and is recommended to run
     * in a different thread. If we found new policy units we save them to db and expose them for usage.
     *
     * @return {@code true} if new policies where found and saved to db, {@code false} otherwise.
     */
    public boolean discover() {
        boolean dbUpdated;

        Optional<ExternalSchedulerDiscoveryResult> discoveryResult = broker.runDiscover();
        if (discoveryResult.isPresent()) {
            updateDB(discoveryResult.get());
            log.debug("PolicyUnits updated for external broker.");
            dbUpdated = true;
        } else {
            AuditLogable loggable = new AuditLogableImpl();
            auditLogDirector.log(loggable, AuditLogType.FAILED_TO_CONNECT_TO_SCHEDULER_PROXY);
            log.warn("Discovery returned empty result when talking to broker. Disabling external units");

            List<PolicyUnit> failingPolicyUnits = policyUnitDao.getAll();
            markExternalPoliciesAsDisabled(failingPolicyUnits);
            dbUpdated = true;
        }

        return dbUpdated;
    }

    private void updateDB(ExternalSchedulerDiscoveryResult discoveryResult) {
        List<PolicyUnit> allPolicyUnits = policyUnitDao.getAll();
        Set<PolicyUnit> foundInBoth = new HashSet<>();

        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getFilters()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.FILTER);
            foundInBoth.add(found);
        }
        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getScores()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.WEIGHT);
            foundInBoth.add(found);
        }
        for (ExternalSchedulerDiscoveryUnit unit : discoveryResult.getBalance()) {
            PolicyUnit found = compareToDB(allPolicyUnits, unit, PolicyUnitType.LOAD_BALANCING);
            foundInBoth.add(found);
        }

        // found in the db for the current broker, but not found in discovery, mark as such
        markExternalPoliciesAsDisabled(allPolicyUnits.stream()
                .filter(unit -> !foundInBoth.contains(unit))
                .collect(Collectors.toList()));
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

    private PolicyUnit compareToDB(List<PolicyUnit> dbEntries,
                                   ExternalSchedulerDiscoveryUnit discoveryUnit,
                                   PolicyUnitType type) {
        for (PolicyUnit policyUnit : dbEntries) {
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
                sendToDb(discoveryUnit, policyUnit, type);
            }

            return policyUnit;
        }

        return sendToDb(discoveryUnit, null, type);
    }

    private PolicyUnit sendToDb(ExternalSchedulerDiscoveryUnit discovery,
            /* Nullable */PolicyUnit policyUnit, PolicyUnitType type) {
        PolicyUnit policy = createFromDiscoveryUnit(discovery, type);

        if (policyUnit != null) {
            log.warn("Policy unit {} already reported by broker.", policyUnit.getName());
        }

        if (policyUnit != null && policyUnit.getId() != null) {
            policy.setId(policyUnit.getId());
            policyUnitDao.update(policy);
        } else {
            policy.setId(Guid.newGuid());
            policyUnitDao.save(policy);
        }

        return policy;
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
