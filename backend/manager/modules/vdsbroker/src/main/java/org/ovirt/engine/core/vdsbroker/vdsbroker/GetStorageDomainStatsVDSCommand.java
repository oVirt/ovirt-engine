package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetStorageDomainStatsVDSCommand<P extends GetStorageDomainStatsVDSCommandParameters>
        extends VdsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(GetStorageDomainStatsVDSCommand.class);

    private OneStorageDomainStatsReturn result;

    public GetStorageDomainStatsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().getStorageDomainStats(getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();
        StorageDomain domain = buildStorageDynamicStruct(result.storageStats);
        domain.setId(getParameters().getStorageDomainId());
        setReturnValue(domain);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @SuppressWarnings("unchecked")
    public static StorageDomain buildStorageDynamicStruct(Map<String, Object> struct) {
        try {
            StorageDomain domain = new StorageDomain();
            if (struct.containsKey("status")) {
                if ("Attached".equals(struct.get("status").toString())) {
                    domain.setStatus(StorageDomainStatus.Inactive);
                } else {
                    domain.setStatus(EnumUtils.valueOf(StorageDomainStatus.class, struct.get("status")
                            .toString(), true));
                }
            }
            Long size = IrsBrokerCommand.assignLongValue(struct, "diskfree");
            domain.setAvailableDiskSize((size == null) ? null : (int) (size / SizeConverter.BYTES_IN_GB));
            size = IrsBrokerCommand.assignLongValue(struct, "disktotal");
            domain.setUsedDiskSize((size == null || domain.getAvailableDiskSize() == null) ? null :
                    (int) (size / SizeConverter.BYTES_IN_GB) - domain.getAvailableDiskSize());
            if (struct.containsKey("alerts")) {
                Object[] rawAlerts = (Object[]) struct.get("alerts");
                Set<EngineError> alerts = new HashSet<>(rawAlerts.length);

                for (Object rawAlert : rawAlerts) {
                    Map<String, Object> alert = (Map<String, Object>) rawAlert;
                    Integer alertCode = (Integer) alert.get("code");
                    if (alertCode == null || EngineError.forValue(alertCode) == null) {
                        log.warn("Unrecognized alert code: {}.", alertCode);
                        StringBuilder alertStringBuilder = new StringBuilder();
                        ObjectDescriptor.toStringBuilder(alert, alertStringBuilder);
                        log.info("The received alert is: {}", alertStringBuilder);
                    } else {
                        alerts.add(EngineError.forValue(alertCode));
                    }
                }

                domain.setAlerts(alerts);
            }
            return domain;
        } catch (RuntimeException ex) {
            log.error(
                    "vdsBroker::buildStorageDynamicFromStruct::Failed building Storage dynamic, struct = {}",
                    struct);
            VDSErrorException outEx = new VDSErrorException(ex);
            log.error("Exception", outEx);
            throw outEx;
        }
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
