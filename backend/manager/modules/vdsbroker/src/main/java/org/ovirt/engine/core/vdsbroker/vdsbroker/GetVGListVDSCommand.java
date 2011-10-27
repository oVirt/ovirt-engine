package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.*;

public class GetVGListVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected VGListReturnForXmlRpc _result;

    public GetVGListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getVGList();
        ProceedProxyReturnValue();
        setReturnValue(ParseVGList(_result.vgList));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    protected java.util.ArrayList<storage_domains> ParseVGList(XmlRpcStruct[] vgList) {
        java.util.ArrayList<storage_domains> result = new java.util.ArrayList<storage_domains>(vgList.length);
        for (XmlRpcStruct vg : vgList) {
            try {
                storage_domains sDomain = new storage_domains();
                if (vg.contains("name")) {
                    try {
                        sDomain.setid(new Guid(vg.getItem("name").toString()));
                    } catch (java.lang.Exception e) {
                        sDomain.setstorage_name(vg.getItem("name").toString());
                    }
                }
                sDomain.setstorage(vg.getItem("vgUUID").toString());
                Long size = IrsBrokerCommand.AssignLongValue(vg, "vgfree");
                if (size != null) {
                    sDomain.setavailable_disk_size((int) (size / IrsBrokerCommand.BYTES_TO_GB));
                }
                size = IrsBrokerCommand.AssignLongValue(vg, "vgsize");
                if (size != null && sDomain.getavailable_disk_size() != null) {
                    sDomain.setused_disk_size((int) (size / IrsBrokerCommand.BYTES_TO_GB)
                            - sDomain.getavailable_disk_size());
                }
                if (vg.containsKey("vgtype")) {
                    sDomain.setstorage_type(EnumUtils.valueOf(StorageType.class, vg.getItem("vgtype").toString(), true));
                } else {
                    sDomain.setstorage_type(StorageType.ALL);
                }
                result.add(sDomain);
            } catch (RuntimeException ex) {
                log.errorFormat("irsBroker::ParseVGList::Failed building Storage domain, xmlRpcStruct = {0}",
                        vg.toString());
                IRSErrorException outEx = new IRSErrorException(ex);
                log.error(outEx);
                throw outEx;
            }
        }
        return result;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    private static LogCompat log = LogFactoryCompat.getLog(GetVGListVDSCommand.class);
}
