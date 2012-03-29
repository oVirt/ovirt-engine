package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.AddImagesFromImportParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@InternalCommandAttribute
public class AddTemplateImagesFromImportCommand<T extends AddImagesFromImportParameters> extends
        AddImagesFromImportCommand<T> {
    public AddTemplateImagesFromImportCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        ProcessImageInIrs();

        for (List<DiskImage> importedImagesPerDrive : getParameters().getImportedImages().values()) // foreach
                                                                                                    // drive:
        {
            for (DiskImage importedImage : importedImagesPerDrive) // foreach
                                                                   // image in
                                                                   // current
                                                                   // drive
                                                                   // (should
                                                                   // be only
                                                                   // one):
            {
                try {
                    importedImage.setimageStatus(ImageStatus.LOCKED);
                    saveImage(importedImage);
                    saveDiskIfNotExists(importedImage);
                }

                catch (RuntimeException e) {
                    log.error(
                            String.format(
                                    "ImagesHandler::AddTemplateImagesFromImportCommand::ExecuteCommand: Failed adding image %1$s to DB",
                                    importedImage.getId()),
                            e);
                    throw new VdcBLLException(VdcBllErrors.DB, e);
                }
                setActionReturnValue(importedImage); // / TODO: Not a good return value -
                                          // template has
                // several DiskImageTemplates in case of a multi-drive template.
            }
        }

        setSucceeded(true);
    }

    private static Log log = LogFactory.getLog(AddTemplateImagesFromImportCommand.class);
}
