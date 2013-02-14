package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

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

    protected java.util.ArrayList<StorageDomain> ParseVGList(XmlRpcStruct[] vgList) {
        java.util.ArrayList<StorageDomain> result = new java.util.ArrayList<StorageDomain>(vgList.length);
        for (XmlRpcStruct vg : vgList) {
            StorageDomain sDomain = new StorageDomain();
            if (vg.contains("name")) {
                try {
                    sDomain.setId(new Guid(vg.getItem("name").toString()));
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
        }
        return result;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
