package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collection;
import java.util.Collections;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.RemoveImageCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveCinderDiskVolumeParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class RemoveCinderDiskVolumeCommand<T extends RemoveCinderDiskVolumeParameters> extends RemoveImageCommand<T> {
    private Guid storageDomainId;
    @Inject
    @Typed(RemoveCinderDiskVolumeCommandCallback.class)
    private Instance<RemoveCinderDiskVolumeCommandCallback> callbackProvider;

    public RemoveCinderDiskVolumeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void executeCommand() {
        CinderDisk volumeToDelete = getParameters().getRemovedVolume();
        if (!deleteVolumeFromCinder(volumeToDelete)) {
            setCommandStatus(CommandStatus.FAILED);
        }
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(true);
    }

    private boolean deleteVolumeFromCinder(CinderDisk lastCinderVolume) {
        try {
            getCinderBroker().deleteVolumeByClassificationType(lastCinderVolume);
        } catch (Exception e) {
            log.error("Failed to remove volume '{}' of cider disk id '{}'. Exception: {}",
                    lastCinderVolume.getImageId(),
                    lastCinderVolume.getId(),
                    e);
            return false;
        }
        return true;
    }

    @Override
    public Guid getStorageDomainId() {
        if (storageDomainId == null) {
            storageDomainId = getParameters().getRemovedVolume().getStorageIds().get(0);
        }
        return storageDomainId;
    }

    @Override
    protected void endWithFailure() {
        super.endWithFailure();
        CinderDisk cinderVolume = getParameters().getRemovedVolume();
        log.error("Could not remove Cinder volume id '{}' of disk id '{}'.",
                cinderVolume.getImageId(),
                cinderVolume.getId());
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage,
                getParameters().getRemovedVolume().getStorageIds().get(0)));
    }
}
