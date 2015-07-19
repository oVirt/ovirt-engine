package org.ovirt.engine.core.bll;

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

    protected ImportVmFromOvaCommand(T parameters) {
        super(parameters);
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
        parameters.setVdsGroupId(getVdsGroupId());
        parameters.setVirtioIsoName(getParameters().getVirtioIsoName());
        return parameters;
    }
}
