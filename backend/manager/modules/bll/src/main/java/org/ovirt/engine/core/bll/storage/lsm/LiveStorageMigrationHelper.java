package org.ovirt.engine.core.bll.storage.lsm;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

@Singleton
public class LiveStorageMigrationHelper {

    @Inject
    private BaseDiskDao baseDiskDao;

    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    public void removeImage(CommandBase<?> cmd, Guid storageDomainId, Guid imageGroupId, Guid imageId,
                                    AuditLogType failureAuditLog) {
        RemoveImageParameters removeImageParams =
                new RemoveImageParameters(imageId);
        removeImageParams.setStorageDomainId(storageDomainId);
        removeImageParams.setParentCommand(VdcActionType.RemoveImage);
        removeImageParams.setDbOperationScope(ImageDbOperationScope.NONE);
        removeImageParams.setShouldLockImage(false);
        VdcReturnValueBase returnValue = cmd.runInternalActionWithTasksContext(
                VdcActionType.RemoveImage,
                removeImageParams);
        if (returnValue.getSucceeded()) {
            cmd.startPollingAsyncTasks(returnValue.getInternalVdsmTaskIdList());
        } else {
            cmd.addCustomValue("DiskAlias", baseDiskDao.get(imageGroupId).getDiskAlias());
            cmd.addCustomValue("StorageDomainName", storageDomainStaticDao.get(storageDomainId).getName());
            cmd.addCustomValue("UserName", cmd.getUserName());
            cmd.getAuditLogDirector().log(cmd, failureAuditLog);
        }
    }
}
