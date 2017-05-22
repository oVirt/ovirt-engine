package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

@NonTransactiveCommandAttribute
public class GetGlusterHostPublicKeysQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterHostPublicKeysQuery(P params, EngineContext engineContext) {
        super(params, engineContext);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeQueryCommand() {
        VDSReturnValue readPubKeyReturnValue =
                runVdsCommand(VDSCommandType.GetGlusterHostsPubKey,
                        new VdsIdVDSCommandParametersBase(getParameters().getId()));
        getQueryReturnValue().setReturnValue(readPubKeyReturnValue.getReturnValue());
    }

}
