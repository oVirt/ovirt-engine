package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class GetVGInfoVDSCommand<P extends GetVGInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    private OneVGReturn _result;

    @Inject
    private StoragePoolDao storagePoolDao;

    public GetVGInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().getVGInfo(getParameters().getVGID());
        proceedProxyReturnValue();
        // build temp data
        String vgName = (String) _result.vgInfo.get("name");
        Object[] temp = (Object[]) _result.vgInfo.get("pvlist");
        Map<String, Object>[] pvList = new Map[0];
        if (temp != null) {
            pvList = new Map[temp.length];
            for (int i = 0; i < temp.length; i++) {
                pvList[i] = (Map<String, Object>) temp[i];
                pvList[i].put("vgName", vgName);
            }
        }
        Version compatibilityVersion = storagePoolDao.getForVds(getParameters().getVdsId()).getCompatibilityVersion();
        setReturnValue(GetDeviceListVDSCommand.parseLUNList(pvList, compatibilityVersion));
    }

    @Override
    protected Status getReturnStatus() {
        return _result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
