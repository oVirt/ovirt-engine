package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetStorageDomainStatsVDSCommand<P extends GetStorageDomainStatsVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    private OneStorageDomainStatsReturnForXmlRpc _result;

    public GetStorageDomainStatsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getStorageDomainStats(getParameters().getStorageDomainId().toString());
        ProceedProxyReturnValue();
        storage_domains domain = BuildStorageDynamicFromXmlRpcStruct(_result.mStorageStats);
        domain.setid(getParameters().getStorageDomainId());
        setReturnValue(domain);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    public static storage_domains BuildStorageDynamicFromXmlRpcStruct(XmlRpcStruct xmlRpcStruct) {
        try {
            storage_domains domain = new storage_domains();
            if (xmlRpcStruct.contains("status")) {
                if (StringHelper.EqOp(xmlRpcStruct.getItem("status").toString(), "Attached")) {
                    domain.setstatus(StorageDomainStatus.InActive);
                } else {
                    domain.setstatus(EnumUtils.valueOf(StorageDomainStatus.class, xmlRpcStruct.getItem("status")
                            .toString(), true));
                }
            }
            Long size = IrsBrokerCommand.AssignLongValue(xmlRpcStruct, "diskfree");
            domain.setavailable_disk_size((size == null) ? null : (int) (size / IrsBrokerCommand.BYTES_TO_GB));
            size = IrsBrokerCommand.AssignLongValue(xmlRpcStruct, "disktotal");
            domain.setused_disk_size((size == null || domain.getavailable_disk_size() == null) ? null :
                (int) (size / IrsBrokerCommand.BYTES_TO_GB) - domain.getavailable_disk_size());
            if (xmlRpcStruct.contains("alerts")) {
                Object[] rawAlerts = (Object[]) xmlRpcStruct.getItem("alerts");
                Set<VdcBllErrors> alerts = new HashSet<VdcBllErrors>(rawAlerts.length);

                for (Object rawAlert : rawAlerts) {
                    XmlRpcStruct alert = new XmlRpcStruct((Map<String, Object>) rawAlert);
                    Integer alertCode = (Integer) alert.getItem("code");
                    if (alertCode == null || VdcBllErrors.forValue(alertCode) == null) {
                        log.warnFormat("Unrecognized alert code: {0}.", alertCode);
                        StringBuilder alertStringBuilder = new StringBuilder();
                        XmlRpcObjectDescriptor.ToStringBuilder(alert, alertStringBuilder);
                        log.infoFormat("The received alert is: {0}", alertStringBuilder.toString());
                    } else {
                        alerts.add(VdcBllErrors.forValue(alertCode));
                    }
                }

                domain.setAlerts(alerts);
            }
            return domain;
        } catch (RuntimeException ex) {
            log.errorFormat(
                    "vdsBroker::BuildStorageDynamicFromXmlRpcStruct::Failed building Storage dynamic, xmlRpcStruct = {0}",
                    xmlRpcStruct.toString());
            VDSErrorException outEx = new VDSErrorException(ex);
            log.error(outEx);
            throw outEx;
        }
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    private static LogCompat log = LogFactoryCompat.getLog(GetStorageDomainStatsVDSCommand.class);
}
