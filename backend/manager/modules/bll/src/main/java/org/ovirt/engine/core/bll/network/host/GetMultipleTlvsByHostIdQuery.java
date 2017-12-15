package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetLldpVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetMultipleTlvsByHostIdQuery<P extends IdQueryParameters> extends AbstractGetTlvsQuery<P> {

    public GetMultipleTlvsByHostIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String[] names = new String[0];
        setLldpVDSCommandParameters(new GetLldpVDSCommandParameters(getHostId(), names));
        super.executeQueryCommand();
    }

    @Override protected boolean validateInputs() {
        if (!super.validateInputs()) {
            return false;
        }

        if (getHostId() == null) {
            getQueryReturnValue().setExceptionString(EngineMessage.HOST_ID_IS_NULL.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        if (getHost() == null) {
            getQueryReturnValue().setExceptionString(EngineMessage.VDS_INVALID_SERVER_ID.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        return true;
    }

    @Override
    protected Guid getHostId() {
        return getParameters().getId();
    }
}
