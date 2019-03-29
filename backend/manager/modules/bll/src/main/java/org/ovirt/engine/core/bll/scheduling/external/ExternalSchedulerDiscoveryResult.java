package org.ovirt.engine.core.bll.scheduling.external;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExternalSchedulerDiscoveryResult {
    private static final Logger log = LoggerFactory.getLogger(ExternalSchedulerDiscoveryResult.class);
    private static final String FILTERS = "filters";
    private static final String SCORES = "scores";
    private static final String BALANCE = "balance";
    private List<ExternalSchedulerDiscoveryUnit> filters;
    private List<ExternalSchedulerDiscoveryUnit> scores;
    private List<ExternalSchedulerDiscoveryUnit> balance;

    ExternalSchedulerDiscoveryResult() {
        filters = new LinkedList<>();
        scores = new LinkedList<>();
        balance = new LinkedList<>();
    }

    public boolean populate(Object xmlRpcRawResult) {
        try {
        if (!(xmlRpcRawResult instanceof HashMap)) {
            log.error("External scheduler error, malformed discover results");
            return false;
        }
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object[]>> castedResult = (Map<String, Map<String, Object[]>>) xmlRpcRawResult;

        // keys will be filter, score and balance
        for (Map.Entry<String, Map<String, Object[]>> entry : castedResult.entrySet()) {
            String type = entry.getKey();
            Map<String, Object[]> typeMap = entry.getValue();
                List<ExternalSchedulerDiscoveryUnit> currentList = getRelevantList(type);
                if (currentList == null) {
                    log.error("External scheduler error, got unknown type");
                    return false;
                }
                // list of module names as keys and [description, regex] as value
                for (Map.Entry<String, Object[]> module: typeMap.entrySet()) {
                    String moduleName = module.getKey();
                    Object[] singleModule = module.getValue();
                    // check custom properties format.
                    String customPropertiesRegex = singleModule[1].toString();
                    if (!StringUtils.isEmpty(customPropertiesRegex) && SimpleCustomPropertiesUtil.getInstance()
                            .syntaxErrorInProperties(customPropertiesRegex)) {
                        log.error("Module '{}' will not be loaded, wrong custom properties format ({})",
                                moduleName,
                                customPropertiesRegex);
                        continue;
                    }
                    ExternalSchedulerDiscoveryUnit currentUnit = new ExternalSchedulerDiscoveryUnit(moduleName,
                            singleModule[0].toString(),
                            customPropertiesRegex);
                    currentList.add(currentUnit);
                }
        }
        return true;
        } catch (Exception e) {
            log.error("External scheduler error, exception while parsing discovery results: {}", e.getMessage());
            log.debug("Exception", e);
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
