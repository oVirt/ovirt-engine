package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.AddImagesFromImportParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ImportCandidateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@InternalCommandAttribute
public class AddImagesFromImportCommand<T extends AddImagesFromImportParameters> extends AddImageFromImportCommand<T> {
    public AddImagesFromImportCommand(T parameters) {
        super(parameters);
        super.setVmId(new Guid(getParameters().getCandidateID()));
    }

    @Override
    protected VDSReturnValue ResourceManagerImport() {
        return Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.ImportCandidate,
                        new ImportCandidateVDSCommandParameters(getVm().getstorage_pool_id(), Guid.Empty,
                                getImageGroupId(), getParameters().getCandidateID(), getParameters().getBaseID(),
                                getParameters().getBaseImageIDs(), getParameters().getSource(), getParameters()
                                        .getPath(), getParameters().getForce()));
    }

    @Override
    protected void executeCommand() {
        setImageGroupId(Guid.NewGuid());
        ProcessImageInIrs();

        java.util.HashMap<String, DiskImage> leafsByDrive = null;
        java.util.HashMap<String, java.util.ArrayList<DiskImage>> restByDrive = null;
        RefObject<java.util.HashMap<String, DiskImage>> tempRefObject =
                new RefObject<java.util.HashMap<String, DiskImage>>(
                        leafsByDrive);
        RefObject<java.util.HashMap<String, java.util.ArrayList<DiskImage>>> tempRefObject2 =
                new RefObject<java.util.HashMap<String, java.util.ArrayList<DiskImage>>>(
                        restByDrive);
        SeparateLeafFromRest(getParameters().getImportedImages(), getParameters().getBaseImageIDs(), tempRefObject,
                tempRefObject2);
        leafsByDrive = tempRefObject.argvalue;
        restByDrive = tempRefObject2.argvalue;

        for (String drive : restByDrive.keySet()) {
            // Add all images except leaf image to DB:
            java.util.ArrayList<DiskImage> rest = restByDrive.get(drive);
            DiskImage leaf = leafsByDrive.get(drive);

            for (DiskImage image : rest) {
                try {
                    DbFacade.getInstance().getDiskImageDAO().save(image);
                } catch (RuntimeException e) {
                    log.error(
                            String.format(
                                    "ImagesHandler::AddImagesFromImportCommand::ExecuteCommand: Failed adding image %1$s to DB",
                                    image.getId()),
                            e);
                    throw new VdcBLLException(VdcBllErrors.DB, e);
                }
            }

            // Add locked leaf image to DB:
            leaf.setimageStatus(ImageStatus.LOCKED);
            AddDiskImageToDb(leaf);
        }

        setSucceeded(true);
    }

    private void SeparateLeafFromRest(Map<String, List<DiskImage>> imagesList, Map<String, Guid> imageTemplateIDs,
                                      RefObject<java.util.HashMap<String, DiskImage>> leafs,
                                      RefObject<java.util.HashMap<String, java.util.ArrayList<DiskImage>>> rests) {
        leafs.argvalue = new java.util.HashMap<String, DiskImage>();
        rests.argvalue = new java.util.HashMap<String, java.util.ArrayList<DiskImage>>();

        for (String drive : imagesList.keySet()) {
            DiskImage leaf = null;
            RefObject<DiskImage> tempRefObject = new RefObject<DiskImage>(leaf);
            GetLeafRecurisvely(tempRefObject, imagesList.get(drive), imageTemplateIDs.get(drive));
            leaf = tempRefObject.argvalue;
            // for some reason, 'leaf' cannot be used inside the Linq expression
            // below
            // since it is an 'out' parameter -> we make a copy.
            final DiskImage leafCopy = leaf;
            // LINQ 29456
            // List<DiskImage> rest = imagesList[drive].Where(a => a.image_guid
            // != leafCopy.image_guid).ToList();
            List<DiskImage> rest = LinqUtils.filter(imagesList.get(drive), new Predicate<DiskImage>() {
                @Override
                public boolean eval(DiskImage diskImage) {
                    return !diskImage.getId().equals(leafCopy.getId());
                }
            });

            // leafs.Add(drive, leaf);
            // rests.Add(drive, rest);
        }
    }

    private void GetLeafRecurisvely(RefObject<DiskImage> leaf, List<DiskImage> imagesList, final Guid parentImageID) {
        // LINQ 29456
        // List<DiskImage> nextInChain = imagesList.Where(a => a.ParentId ==
        // parentImageID).ToList();
        List<DiskImage> nextInChain = LinqUtils.filter(imagesList, new Predicate<DiskImage>() {
            @Override
            public boolean eval(DiskImage diskImage) {
                return diskImage.getParentId().equals(parentImageID);
            }
        });
        // LINQ 29456
        if (nextInChain.size() > 0) {
            // There is a child image to parentImageID -> parentImageID is not
            // last in
            // the chain (it is not a 'leaf' image) -> keep going recursively
            // (find out
            // whether the child image that we found is the 'leaf'):
            GetLeafRecurisvely(leaf, imagesList, nextInChain.get(0).getId());
        } else {
            // No child images to parentImageID -> parentImageID is the 'leaf'
            // image:
            // LINQ 29456
            // leaf = imagesList.Where(a => a.image_guid ==
            // parentImageID).ToList()[0];
            leaf.argvalue = LinqUtils.filter(imagesList, new Predicate<DiskImage>() {
                @Override
                public boolean eval(DiskImage diskImage) {
                    return diskImage.getId().equals(parentImageID);
                }
            }).get(0);
            // LINQ 29456
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(AddImagesFromImportCommand.class);
}
