package org.ovirt.engine.core.bll.scheduling.external;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;


public class ExternalSchedulerDiscoveryResult {
    private static Log log = LogFactory.getLog(ExternalSchedulerDiscoveryResult.class);
    private static final String FILTERS = "filters";
    private static final String SCORES = "scores";
    private static final String BALANCE = "balance";
    private List<ExternalSchedulerDiscoveryUnit> filters;
    private List<ExternalSchedulerDiscoveryUnit> scores;
    private List<ExternalSchedulerDiscoveryUnit> balance;

    ExternalSchedulerDiscoveryResult() {
        filters = new LinkedList<ExternalSchedulerDiscoveryUnit>();
        scores = new LinkedList<ExternalSchedulerDiscoveryUnit>();
        balance = new LinkedList<ExternalSchedulerDiscoveryUnit>();
    }

    public boolean populate(Object xmlRpcRawResult) {
        try {
        if (xmlRpcRawResult == null || !(xmlRpcRawResult instanceof HashMap)) {
            log.error("External scheduler error, malformed discover results");
            return false;
        }
        @SuppressWarnings("unchecked")
        HashMap<String, HashMap<String, Object[]>> castedResult = (HashMap<String, HashMap<String, Object[]>>) xmlRpcRawResult;

        // keys will be filter, score and balance
        for (String type : castedResult.keySet()) {
            HashMap<String, Object[]> typeMap = castedResult.get(type);
                List<ExternalSchedulerDiscoveryUnit> currentList = getRelevantList(type);
                if (currentList == null) {
                    log.error("External scheduler error, got unknown type");
                    return false;
                }
            // list of module names as keys and [description, regex] as value
            for (String moduleName : typeMap.keySet()) {
                Object[] singleModule = typeMap.get(moduleName);
                    ExternalSchedulerDiscoveryUnit currentUnit = new ExternalSchedulerDiscoveryUnit(moduleName,
                        singleModule[0].toString(),
                        singleModule[1].toString());
                    currentList.add(currentUnit);
            }
        }
        return true;
        } catch (Exception e) {
            log.error("External scheduler error, exception why parsing discovery results", e);
            return false;
        }
    }

    private List<ExternalSchedulerDiscoveryUnit> getRelevantList(String type) {
        switch (type) {
        case FILTERS:
            return filters;
        case SCORES:
            return scores;
        case BALANCE:
            return balance;
        default:
            return null;
        }
    }

    List<ExternalSchedulerDiscoveryUnit> getFilters() {
        return filters;
    }

    void setFilters(List<ExternalSchedulerDiscoveryUnit> filters) {
        this.filters = filters;
    }

    List<ExternalSchedulerDiscoveryUnit> getScores() {
        return scores;
    }

    void setScores(List<ExternalSchedulerDiscoveryUnit> scores) {
        this.scores = scores;
    }

    List<ExternalSchedulerDiscoveryUnit> getBalance() {
        return balance;
    }

    void setBalance(List<ExternalSchedulerDiscoveryUnit> balance) {
        this.balance = balance;
    }
}
