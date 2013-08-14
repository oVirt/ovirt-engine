package org.ovirt.engine.core.bll.scheduling.external;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ExternalSchedulerBrokerImpl implements ExternalSchedulerBroker {

    private static String DISCOVER = "discover";
    private static String FILTER = "runFilters";
    private static String SCORE = "runCostFunctions";
    private static String BALANCE = "runLoadBalancing";

    private static Object[] EMPTY = new Object[] {};

    private final static Log log = LogFactory.getLog(ExternalSchedulerBrokerImpl.class);

    private XmlRpcClientConfigImpl config = null;

    public ExternalSchedulerBrokerImpl() {
        String extSchedUrl = Config.GetValue(ConfigValues.ExternalSchedulerServiceURL);
        config = new XmlRpcClientConfigImpl();
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout((Integer) Config.GetValue(ConfigValues.ExternalSchedulerConnectionTimeout));
        config.setReplyTimeout((Integer) Config.GetValue(ConfigValues.ExternalSchedulerResponseTimeout));
        try {
            config.setServerURL(new URL(extSchedUrl));
        } catch (MalformedURLException e) {
            log.error("External scheduler got bad url", e);
        }
    }

    @Override
    public ExternalSchedulerDiscoveryResult runDiscover() {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object result = client.execute(DISCOVER, EMPTY);
            return parseDiscoverResults(result);

        } catch (XmlRpcException e) {
            log.error("Could not communicate with the external scheduler while discovering", e);
            return null;
        }
    }

    private ExternalSchedulerDiscoveryResult parseDiscoverResults(Object result) {
        ExternalSchedulerDiscoveryResult retValue = new ExternalSchedulerDiscoveryResult();
        if (!retValue.populate(result)) {
            return null;
        }
        return retValue;
    }

    @Override
    public List<Guid> runFilters(List<String> filterNames,
            List<Guid> hostIDs,
            Guid vmID,
            Map<String, String> propertiesMap) {

        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object result = client.execute(FILTER, createFilterArgs(filterNames, hostIDs, vmID, propertiesMap));
            return parseFilterResults(result);

        } catch (XmlRpcException e) {
            log.error("Could not communicate with the external scheduler while filtering", e);
            return hostIDs;
        }
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

    private List<Guid> parseFilterResults(Object result) {
        if (!(result instanceof Object[])) {
            log.error("External scheduler error, malformed filter results");
            return null;
        }
        // Its a list of host IDs
        List<Guid> retValue = new LinkedList<Guid>();
        for (Object hostID : (Object[]) result) {
            retValue.add(new Guid(hostID.toString()));
        }
        return retValue;
    }

    @Override
    public List<Pair<Guid, Integer>> runScores(List<Pair<String, Integer>> scoreNameAndWeight,
            List<Guid> hostIDs,
            Guid vmID,
            Map<String, String> propertiesMap) {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object result = client.execute(SCORE, createScoreArgs(scoreNameAndWeight, hostIDs, vmID, propertiesMap));
            return parseScoreResults(result);

        } catch (XmlRpcException e) {
            log.error("Could not communicate with the external scheduler while running weight modules", e);
            return null;
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

    private List<Pair<Guid, Integer>> parseScoreResults(Object result) {
        if (!(result instanceof Object[])) {
            log.error("External scheduler error, malformed score results");
            return null;
        }
        List<Pair<Guid, Integer>> retValue = new LinkedList<Pair<Guid, Integer>>();
        // Its a list of (hostID,score) pairs
        for (Object hostsIDAndScore : (Object[]) result) {
            if (!(hostsIDAndScore instanceof Object[]) || ((Object[]) hostsIDAndScore).length != 2) {
                // some kind of error
                log.error("External scheduler error, malformed score results");
                return null;
            }
            Object[] castedHostsIDAndScore = (Object[]) hostsIDAndScore;
            Pair<Guid, Integer> pair = new Pair<Guid, Integer>();
            pair.setFirst(new Guid(castedHostsIDAndScore[0].toString()));
            pair.setSecond((Integer) castedHostsIDAndScore[1]);
            retValue.add(pair);
        }
        return retValue;
    }

    @Override
    public Pair<List<Guid>, Guid> runBalance(String balanceName, List<Guid> hostIDs, Map<String, String> propertiesMap) {
        // TODO Auto-generated method stub
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object result =
                    client.execute(BALANCE, createBalanceArgs(balanceName, hostIDs, propertiesMap));
            return parseBalanceResults(result);

        } catch (XmlRpcException e) {
            log.error("Could not communicate with the external scheduler while balancing", e);
            return null;
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

    private Pair<List<Guid>, Guid> parseBalanceResults(Object result) {
        if (!(result instanceof Object[])) {
            log.error("External scheduler error, malformed balance results");
            return null;
        }
        Object[] castedResult = (Object[]) result;

        List<Guid> hostIDs = new LinkedList<Guid>();
        for (Object hostID : (Object[]) castedResult[1]) {
            hostIDs.add(new Guid(hostID.toString()));
        }
        Pair<List<Guid>, Guid> retValue = new Pair<List<Guid>, Guid>();
        retValue.setFirst(hostIDs);
        retValue.setSecond(new Guid(castedResult[0].toString()));

        return retValue;
    }
}
