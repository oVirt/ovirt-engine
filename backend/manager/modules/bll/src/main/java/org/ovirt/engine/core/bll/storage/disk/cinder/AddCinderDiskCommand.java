package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.AddDiskCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class AddCinderDiskCommand<T extends AddDiskParameters> extends AddDiskCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(AddCinderDiskCommand.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

    public AddCinderDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void executeCommand() {
        CinderDisk cinderDisk = createCinderDisk();
        String volumeId = getCinderBroker().createDisk(cinderDisk);

        Guid volumeGuid = Guid.createGuidFromString(volumeId);
        cinderDisk.setId(volumeGuid);
        cinderDisk.setImageId(volumeGuid);
        getDiskVmElement().getId().setDeviceId(volumeGuid);
        cinderDisk.setVolumeClassification(VolumeClassification.Volume);
        addCinderDiskToDB(cinderDisk);

        getParameters().setDiskInfo(cinderDisk);
        persistCommand(getParameters().getParentCommand(), true);
        getReturnValue().setActionReturnValue(cinderDisk.getId());
        setSucceeded(true);
    }

    protected void addCinderDiskToDB(final CinderDisk cinderDisk) {
        TransactionSupport.executeInNewTransaction(() -> {
            baseDiskDao.save(cinderDisk);
            imageDao.save(cinderDisk.getImage());
            imageStorageDomainMapDao.save(new ImageStorageDomainMap(cinderDisk.getImageId(),
                    cinderDisk.getStorageIds().get(0), cinderDisk.getQuotaId(), cinderDisk.getDiskProfileId()));

            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(cinderDisk.getImageId());
            diskImageDynamicDao.save(diskDynamic);

            if (getVm() != null) {
                addDiskVmElementForDisk(getDiskVmElement());
                addManagedDeviceForDisk(cinderDisk.getId());
            }
            return null;
        });
    }

    private CinderDisk createCinderDisk() {
        final CinderDisk cinderDisk = new CinderDisk();
        cinderDisk.setDiskAlias(getDiskAlias());
        cinderDisk.setSize(getParameters().getDiskInfo().getSize());
        cinderDisk.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
        cinderDisk.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
        cinderDisk.setShareable(getParameters().getDiskInfo().isShareable());
        cinderDisk.setStorageIds(new ArrayList<>(Arrays.asList(getParameters().getStorageDomainId())));
        cinderDisk.setSize(getParameters().getDiskInfo().getSize());
        cinderDisk.setVolumeType(VolumeType.Unassigned);
        cinderDisk.setVolumeFormat(VolumeFormat.RAW);
        cinderDisk.setCreationDate(new Date());
        cinderDisk.setLastModified(new Date());
        cinderDisk.setActive(true);
        cinderDisk.setImageStatus(ImageStatus.LOCKED);
        cinderDisk.setVmSnapshotId(getParameters().getVmSnapshotId());
        cinderDisk.setCinderVolumeType(getParameters().getDiskInfo().getCinderVolumeType());
        cinderDisk.setQuotaId(getParameters().getQuotaId());

        if (getVm() != null) {
            cinderDisk.setDiskVmElements(Collections.singletonList(getDiskVmElement()));
        }
        return cinderDisk;
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        imagesHandler.updateImageStatus(getDiskId(), ImageStatus.OK);
        auditLogDirector.log(this, getEndSuccessAuditLogTypeValue(true));
    }

    @Override
    protected void endWithFailure() {
        super.endWithFailure();
        imagesHandler.updateImageStatus(getDiskId(), ImageStatus.ILLEGAL);
        auditLogDirector.log(this, getEndSuccessAuditLogTypeValue(false));
    }

    private Guid getDiskId() {
        return getParameters().getDiskInfo().getId();
    }

    @Override
    public boolean validate() {
        if (!validateQuota()){
            return false;
        }

        if (getVm() != null) {
            return validateDiskVmData();
        }
        return true;
    }

    @Override
    public CommandCallback getCallback() {
        return Injector.injectMembers(new AddCinderDiskCommandCallback());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        // The disk is already locked by the caller command (AddDiskCommand).
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        // The VM is already locked by the caller command (AddDiskCommand).
        return Collections.emptyMap();
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getParameters().getStorageDomainId()));
    }
}
