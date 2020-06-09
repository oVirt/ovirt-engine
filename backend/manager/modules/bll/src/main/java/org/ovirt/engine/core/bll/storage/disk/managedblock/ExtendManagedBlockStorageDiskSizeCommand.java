package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.UpdateDiskCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExtendManagedBlockStorageDiskSizeParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor.CinderlibCommand;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibReturnValue;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class ExtendManagedBlockStorageDiskSizeCommand<T extends ExtendManagedBlockStorageDiskSizeParameters> extends UpdateDiskCommand<T> {
    @Inject
    private CinderStorageDao cinderStorageDao;

    @Inject
    private CinderlibExecutor cinderlibExecutor;

    @Inject
    private ImageDao imageDao;

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public ExtendManagedBlockStorageDiskSizeCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getNewDisk().getId().toString());
        Number sizeInGiB = SizeConverter.convert(getParameters().getDiskInfo().getSize(),
                SizeConverter.SizeUnit.BYTES,
                SizeConverter.SizeUnit.GiB);
        extraParams.add(Long.toString(sizeInGiB.longValue()));

        CinderlibReturnValue returnValue;

        try {
            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams,
                            getCorrelationId());
            returnValue = cinderlibExecutor.runCommand(CinderlibCommand.EXTEND_VOLUME, params);
        } catch (Exception e) {
            log.error("Failed executing volume extension", e);
            return;
        }

        if (!returnValue.getSucceed()) {
            return;
        }

        updateDisk();
        setSucceeded(true);
    }

    private void updateDisk() {
        ManagedBlockStorageDisk managedBlockStorageDisk = (ManagedBlockStorageDisk) getNewDisk();
        TransactionSupport.executeInNewTransaction(() -> {
            imageDao.updateImageSize(managedBlockStorageDisk.getImageId(), managedBlockStorageDisk.getSize());
            return null;
        });
        performDiskUpdate();
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
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
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getParameters().getStorageDomainId()));
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
