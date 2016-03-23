package org.ovirt.engine.core.bll.exportimport;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmFromOvaParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromOvaCommand<T extends ImportVmFromOvaParameters> extends ImportVmFromExternalProviderCommand<T> {

    public ImportVmFromOvaCommand(Guid cmdId) {
        super(cmdId);
    }

    public ImportVmFromOvaCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void convert() {
        CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.ConvertOva,
                buildConvertOvaParameters(),
                cloneContextAndDetachFromParent());
    }

    private ConvertOvaParameters buildConvertOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmName());
        parameters.setDisks(getDisks());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        parameters.setVirtioIsoName(getParameters().getVirtioIsoName());
        return parameters;
    }
}
