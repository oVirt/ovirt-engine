package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.RemoveDiskCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@InternalCommandAttribute
public class RemoveCinderDiskCommand<T extends RemoveDiskParameters> extends RemoveDiskCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(RemoveCinderDiskCommand.class);
    private CinderBroker cinderBroker;
    private Guid storageDomainId;

    public RemoveCinderDiskCommand(T parameters) {
        super(parameters, null);
    }

    public RemoveCinderDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void executeCommand() {
        CinderDisk disk = (CinderDisk) getDisk();
        if (disk.getImageStatus() == ImageStatus.ILLEGAL) {
            // Remove disk from DB
            setCommandStatus(CommandStatus.SUCCEEDED);
        } else {
            // Remove disk from Cinder
            ImagesHandler.updateImageStatus(disk.getId(), ImageStatus.LOCKED);
            getCinderBroker().deleteDisk(disk);
        }
        persistCommand(getParameters().getParentCommand(), true);
        getReturnValue().setActionReturnValue(disk.getId());
        setSucceeded(true);
    }

    protected void removeDiskFromDb() {
        final CinderDisk cinderDisk = (CinderDisk) getDisk();
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        getDiskImageDynamicDAO().remove(cinderDisk.getImageId());
                        getImageDao().remove(cinderDisk.getImageId());
                        // todo: remove snapshot
                        getBaseDiskDao().remove(cinderDisk.getId());
                        getVmDeviceDAO().remove(new VmDeviceId(cinderDisk.getId(), null));
                        return null;
                    }
                });
    }

    protected void endSuccessfully() {
        setSucceeded(true);
    }

    protected ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    @Override
    public CommandCallback getCallback() {
        return new RemoveCinderDiskCommandCallback();
    }

    @Override
    public boolean canDoAction() {
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

    protected CinderBroker getCinderBroker() {
        if (cinderBroker == null) {
            cinderBroker = new CinderBroker(getStorageDomainId(), getReturnValue().getExecuteFailedMessages());
        }
        return cinderBroker;
    }

    @Override
    public Guid getStorageDomainId() {
        if (storageDomainId == null) {
            storageDomainId = getCinderDisk().getStorageIds().get(0);
        }
        return storageDomainId;
    }

    private CinderDisk getCinderDisk() {
        return (CinderDisk) getDisk();
    }
}
