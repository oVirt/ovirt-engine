package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.ImportCandidateVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class ImportCandidateVDSCommand<P extends ImportCandidateVDSCommandParameters> extends IrsCreateCommand<P> {
    public ImportCandidateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        // NOTE: since the IRS doesn't support in multi-drive VMs,
        // we choose a drive randomly and import only it:
        // LINQ 29456
        // string randomDrive = ImportParameters.BaseImageIDs.Keys.ToList()[0];
        String randomDrive = (getParameters().getBaseImageIDs().keySet().toArray(new String[0]))[0];
        // LINQ 29456
        uuidReturn = getIrsProxy().importCandidate(getParameters().getStorageDomainId().toString(),
                getParameters().getCandidateID(), getParameters().getBaseID().toString(),
                getParameters().getBaseImageIDs().get(randomDrive).toString(),
                StringHelper.trimEnd(getParameters().getImportPath(), '/'),
                ImportEnumsManager.CandidateSourceString(getParameters().getCandidateSource()),
                (new Boolean(getParameters().getForce())).toString().toLowerCase());
        ProceedProxyReturnValue();
        setReturnValue(new Guid(uuidReturn.mUuid));
    }

    @Override
    public void Rollback() {
        for (Guid imageGUID : getParameters().getBaseImageIDs().values()) {
            try {
                // todo - omer sending false for postZero, check that is correct
                // always (and not parameter from user)
                getIrsProxy().deleteVolume(getParameters().getStorageDomainId().toString(),
                                           getParameters().getStoragePoolId().toString(),
                                           getParameters().getImageGroupId().toString(),
                                           new String[] { imageGUID.toString() },
                                           "false",
                                           "false");
            } catch (java.lang.Exception e) {
            }
        }

        BaseRollback();
    }
}
