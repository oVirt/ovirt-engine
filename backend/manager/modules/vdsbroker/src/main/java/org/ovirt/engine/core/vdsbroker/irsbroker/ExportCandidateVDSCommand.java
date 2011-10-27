package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class ExportCandidateVDSCommand<P extends ExportCandidateVDSCommandParameters> extends IrsBrokerCommand<P> {
    private OneUuidReturnForXmlRpc _retUUID;

    public ExportCandidateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        // NOTE: The IrsProxy doesn't handle multi-drive yet, so we
        // choose a drive randomly and send only its parameters:
        // LINQ 29456
        // string randomDrive = ExportParameters.ListOfImages.Keys.ToList()[0];
        String randomDrive = (getParameters().getListOfImages().keySet().toArray(new String[0]))[0];
        // LINQ 29456

        // LINQ 29456
        // ExportParameters.ListOfImages[randomDrive].Select<Guid, string>(a =>
        // a.toString()).ToArray(),
        Guid[] imagesForDrive = getParameters().getListOfImages().get(randomDrive).toArray(new Guid[0]);
        String[] volumesList = new String[imagesForDrive.length];
        for (int i = 0; i < imagesForDrive.length; i++) {
            volumesList[i] = imagesForDrive[i].toString();
        }
        // LINQ 29456

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
