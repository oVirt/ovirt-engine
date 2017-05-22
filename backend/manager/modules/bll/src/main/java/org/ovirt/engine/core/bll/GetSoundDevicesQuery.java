package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetSoundDevicesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDeviceDao vmDeviceDao;

    public GetSoundDevicesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmDevice> soundDevList =
                vmDeviceDao.getVmDeviceByVmIdAndType(getParameters().getId(), VmDeviceGeneralType.SOUND);

        List<String> result = new ArrayList<>(soundDevList.size());
        for (VmDevice v : soundDevList) {
            result.add(v.getDevice());
        }
        getQueryReturnValue().setReturnValue(result);
    }
}
