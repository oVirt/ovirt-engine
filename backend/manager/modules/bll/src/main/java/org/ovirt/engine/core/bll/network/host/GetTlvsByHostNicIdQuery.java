package org.ovirt.engine.core.bll.network.host;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.LldpInfo;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetLldpVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class GetTlvsByHostNicIdQuery<P extends IdQueryParameters> extends AbstractGetTlvsQuery<P> {

    private VdsNetworkInterface nic;

    @Inject
    private InterfaceDao interfaceDao;

    public GetTlvsByHostNicIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = interfaceDao.get(getParameters().getId());
        }
        return nic;
    }

    @Override
    protected void executeQueryCommand() {
        String interfaceName = getNic().getName();
        setLldpVDSCommandParameters(new GetLldpVDSCommandParameters(getNic().getVdsId(),
                new String[] { interfaceName }));

        super.executeQueryCommand();

        Map<String, LldpInfo> lldpInfos = getQueryReturnValue().getReturnValue();
        if (lldpInfos != null) {
            LldpInfo lldpInfo = lldpInfos.get(interfaceName);
            getQueryReturnValue().setReturnValue(lldpInfo.isEnabled() ? lldpInfo.getTlvs() : null);
        }
    }

    @Override
    protected boolean validateInputs() {
        if (!super.validateInputs()) {
            return false;
        }

        if (getParameters().getId() == null) {
            getQueryReturnValue().setExceptionString(EngineMessage.NIC_ID_IS_NULL.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        if (getNic() == null) {
            getQueryReturnValue().setExceptionString(EngineMessage.NIC_ID_NOT_EXIST.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        if (!(getNic() instanceof Nic)) {
            getQueryReturnValue().setExceptionString(EngineMessage.INTERFACE_TYPE_NOT_SUPPORT_LLDP.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        return true;
    }

    @Override
    protected Guid getHostId() {
        return getNic().getVdsId();
    }
}
