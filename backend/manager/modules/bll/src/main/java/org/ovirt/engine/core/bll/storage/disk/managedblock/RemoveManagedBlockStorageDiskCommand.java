package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.managedblock.util.ManagedBlockStorageDiskUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
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

    @Inject
    private ManagedBlockStorageDiskUtil managedBlockStorageDiskUtil;

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public RemoveManagedBlockStorageDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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

        TransactionSupport.executeInNewTransaction(() -> {
            managedBlockStorageDiskUtil.lockImage(getParameters().getDiskId());
            return null;
        });

        try {
            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams,
                            getCorrelationId());
            returnValue = cinderlibExecutor.runCommand(CinderlibExecutor.CinderlibCommand.DELETE_VOLUME, params);
        } catch (Exception e) {
            log.error("Failed to remove volume: {}", e);
            getReturnValue().setActionReturnValue(false);
            return;
        }

        if (!returnValue.getSucceed()) {
            return;
        }

        removeDiskFromDb();
        getReturnValue().setActionReturnValue(true);
        setSucceeded(true);
        persistCommandIfNeeded();
    }

    private void removeDiskFromDb() {
        ManagedBlockStorageDisk disk = new ManagedBlockStorageDisk();
        disk.setId(getParameters().getDiskId());
        disk.setImageId(getParameters().getDiskId());

        TransactionSupport.executeInNewTransaction(() -> {
            imageStorageDomainMapDao.remove(disk.getImageId());
            imageDao.remove(disk.getImageId());
            diskImageDynamicDao.remove(disk.getImageId());

            // Make sure it's a base disk and not a cloned volume
            if (disk.getImageId() == disk.getId()) {
                baseDiskDao.remove(disk.getId());
            }

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

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void endSuccessfully() {
        managedBlockStorageDiskUtil.unlockImage(getParameters().getDiskId());
        super.endSuccessfully();
    }

    @Override
    protected void endWithFailure() {
        endSuccessfully();
    }
}
