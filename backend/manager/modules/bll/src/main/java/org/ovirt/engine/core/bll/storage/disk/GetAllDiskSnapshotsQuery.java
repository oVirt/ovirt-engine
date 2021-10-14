package org.ovirt.engine.core.bll.storage.disk;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.DiskSnapshotsQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;

public class GetAllDiskSnapshotsQuery<P extends DiskSnapshotsQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskImageDao diskImageDao;

    public GetAllDiskSnapshotsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Collection<Disk> allDiskSnapshots = diskDao.getAllFromDisksIncludingSnapshotsByDiskId(getParameters().getId(),
                getUserID(), getParameters().isFiltered());

        // Unless 'include_active' flag requested - filter out the active snapshot
        Predicate<Disk> filter = getParameters().getIncludeActive() ?
                DisksFilter.ONLY_DISK_SNAPSHOT.or(DisksFilter.ONLY_ACTIVE) : DisksFilter.ONLY_DISK_SNAPSHOT;
        List<DiskImage> imagesToReturn = DisksFilter.filterImageDisks(allDiskSnapshots, filter);

        // If the 'include_template' flag requested - check if one of the disk images' parents is a template disk,
        // fetch it and add to the result
        if (getParameters().getIncludeTemplate()) {
            Set<Guid> imageIds = imagesToReturn.stream().map(DiskImage::getImageId).collect(Collectors.toSet());
            DiskImage imageWithMissingParent = imagesToReturn.stream().
                    filter(image -> image.hasParent() && !imageIds.contains(image.getParentId()))
                    .findFirst().orElse(null);
            if (imageWithMissingParent != null) {
                // Found image with parent guid that does not belong to the requested disk
                DiskImage parentImage = diskImageDao.getAncestor(imageWithMissingParent.getImageId());
                if (parentImage != null && parentImage.isTemplate()) {
                    imagesToReturn.add(parentImage);
                } else {
                    log.error("Image '{}' which is a parent of image '{}', was not found",
                            imageWithMissingParent.getParentId(), imageWithMissingParent.getId());
                }
            }
        }

        getQueryReturnValue().setReturnValue(imagesToReturn);
    }
}
