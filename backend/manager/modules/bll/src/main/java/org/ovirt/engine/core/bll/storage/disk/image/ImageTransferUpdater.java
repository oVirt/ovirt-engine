package org.ovirt.engine.core.bll.storage.disk.image;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferUpdates;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageTransferUpdater {
    private static final Logger log = LoggerFactory.getLogger(ImageTransferUpdater.class);

    private final ImageTransferDao imageTransferDao;

    @Inject
    ImageTransferUpdater(ImageTransferDao imageTransferDao) {
        this.imageTransferDao = requireNonNull(imageTransferDao);
    }

    public ImageTransfer updateEntity(ImageTransferUpdates updates, Guid commandId) {
        // TODO this lock might not be enough; analyze possible concurrent calls
        LockManager lockManager = LockManagerFactory.getLockManager();
        EngineLock lock = getEntityUpdateLock(commandId);
        lockManager.acquireLockWait(lock);

        ImageTransfer entity = imageTransferDao.get(commandId);
        if (entity == null) {
            log.error("Attempt to update non-existent ImageUpload entity");
            return null;
        }

        entity.setLastUpdated(new java.util.Date());

        if (updates != null) {
            if (updates.getId() != null) {
                entity.setId(updates.getId());
            }
            if (updates.getPhase() != null) {
                String disk = entity.getDiskId() != null
                        ? String.format(" (image %s)", entity.getDiskId().toString()) : "";
                String message = entity.getMessage() != null
                        ? String.format(" (message: '%s')", entity.getMessage()) : "";
                log.info("Updating image upload {}{} phase to {}{}",
                        commandId,
                        disk,
                        updates.getPhase(),
                        message);
                entity.setPhase(updates.getPhase());
            }
            if (updates.getMessage() != null) {
                entity.setMessage(updates.getMessage());
            }

            if (updates.getVdsId() != null) {
                entity.setVdsId(updates.getVdsId());
            }
            if (updates.getDiskId() != null) {
                entity.setDiskId(updates.getDiskId());
            }
            if (updates.getImagedTicketId() != null || updates.isClearResourceId()) {
                entity.setImagedTicketId(updates.getImagedTicketId());
            }
            if (updates.getProxyUri() != null) {
                entity.setProxyUri(updates.getProxyUri());
            }
            if (updates.getSignedTicket() != null) {
                entity.setSignedTicket(updates.getSignedTicket());
            }

            if (updates.getBytesSent() != null) {
                entity.setBytesSent(updates.getBytesSent());
            }
            if (updates.getBytesTotal() != null) {
                entity.setBytesTotal(updates.getBytesTotal());
            }
        }

        imageTransferDao.update(entity);

        lockManager.releaseLock(lock);
        return entity;
    }

    private EngineLock getEntityUpdateLock(Guid commandId) {
        Map<String, org.ovirt.engine.core.common.utils.Pair<String, String>> lockMap =
                Collections.singletonMap(commandId.toString(),
                        LockMessagesMatchUtil.makeLockingPair(
                                LockingGroup.DISK,
                                EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return new EngineLock(lockMap);
    }
}
