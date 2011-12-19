package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.AddImagesFromImportParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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
                DiskImageTemplate dt = new DiskImageTemplate(importedImage.getId(),
                        importedImage.getcontainer_guid(), importedImage.getinternal_drive_mapping(),
                        importedImage.getId(), "", "", getNow(), importedImage.getsize(),
                        importedImage.getdescription(), null);

                try {
                    DbFacade.getInstance().getDiskImageTemplateDAO().save(dt);
                    importedImage.setimageStatus(ImageStatus.LOCKED);
                    DbFacade.getInstance().getDiskImageDAO().save(importedImage);
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

                setActionReturnValue(dt); // / TODO: Not a good return value -
                                          // template has
                // several DiskImageTemplates in case of a multi-drive template.
            }
        }

        setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(AddTemplateImagesFromImportCommand.class);
}
