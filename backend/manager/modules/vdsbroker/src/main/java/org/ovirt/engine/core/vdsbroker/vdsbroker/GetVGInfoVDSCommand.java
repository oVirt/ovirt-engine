package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.*;

public class GetVGInfoVDSCommand<P extends GetVGInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    private OneVGReturnForXmlRpc _result;

    public GetVGInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getVGInfo(getParameters().getVGID());
        ProceedProxyReturnValue();
        // build temp data
        Object[] temp = (Object[]) _result.vgInfo.get("pvlist");
        Map<String, Object>[] pvList = new Map[0];
        if (temp != null) {
            pvList = new Map[temp.length];
            for (int i = 0; i < temp.length; i++) {
                pvList[i] = (Map<String, Object>) temp[i];
            }
        }
        setReturnValue(GetDeviceListVDSCommand.ParseLUNList(pvList));
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
