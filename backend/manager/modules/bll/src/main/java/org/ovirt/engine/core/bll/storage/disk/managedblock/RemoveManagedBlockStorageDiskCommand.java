package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibReturnValue;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RemoveManagedBlockStorageDiskCommand<T extends RemoveDiskParameters> extends CommandBase<T> {

    @Inject
    private ImageDao imageDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;

    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

    @Inject
    private CinderlibExecutor cinderlibExecutor;

    @Inject
    private CinderStorageDao cinderStorageDao;

    @Inject
    private BaseDiskDao baseDiskDao;

    public RemoveManagedBlockStorageDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        DiskImage image = diskImageDao.get(getParameters().getDiskId());
        getParameters().setStorageDomainId(image.getStorageIds().get(0));
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getParameters().getDiskId().toString());
        CinderlibReturnValue returnValue;

        try {
            CinderlibCommandParameters params = new CinderlibCommandParameters(
                    JsonHelper.mapToJson(managedBlockStorage.getDriverOptions(), false),
                    extraParams);
            returnValue = cinderlibExecutor.runCommand(CinderlibExecutor.CinderlibCommand.DELETE_VOLUME, params);
        } catch (Exception e) {
            log.error("Failed to remove volume: {}", e);
            return;
        }

        if (!returnValue.getSucceed()) {
            return;
        }

        removeDiskFromDb();
        setSucceeded(true);
    }

    private void removeDiskFromDb() {
        ManagedBlockStorageDisk disk = new ManagedBlockStorageDisk();
        disk.setId(getParameters().getDiskId());
        disk.setImageId(getParameters().getDiskId());

        TransactionSupport.executeInNewTransaction(() -> {
            imageStorageDomainMapDao.remove(disk.getImageId());
            imageDao.remove(disk.getImageId());
            diskImageDynamicDao.remove(disk.getImageId());
            baseDiskDao.remove(disk.getId());
            return null;
        });
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getParameters().getStorageDomainId()));
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return null;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
