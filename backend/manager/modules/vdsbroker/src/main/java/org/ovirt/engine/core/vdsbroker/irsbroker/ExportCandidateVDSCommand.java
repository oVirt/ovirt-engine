package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.ExportCandidateVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class ExportCandidateVDSCommand<P extends ExportCandidateVDSCommandParameters> extends IrsBrokerCommand<P> {
    private OneUuidReturnForXmlRpc _retUUID;

    public ExportCandidateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        // NOTE: The IrsProxy doesn't handle multi-drive yet, so we
        // choose a drive randomly and send only its parameters:
        String randomDrive = (getParameters().getListOfImages().keySet().toArray(new String[0]))[0];

        Guid[] imagesForDrive = getParameters().getListOfImages().get(randomDrive).toArray(new Guid[0]);
        String[] volumesList = new String[imagesForDrive.length];
        for (int i = 0; i < imagesForDrive.length; i++) {
            volumesList[i] = imagesForDrive[i].toString();
        }

        _retUUID = getIrsProxy().exportCandidate(getParameters().getStorageDomainId().toString(),
                getParameters().getVmGUID().toString(), volumesList, getParameters().getVmMeta(),
                getParameters().getVmTemplateGUID().toString(),
                getParameters().getVmTemplateImageGUIDs().get(randomDrive).toString(),
                getParameters().getVmTemplateMeta(), getParameters().getPath(),
                (new Boolean(getParameters().getCollapse())).toString(),
                (new Boolean(getParameters().getForce())).toString());
        ProceedProxyReturnValue();
        setReturnValue(new Guid(_retUUID.mUuid));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _retUUID.mStatus;
    }
}
