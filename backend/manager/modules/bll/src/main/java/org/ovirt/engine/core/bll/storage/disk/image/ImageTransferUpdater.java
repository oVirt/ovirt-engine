package org.ovirt.engine.core.bll.storage.disk.image;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageTransferUpdater {
    private static final Logger log = LoggerFactory.getLogger(ImageTransferUpdater.class);

    private final ImageTransferDao imageTransferDao;
    private final LockManager lockManager;

    @Inject
    ImageTransferUpdater(ImageTransferDao imageTransferDao, LockManager lockManager) {
        this.imageTransferDao = requireNonNull(imageTransferDao);
        this.lockManager = requireNonNull(lockManager);
    }

    public ImageTransfer updateEntity(ImageTransfer updates, Guid commandId, boolean clearResourceId) {
        // TODO this lock might not be enough; analyze possible concurrent calls
        EngineLock lock = getEntityUpdateLock(commandId);
        try {
            lockManager.acquireLockWait(lock);

            ImageTransfer entity = imageTransferDao.get(commandId);
            if (entity == null) {
                log.error("Attempt to update non-existent ImageTransfer entity");
                return null;
            }

            entity.setLastUpdated(new Date());

            if (updates != null) {
                if (updates.getId() != null) {
                    entity.setId(updates.getId());
                }
                if (updates.getPhase() != null) {
                    String disk = entity.getDiskId() != null
                            ? String.format(" (image %s)", entity.getDiskId().toString()) : "";
                    String message = entity.getMessage() != null
                            ? String.format(" (message: '%s')", entity.getMessage()) : "";
                    log.info("Updating image transfer {}{} phase to {}{}",
                            commandId,
                            disk,
                            updates.getPhase(),
                            message);
                    entity.setPhase(updates.getPhase());
                }
                if (updates.getType() != null) {
                    entity.setType(updates.getType());
                }
                if (updates.getActive() != null) {
                    entity.setActive(updates.getActive());
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
                if (updates.getImagedTicketId() != null || clearResourceId) {
                    entity.setImagedTicketId(updates.getImagedTicketId());
                }
                if (updates.getProxyUri() != null) {
                    entity.setProxyUri(updates.getProxyUri());
                }
                if (updates.getDaemonUri() != null) {
                    entity.setDaemonUri(updates.getDaemonUri());
                }

                if (updates.getBytesSent() != null) {
                    entity.setBytesSent(updates.getBytesSent());
                }
                if (updates.getBytesTotal() != null) {
                    entity.setBytesTotal(updates.getBytesTotal());
                }
            }

            imageTransferDao.update(entity);

            return entity;
        } finally {
            lockManager.releaseLock(lock);
        }
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
