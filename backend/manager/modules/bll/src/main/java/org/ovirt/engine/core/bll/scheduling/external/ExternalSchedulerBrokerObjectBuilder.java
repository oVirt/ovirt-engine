package org.ovirt.engine.core.bll.scheduling.external;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalSchedulerBrokerObjectBuilder {
    private static final Logger log = LoggerFactory.getLogger(ExternalSchedulerBrokerObjectBuilder.class);
    private static final int RESULT_OK = 0;
    private static final AuditLogDirector auditLogDirector = Injector.get(AuditLogDirector.class);

    private static void auditLogPluginError(String pluginName, String errorMessage) {
        AuditLogable loggable = new AuditLogableImpl();

        loggable.addCustomValue("PluginName", pluginName);
        loggable.addCustomValue("ErrorMessage", errorMessage);

        auditLogDirector.log(loggable, AuditLogType.EXTERNAL_SCHEDULER_PLUGIN_ERROR);
    }

    private static void auditLogExternalSchedulerError(String errorMessage) {
        AuditLogable loggable = new AuditLogableImpl();
        loggable.addCustomValue("ErrorMessage", errorMessage);
        auditLogDirector.log(loggable, AuditLogType.EXTERNAL_SCHEDULER_ERROR);
    }


    private static Object populateCommonFields(Object xmlRpcStruct, SchedulerResult result) {
        /* new response format
        {
          "result_code": int,
          "result": [list of UUIDS],
          "plugin_errors": { "plugin_name": ["errormsgs"] },
          "errors": ["errormsgs"]
        }
        */

        if (xmlRpcStruct instanceof Object[]) { //old version of ext scheduler?
            log.info("Got old XMLRPC response from external scheduler");
            return xmlRpcStruct;
        } else if (!(xmlRpcStruct instanceof HashMap)) {
            log.error("External scheduler error, malformed filter results");
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> castedResult = (Map<String, Object>) xmlRpcStruct;

        // keys will be status_code, plugin_errors, errors and result
        result.setResultCode((int) castedResult.get("result_code"));
        Map<String, Object[]> plugin_errors;
        Object[] errors;

        if (result.getResultCode() != RESULT_OK) {
            plugin_errors = (Map<String, Object[]>) castedResult.get("plugin_errors");
            errors = (Object[])castedResult.get("errors");

            if (plugin_errors != null) {
                for (Map.Entry<String, Object[]> entry: plugin_errors.entrySet()) {
                    for (Object errorMsg: entry.getValue()) {
                        auditLogPluginError(entry.getKey(), errorMsg.toString());
                        result.addPluginErrors(entry.getKey(), errorMsg.toString());
                    }
                }
            }

            if (errors != null) {
                for (Object msg: errors) {
                    auditLogExternalSchedulerError((String)msg);
                    result.addError((String)msg);
                }
            }
        }

        /* returns just result without any headers
           so it can be passed to the old parsers
         */
        return castedResult.get("result");
    }

    public static FilteringResult getFilteringResult(Object xmlRpcStruct) {
        FilteringResult result = new FilteringResult();
        Object rawResult = populateCommonFields(xmlRpcStruct, result);

        if (rawResult == null) {
            return result;
        }

        // Its a list of host IDs
        for (Object hostID : (Object[]) rawResult) {
            result.addHost(new Guid(hostID.toString()));
        }

        return result;
    }

    public static ScoringResult getScoreResult(Object xmlRpcStruct) {
        ScoringResult result = new ScoringResult();
        Object rawResult = populateCommonFields(xmlRpcStruct, result);

        if (rawResult == null) {
            return result;
        }

        // Its a list of (hostID,score) pairs
        for (Object hostsIDAndScore : (Object[]) rawResult) {
            if (!(hostsIDAndScore instanceof Object[])
                    || ((Object[]) hostsIDAndScore).length < 2
                    || ((Object[]) hostsIDAndScore).length > 3) {
                // some kind of error
                log.error("External scheduler error, malformed score results");
                return result;
            }
            Object[] castedHostsIDAndScore = (Object[]) hostsIDAndScore;

            // External scheduler either reports cumulative data (just two fields - host, weight)
            // or per policy unit data (three fields - host, weight, policy unit name)
            String policyUnitId = castedHostsIDAndScore.length > 2 ? castedHostsIDAndScore[2].toString() : null;
            result.addHost(policyUnitId, Guid.createGuidFromString(castedHostsIDAndScore[0].toString()),
                    (Integer) castedHostsIDAndScore[1]);
        }
        return result;
    }

    public static BalanceResult getBalanceResult(Object xmlRpcStruct) {
        BalanceResult result = new BalanceResult();
        Object[] castedRawResult = (Object[]) populateCommonFields(xmlRpcStruct, result);

        if (castedRawResult == null) {
            return result;
        }

        for (Object hostID : (Object[]) castedRawResult[1]) {
            result.addHost(new Guid(hostID.toString()));
        }

        if (!castedRawResult[0].toString().isEmpty()) {
            result.setVmToMigrate(new Guid(castedRawResult[0].toString()));
        }

        return result;
    }

}
