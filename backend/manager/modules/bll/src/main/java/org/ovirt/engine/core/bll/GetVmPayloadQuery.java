package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDAO;


public class GetVmPayloadQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmPayloadQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (MultiLevelAdministrationHandler.isAdminUser(getUser())) {
            VmDeviceDAO dao = getDbFacade().getVmDeviceDao();
            List<VmDevice> disks = dao.getVmDeviceByVmIdAndType(getParameters().getId(), VmDeviceGeneralType.DISK);

            for (VmDevice disk : disks) {
                if (VmPayload.isPayload(disk.getSpecParams())) {
                    VmPayload payload = new VmPayload(VmDeviceType.valueOf(disk.getType().name()),
                            disk.getSpecParams());
                    payload.setType(VmDeviceType.valueOf(disk.getDevice().toUpperCase()));
                    payload.setContent(new String(Base64.decodeBase64(payload.getContent())));

                    getQueryReturnValue().setReturnValue(payload);
                }
            }
        }
    }
}
