package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AddImageFromImportParameters;
import org.ovirt.engine.core.common.vdscommands.ImportCandidateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

@InternalCommandAttribute
public class AddImageFromImportCommand<T extends AddImageFromImportParameters> extends AddImageFromScratchCommand<T> {
    public AddImageFromImportCommand(T parameters) {
        super(parameters);
    }

    protected VDSReturnValue ResourceManagerImport() {
        java.util.HashMap<String, Guid> templateImagesIDs = new java.util.HashMap<String, Guid>();
        templateImagesIDs.put("someDrive", Guid.Empty);
        return Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.ImportCandidate,
                        new ImportCandidateVDSCommandParameters(getVm().getstorage_pool_id(), Guid.Empty,
                                getImageGroupId(), getParameters().getCandidateID(), Guid.Empty, templateImagesIDs,
                                getParameters().getSource(), getParameters().getPath(), getParameters().getForce()));
    }

    @Override
    protected boolean ProcessImageInIrs() {
        VDSReturnValue ret = ResourceManagerImport();

        if (!ret.getSucceeded()) {
            String errorMessage = String.format(
                    "ImagesHandler::AddImageFromImportCommand::ProcessImageInIrs: ImportCandidate "
                            + "didn't succeed for Candidate ID %1$s, Source %2$s, Path %3$s", getParameters()
                            .getCandidateID(), getParameters().getSource(), getParameters().getPath());
            log.error(errorMessage);
        }

        return ret.getSucceeded();
    }

    private static LogCompat log = LogFactoryCompat.getLog(AddImageFromImportCommand.class);
}
