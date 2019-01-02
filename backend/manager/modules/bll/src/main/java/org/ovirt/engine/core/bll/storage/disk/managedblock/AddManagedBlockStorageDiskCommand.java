package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor.CinderlibCommand;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@InternalCommandAttribute
public class AddManagedBlockStorageDiskCommand<T extends AddDiskParameters> extends CommandBase<T> {

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private CinderStorageDao cinderStorageDao;

    @Inject
    private CinderlibExecutor cinderlibExecutor;

    @Inject
    private BaseDiskDao baseDiskDao;

    @Inject
    private ImageDao imageDao;

    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;

    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

    public AddManagedBlockStorageDiskCommand(Guid commandId) {
        super(commandId);
    }

    public AddManagedBlockStorageDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());
        Guid volumeId = Guid.newGuid();
        List<String> extraParams = new ArrayList<>();
        extraParams.add(volumeId.toString());
        Number sizeInGiB = SizeConverter.convert(getParameters().getDiskInfo().getSize(),
                SizeConverter.SizeUnit.BYTES,
                SizeConverter.SizeUnit.GiB);
        extraParams.add(Long.toString(sizeInGiB.longValue()));
        CinderlibReturnValue returnValue;

        try {
            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(
                                managedBlockStorage.getAllDriverOptions(),
                            false),
                    extraParams);
            returnValue = cinderlibExecutor.runCommand(CinderlibCommand.CREATE_VOLUME, params);
        } catch (Exception e) {
            log.error("Failed executing volume creation", e);
            return;
        }

        if (!returnValue.getSucceed()) {
            return;
        }

        saveDisk(volumeId);
        getReturnValue().setActionReturnValue(volumeId);
        setSucceeded(true);
    }

    private void saveDisk(Guid volumeId) {
        ManagedBlockStorageDisk disk = createDisk();
        disk.setImageId(volumeId);
        disk.setId(volumeId);

        TransactionSupport.executeInNewTransaction(() -> {
            baseDiskDao.save(disk);
            imageDao.save(disk.getImage());
            imageStorageDomainMapDao.save(new ImageStorageDomainMap(disk.getImageId(),
                    disk.getStorageIds().get(0), disk.getQuotaId(), disk.getDiskProfileId()));

            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(disk.getImageId());
            diskImageDynamicDao.save(diskDynamic);

            return null;
        });
    }

    private ManagedBlockStorageDisk createDisk() {
        ManagedBlockStorageDisk disk = new ManagedBlockStorageDisk();
        disk.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
        disk.setSize(getParameters().getDiskInfo().getSize());
        disk.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
        disk.setShareable(getParameters().getDiskInfo().isShareable());
        disk.setStorageIds(new ArrayList<>(Arrays.asList(getParameters().getStorageDomainId())));
        disk.setVolumeType(VolumeType.Unassigned);
        disk.setVolumeFormat(VolumeFormat.RAW);
        disk.setCreationDate(new Date());
        disk.setLastModified(new Date());
        disk.setActive(true);
        disk.setQuotaId(getParameters().getQuotaId());

        return disk;
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.emptyMap();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getParameters().getStorageDomainId()));
    }
}
