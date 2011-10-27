package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetVGInfoVDSCommand<P extends GetVGInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    private OneVGReturnForXmlRpc _result;

    public GetVGInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getVGInfo(getParameters().getVGID());
        ProceedProxyReturnValue();
        // build temp data
        Object[] temp = (Object[]) _result.vgInfo.getItem("pvlist");
        XmlRpcStruct[] pvList = new XmlRpcStruct[0];
        if (temp != null) {
            pvList = new XmlRpcStruct[temp.length];
            for (int i = 0; i < temp.length; i++) {
                pvList[i] = new XmlRpcStruct((Map<String, Object>) temp[i]);
            }
        }
        setReturnValue(GetDeviceListVDSCommand.ParseLUNList(pvList));
        // ReturnValue = ((string[])_result.vgInfo["pvlist"]).ToList();
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
