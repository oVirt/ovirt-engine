package org.ovirt.engine.core.bll.scheduling.external;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ExternalSchedulerBrokerImpl implements ExternalSchedulerBroker {

    private static final String DISCOVER = "discover";
    private static final String FILTER = "runFilters";
    private static final String SCORE = "runCostFunctions";
    private static final String BALANCE = "runLoadBalancing";

    private static final Object[] EMPTY = new Object[] {};

    private static final Logger log = LoggerFactory.getLogger(ExternalSchedulerBrokerImpl.class);

    private XmlRpcClientConfigImpl config;

    @Inject
    private AuditLogDirector auditLogDirector;

    public ExternalSchedulerBrokerImpl() {
        String extSchedUrl = Config.getValue(ConfigValues.ExternalSchedulerServiceURL);
        config = new XmlRpcClientConfigImpl();
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(Config.getValue(ConfigValues.ExternalSchedulerConnectionTimeout));
        config.setReplyTimeout(Config.getValue(ConfigValues.ExternalSchedulerResponseTimeout));
        try {
            config.setServerURL(new URL(extSchedUrl));
        } catch (MalformedURLException e) {
            log.error("External scheduler got bad url: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }

    @Override
    public Optional<ExternalSchedulerDiscoveryResult> runDiscover() {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object result = client.execute(DISCOVER, EMPTY);
            return parseDiscoverResults(result);

        } catch (XmlRpcException e) {
            log.error("Error communicating with the external scheduler while discovering: {}", e.getMessage());
            log.debug("Exception", e);
            return Optional.empty();
        }
    }

    private Optional<ExternalSchedulerDiscoveryResult> parseDiscoverResults(Object result) {
        ExternalSchedulerDiscoveryResult retValue = new ExternalSchedulerDiscoveryResult();
        if (!retValue.populate(result)) {
            return Optional.empty();
        }
        return Optional.of(retValue);
    }

    @Override
    public List<Guid> runFilters(List<String> filterNames,
            List<Guid> hostIDs,
            Guid vmID,
            Map<String, String> propertiesMap) {
        try {
            // Do not call the scheduler when there is no operation requested from it
            if (filterNames.isEmpty()) {
                return hostIDs;
            }

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object xmlRpcStruct = client.execute(FILTER, createFilterArgs(filterNames, hostIDs, vmID, propertiesMap));
            return ExternalSchedulerBrokerObjectBuilder.getFilteringResult(xmlRpcStruct).getHosts();

        } catch (XmlRpcException e) {
            log.error("Error communicating with the external scheduler while filtering: {}", e.getMessage());
            log.debug("Exception", e);
            auditLogFailedToConnect();
            return hostIDs;
        }
    }

    private void auditLogFailedToConnect() {
        AuditLogable loggable = new AuditLogableImpl();
        auditLogDirector.log(loggable, AuditLogType.FAILED_TO_CONNECT_TO_SCHEDULER_PROXY);
    }

    private Object[] createFilterArgs(List<String> filterNames,
            List<Guid> hostIDs,
            Guid vmID,
            Map<String, String> propertiesMap) {
        Object[] sentObject = new Object[4];
        // filters name
        sentObject[0] = filterNames;
        // hosts ids
        String[] arr = new String[hostIDs.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = hostIDs.get(i).toString();
        }
        sentObject[1] = arr;
        // vm id
        sentObject[2] = vmID.toString();
        // additional args
        sentObject[3] = propertiesMap;
        return sentObject;
    }

    @Override
    public List<WeightResultEntry> runScores(List<Pair<String, Integer>> scoreNameAndWeight,
            List<Guid> hostIDs,
            Guid vmID,
            Map<String, String> propertiesMap) {
        try {
            // Do not call the scheduler when there is no operation requested from it
            if (scoreNameAndWeight.isEmpty()) {
                return Collections.emptyList();
            }

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object result = client.execute(SCORE, createScoreArgs(scoreNameAndWeight, hostIDs, vmID, propertiesMap));
            return ExternalSchedulerBrokerObjectBuilder.getScoreResult(result).getHosts();

        } catch (XmlRpcException e) {
            log.error("Error communicating with the external scheduler while running weight modules: {}",
                    e.getMessage());
            log.debug("Exception", e);
            auditLogFailedToConnect();
            return Collections.emptyList();
        }
    }

    private Object[] createScoreArgs(List<Pair<String, Integer>> scoreNameAndWeight,
            List<Guid> hostIDs,
            Guid vmID,
            Map<String, String> propertiesMap) {
        Object[] sentObject = new Object[4];

        Object[] pairs = new Object[scoreNameAndWeight.size()];

        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = new Object[] { scoreNameAndWeight.get(i).getFirst(), scoreNameAndWeight.get(i).getSecond() };
        }
        // score name + weight pairs
        sentObject[0] = pairs;
        // hosts ids
        String[] arr = new String[hostIDs.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = hostIDs.get(i).toString();
        }
        sentObject[1] = arr;
        // vm id
        sentObject[2] = vmID.toString();
        // additional args
        sentObject[3] = propertiesMap;

        return sentObject;
    }


    @Override
    public Optional<BalanceResult> runBalance(String balanceName, List<Guid> hostIDs, Map<String, String> propertiesMap) {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object result =
                    client.execute(BALANCE, createBalanceArgs(balanceName, hostIDs, propertiesMap));
            return Optional.of(ExternalSchedulerBrokerObjectBuilder.getBalanceResult(result));

        } catch (XmlRpcException e) {
            log.error("Error communicating with the external scheduler while balancing: {}", e.getMessage());
            log.debug("Exception", e);
            auditLogFailedToConnect();
            return Optional.empty();
        }
    }

    private Object[] createBalanceArgs(String balanceName, List<Guid> hostIDs, Map<String, String> propertiesMap) {
        Object[] sentObject = new Object[3];
        // balance name
        sentObject[0] = balanceName;
        // hosts ids
        String[] arr = new String[hostIDs.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = hostIDs.get(i).toString();
        }
        sentObject[1] = arr;
        // additional args
        sentObject[2] = propertiesMap;

        return sentObject;
    }
}
