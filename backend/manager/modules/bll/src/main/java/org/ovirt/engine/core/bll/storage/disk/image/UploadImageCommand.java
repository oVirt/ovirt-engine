package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UploadImageParameters;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferUpdates;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.AddImageTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ExtendImageTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveImageTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVolumeLegalityVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.uutils.crypto.ticket.TicketEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public abstract class UploadImageCommand<T extends UploadImageParameters> extends BaseImagesCommand<T> {

    // Logger used by static updateEntity() method
    private static final Logger staticLog = LoggerFactory.getLogger(UploadImageCommand.class);

    // Some token/"claim" names are from RFC 7519 on JWT
    private static final String TOKEN_NOT_BEFORE = "nbf";
    private static final String TOKEN_EXPIRATION = "exp";
    private static final String TOKEN_ISSUED_AT = "iat";
    private static final String TOKEN_IMAGED_HOST_URI = "imaged-uri";
    private static final String TOKEN_TRANSFER_TICKET = "transfer-ticket";
    private static final boolean LEGAL_IMAGE = true;
    private static final boolean ILLEGAL_IMAGE = false;

    private static final String HTTP_SCHEME = "http://";
    private static final String HTTPS_SCHEME = "https://";
    private static final String IMAGES_PATH = "/images";

    // Container for context needed by state machine handlers
    class StateContext {
        ImageTransfer entity;
        long iterationTimestamp;
        Guid childCmdId;
    }

    public UploadImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        log.info("Creating ImageTransfer entity for command '{}'", getCommandId());
        ImageTransfer entity = new ImageTransfer(getCommandId());
        entity.setCommandType(getActionType());
        entity.setPhase(ImageTransferPhase.INITIALIZING);
        entity.setLastUpdated(new Date());
        entity.setBytesTotal(getParameters().getUploadSize());
        getImageTransferDao().save(entity);

        log.info("Creating {} image", getUploadType());
        createImage();
        setActionReturnValue(getCommandId());
        setSucceeded(true);
        // The callback will poll for createImage() completion and resume the initialization
    }

    public void proceedCommandExecution(Guid childCmdId) {
        ImageTransfer entity = getImageTransferDao().get(getCommandId());
        if (entity == null || entity.getPhase() == null) {
            log.error("Image Upload status entity corrupt or missing from database"
                    + " for image transfer command '{}'", getCommandId());
            setCommandStatus(CommandStatus.FAILED);
            return;
        }
        if (entity.getDiskId() != null) {
            // Make the disk id available for all states below.  If the upload is still
            // initializing, this may be set below in the INITIALIZING block instead.
            setImage((DiskImage) getDiskDao().get(entity.getDiskId()));
        }

        // Check conditions for pausing the upload (ie UI is MIA)
        long ts = System.currentTimeMillis() / 1000;
        if (pauseUploadIfNecessary(entity, ts)) {
            return;
        }

        executeStateHandler(entity, ts, childCmdId);
    }

    public void executeStateHandler(ImageTransfer entity, long timestamp, Guid childCmdId) {
        StateContext context = new StateContext();
        context.entity = entity;
        context.iterationTimestamp = timestamp;
        context.childCmdId = childCmdId;

        // State handler methods are responsible for calling setCommandStatus
        // as well as updating the entity to reflect transitions.
        switch (entity.getPhase()) {
        case INITIALIZING:
            handleInitializing(context);
            break;
        case RESUMING:
            handleResuming();
            break;
        case TRANSFERRING:
            handleTransferring(context);
            break;
        case PAUSED_SYSTEM:
            handlePausedSystem(context);
            break;
        case PAUSED_USER:
            handlePausedUser(context);
            break;
        case CANCELLED:
            handleCancelled();
            break;
        case FINALIZING_SUCCESS:
            handleFinalizingSuccess(context);
            break;
        case FINALIZING_FAILURE:
            handleFinalizingFailure(context);
            break;
        case FINISHED_SUCCESS:
            handleFinishedSuccess();
            break;
        case FINISHED_FAILURE:
            handleFinishedFailure();
            break;
        }
    }

    private void handleInitializing(final StateContext context) {
        if (context.childCmdId == null) {
            // Guard against callback invocation before executeCommand() is complete
            return;
        }

        switch (CommandCoordinatorUtil.getCommandStatus(context.childCmdId)) {
        case NOT_STARTED:
        case ACTIVE:
            log.info("Waiting for {} to be added for image transfer command '{}'",
                    getUploadType(), getCommandId());
            return;
        case SUCCEEDED:
            break;
        default:
            log.error("Failed to add {} for image transfer command '{}'",
                    getUploadType(), getCommandId());
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        VdcReturnValueBase addDiskRetVal = CommandCoordinatorUtil.getCommandReturnValue(context.childCmdId);
        if (addDiskRetVal == null || !addDiskRetVal.getSucceeded()) {
            log.error("Failed to add {} (command status was success, but return value was failed)"
                    + " for image transfer command '{}'", getUploadType(), getCommandId());
            setReturnValue(addDiskRetVal);
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        Guid createdId = addDiskRetVal.getActionReturnValue();
        DiskImage createdDiskImage = (DiskImage) getDiskDao().get(createdId);
        getParameters().setImageId(createdId);
        persistCommand(getParameters().getParentCommand(), true);
        setImage(createdDiskImage);
        log.info("Successfully added {} for image transfer command '{}'",
                getUploadDescription(), getCommandId());

        ImageTransferUpdates updates = new ImageTransferUpdates();
        updates.setDiskId(createdId);
        updateEntity(updates);

        // The image will remain locked until the upload command has completed.
        lockImage();

        boolean initSessionSuccess = startImageTransferSession();
        updateEntityPhase(initSessionSuccess ? ImageTransferPhase.TRANSFERRING
                : ImageTransferPhase.PAUSED_SYSTEM);
        log.info("Returning from proceedCommandExecution after starting transfer session"
                    + " for image transfer command '{}'", getCommandId());

        resetPeriodicPauseLogTime(0);
    }

    private void handleResuming() {
        lockImage();

        boolean resumeSessionSuccess = startImageTransferSession();
        if (resumeSessionSuccess) {
            log.info("Resuming upload session for {}", getUploadDescription());
        } else {
            log.error("Failed to resume upload session for {}", getUploadDescription());
        }
        updateEntityPhase(resumeSessionSuccess ? ImageTransferPhase.TRANSFERRING
                : ImageTransferPhase.PAUSED_SYSTEM);

        resetPeriodicPauseLogTime(0);
    }

    private void handleTransferring(final StateContext context) {
        // While the transfer is in progress, we're responsible for keeping the transfer
        // session alive.  The polling interval is user-configurable and grows exponentially,
        // make sure to set it with time to spare.
        if (context.iterationTimestamp
                >= getParameters().getSessionExpiration() - getHostTicketRefreshAllowance()) {
            log.info("Renewing transfer ticket for {}", getUploadDescription());
            extendImageTransferSession(context.entity);
        } else {
            log.debug("Not yet renewing transfer ticket for {}", getUploadDescription());
        }

        resetPeriodicPauseLogTime(0);
    }

    private void handlePausedUser(final StateContext context) {
        handlePaused(context);
    }

    private void handlePausedSystem(final StateContext context) {
        handlePaused(context);
    }

    private void handleCancelled() {
        log.info("Upload cancelled for {}", getUploadDescription());
        setAuditLogTypeFromPhase(ImageTransferPhase.CANCELLED);
        updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
    }

    private void handleFinalizingSuccess(final StateContext context) {
        log.info("Finalizing successful upload to {}", getUploadDescription());

        // If stopping the session did not succeed, don't change the upload state.
        if (stopImageTransferSession(context.entity)) {
            // We want to use the transferring vds for image actions for having a coherent log when uploading.
            Guid transferingVdsId = context.entity.getVdsId();
            if (verifyImage(transferingVdsId)) {
                setVolumeLegalityInStorage(LEGAL_IMAGE);
                unLockImage();
                updateEntityPhase(ImageTransferPhase.FINISHED_SUCCESS);
            } else {
                setImageStatus(ImageStatus.ILLEGAL);
                updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
            }

            // Finished using the image, tear it down.
            tearDownImage(context.entity.getVdsId());
        }
    }

    private boolean verifyImage(Guid transferingVdsId) {
        ImageActionsVDSCommandParameters parameters =
                new ImageActionsVDSCommandParameters(transferingVdsId, getStoragePool().getId(),
                        getStorageDomainId(),
                        getImage().getImage().getDiskId(),
                        getImage().getImageId());

        try {
            // As we currently support a single volume image, we only need to verify that volume.
            getBackend().getResourceManager().runVdsCommand(VDSCommandType.VerifyUntrustedVolume,
                    parameters);
        } catch (RuntimeException e) {
            log.error("Failed to verify uploaded image: {}", e);
            return false;
        }
        return true;
    }

    private void handleFinalizingFailure(final StateContext context) {
        log.error("Finalizing failed upload to {}", getUploadDescription());
        stopImageTransferSession(context.entity);
        setImageStatus(ImageStatus.ILLEGAL);
        updateEntityPhase(ImageTransferPhase.FINISHED_FAILURE);
    }

    private void handleFinishedSuccess() {
        log.info("Upload to {} was successful", getUploadDescription());
        setCommandStatus(CommandStatus.SUCCEEDED);
    }

    private void handleFinishedFailure() {
        log.error("Upload to {} failed", getUploadDescription());
        setCommandStatus(CommandStatus.FAILED);
    }


    protected abstract void createImage();


    private void handlePaused(final StateContext context) {
        // Close the session, but leave the command running to support later resumption.
        // Stopping the image transfer session may fail so it's retried; the call is
        // idempotent and lightweight if the session is not in progress.
        periodicPauseLog(context.entity, context.iterationTimestamp);
        stopImageTransferSession(context.entity);
    }

    /**
     * Verify conditions for continuing the upload, pausing it if necessary.
     * @return true if upload was paused
     */
    private boolean pauseUploadIfNecessary(ImageTransfer entity, long ts) {
        // If a keepalive interval was set by the client, then it needs to respond
        // within that interval during INITIALIZING and TRANSFERRING states.
        if (getParameters().getKeepaliveInterval() > 0
                && (entity.getPhase() == ImageTransferPhase.INITIALIZING ||
                    entity.getPhase() == ImageTransferPhase.TRANSFERRING)
                && (entity.getLastUpdated().getTime() / 1000) +
                    getParameters().getKeepaliveInterval() < ts) {
            log.warn("Upload to {} paused due to no updates in {} seconds",
                    getUploadDescription(),
                    ts - (entity.getLastUpdated().getTime() / 1000));
            updateEntityPhase(ImageTransferPhase.PAUSED_SYSTEM);
            return true;
        }
        return false;
    }

    private void resetPeriodicPauseLogTime(long ts) {
        if (getParameters().getLastPauseLogTime() != ts) {
            getParameters().setLastPauseLogTime(ts);
            persistCommand(getParameters().getParentCommand(), true);
        }
    }

    private void periodicPauseLog(ImageTransfer entity, long ts) {
        if (ts >= getParameters().getLastPauseLogTime() + getPauseLogInterval()) {
            log.info("Upload to {} was paused by {}", getUploadDescription(),
                    entity.getPhase() == ImageTransferPhase.PAUSED_SYSTEM ? "system" : "user");
            resetPeriodicPauseLogTime(ts);
        }
    }


    /**
     * Start the ovirt-image-daemon session
     * @return true if session was started
     */
    private boolean startImageTransferSession() {
        if (!initializeVds()) {
            log.error("Could not find a suitable host for image data transfer");
            return false;
        }
        Guid imagedTicketId = Guid.newGuid();

        // Create the signed ticket first because we can just throw it away if we fail to start the image
        // transfer session.  The converse would require us to close the transfer session on failure.
        String signedTicket = createSignedTicket(getVds(), imagedTicketId);
        if (signedTicket == null) {
            return false;
        }

        String[] operations = { "write" };
        long timeout = getHostTicketLifetime();
        String imagePath;
        try {
            imagePath = prepareImage(getVdsId());
        } catch (Exception e) {
            log.error("Failed to prepare image for transfer session: {}", e);
            return false;
        }

        if (!setVolumeLegalityInStorage(ILLEGAL_IMAGE)) {
            return false;
        }

        AddImageTicketVDSCommandParameters
                transferCommandParams = new AddImageTicketVDSCommandParameters(getVdsId(),
                        imagedTicketId, operations, timeout, getParameters().getUploadSize(), imagePath);

        // TODO This is called from doPolling(), we should run it async (runFutureVDSCommand?)
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = getBackend().getResourceManager().runVdsCommand(VDSCommandType.AddImageTicket,
                    transferCommandParams);
        } catch (RuntimeException e) {
            log.error("Failed to start image transfer session: {}", e);
            return false;
        }

        if (!vdsRetVal.getSucceeded()) {
            log.error("Failed to start image transfer session");
            return false;
        }
        log.info("Started transfer session with ticket id {}, timeout {} seconds",
                imagedTicketId.toString(), timeout);

        ImageTransferUpdates updates = new ImageTransferUpdates();
        updates.setVdsId(getVdsId());
        updates.setImagedTicketId(imagedTicketId);
        updates.setProxyUri(getProxyUri());
        updates.setSignedTicket(signedTicket);
        updateEntity(updates);

        setNewSessionExpiration(timeout);
        return true;
    }

    private boolean setVolumeLegalityInStorage(boolean legal) {
        SetVolumeLegalityVDSCommandParameters parameters =
                new SetVolumeLegalityVDSCommandParameters(getStoragePool().getId(),
                        getStorageDomainId(),
                        getImage().getImage().getDiskId(),
                        getImage().getImageId(),
                        legal);
        try {
            runVdsCommand(VDSCommandType.SetVolumeLegality, parameters);
        } catch (EngineException e) {
            log.error("Failed to set image's volume's legality to {} for image {} and volume {}: {}",
                    legal, getImage().getImage().getDiskId(), getImage().getImageId(), e);
            return false;
        }
        return true;
    }

    protected abstract String prepareImage(Guid vdsId);

    private boolean extendImageTransferSession(final ImageTransfer entity) {
        if (entity.getImagedTicketId() == null) {
            log.error("Failed to extend image transfer session: no existing session to extend");
            return false;
        }

        long timeout = getHostTicketLifetime();
        Guid resourceId = entity.getImagedTicketId();
        ExtendImageTicketVDSCommandParameters
                transferCommandParams = new ExtendImageTicketVDSCommandParameters(entity.getVdsId(),
                entity.getImagedTicketId(), timeout);

        // TODO This is called from doPolling(), we should run it async (runFutureVDSCommand?)
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = getBackend().getResourceManager().runVdsCommand(VDSCommandType.ExtendImageTicket,
                    transferCommandParams);
        } catch (RuntimeException e) {
            log.error("Failed to extend image transfer session for ticket '{}': {}",
                    resourceId.toString(), e);
            return false;
        }

        if (!vdsRetVal.getSucceeded()) {
            log.error("Failed to extend image transfer session");
            return false;
        }
        log.info("Transfer session with ticket id {} extended, timeout {} seconds",
                resourceId.toString(), timeout);

        setNewSessionExpiration(timeout);
        return true;
    }

    private void setNewSessionExpiration(long timeout) {
        getParameters().setSessionExpiration((System.currentTimeMillis() / 1000) + timeout);
        persistCommand(getParameters().getParentCommand(), true);
    }

    private boolean stopImageTransferSession(ImageTransfer entity) {
        if (entity.getImagedTicketId() == null) {
            log.warn("Failed to stop image transfer session. Ticket does not exist for image '{}'", entity.getDiskId());
            return false;
        }

        Guid resourceId = entity.getImagedTicketId();
        RemoveImageTicketVDSCommandParameters parameters = new RemoveImageTicketVDSCommandParameters(
                entity.getVdsId(), resourceId);

        // TODO This is called from doPolling(), we should run it async (runFutureVDSCommand?)
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = getBackend().getResourceManager().runVdsCommand(
                    VDSCommandType.RemoveImageTicket, parameters);
        } catch (RuntimeException e) {
            log.error("Failed to stop image transfer session for ticket '{}': {}", resourceId.toString(), e);
            return false;
        }

        if (!vdsRetVal.getSucceeded()) {
            log.warn("Failed to stop image transfer session for ticket '{}'", resourceId.toString());
            return false;
        }
        log.info("Successfully stopped image transfer session for ticket '{}'", resourceId.toString());

        ImageTransferUpdates updates = new ImageTransferUpdates();
        updates.setClearResourceId(true);
        updateEntity(updates);
        return true;
    }

    protected abstract void tearDownImage(Guid vdsId);


    private String createSignedTicket(VDS vds, Guid transferToken) {
        String ticket;

        Map<String, Object> elements = new HashMap<>();
        long ts = System.currentTimeMillis() / 1000;
        elements.put(TOKEN_NOT_BEFORE, ts);
        elements.put(TOKEN_ISSUED_AT, ts);
        elements.put(TOKEN_EXPIRATION, ts + getClientTicketLifetime());
        elements.put(TOKEN_IMAGED_HOST_URI, getImageDaemonUri(vds.getHostName()));
        elements.put(TOKEN_TRANSFER_TICKET, transferToken.toString());

        String payload;
        try {
            payload = JsonHelper.mapToJson(elements);
        } catch (Exception e) {
            log.error("Failed to create JSON payload for signed ticket", e);
            return null;
        }
        log.debug("Signed ticket payload: {}", payload);

        try {
            ticket = new TicketEncoder(
                    EngineEncryptionUtils.getPrivateKeyEntry().getCertificate(),
                    EngineEncryptionUtils.getPrivateKeyEntry().getPrivateKey(),
                    getClientTicketLifetime()
            ).encode(payload);
        } catch (Exception e) {
            log.error("Failed to encode ticket for image upload", e);
            return null;
        }

        return ticket;
    }


    private ImageTransfer updateEntityPhase(ImageTransferPhase phase) {
        ImageTransferUpdates updates = new ImageTransferUpdates(getCommandId());
        updates.setPhase(phase);
        return updateEntity(updates);
    }

    private ImageTransfer updateEntity(ImageTransferUpdates updates) {
        return updateEntity(updates, getCommandId());
    }

    public static ImageTransfer updateEntity(ImageTransferUpdates updates, Guid commandId) {
        // TODO this lock might not be enough; analyze possible concurrent calls
        LockManager lockManager = LockManagerFactory.getLockManager();
        EngineLock lock = getEntityUpdateLock(commandId);
        lockManager.acquireLockWait(lock);

        ImageTransfer entity = DbFacade.getInstance().getImageTransferDao().get(commandId);
        if (entity == null) {
            staticLog.error("Attempt to update non-existent ImageUpload entity");
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
                staticLog.info("Updating image upload {}{} phase to {}{}",
                        commandId, disk, updates.getPhase(), message);
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

        DbFacade.getInstance().getImageTransferDao().update(entity);

        lockManager.releaseLock(lock);
        return entity;
    }

    private static EngineLock getEntityUpdateLock(Guid commandId) {
        Map<String, Pair<String, String>> lockMap =
                Collections.singletonMap(commandId.toString(),
                        LockMessagesMatchUtil.makeLockingPair(
                                LockingGroup.DISK,
                                EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return new EngineLock(lockMap);
    }


    private int getHostTicketRefreshAllowance() {
        return Config.<Integer>getValue(ConfigValues.ImageTransferHostTicketRefreshAllowanceInSeconds);
    }

    private int getHostTicketLifetime() {
        return Config.<Integer>getValue(ConfigValues.ImageTransferHostTicketValidityInSeconds);
    }

    private int getClientTicketLifetime() {
        return Config.<Integer>getValue(ConfigValues.ImageTransferClientTicketValidityInSeconds);
    }

    private int getPauseLogInterval() {
        return Config.<Integer>getValue(ConfigValues.ImageTransferPausedLogIntervalInSeconds);
    }

    private String getProxyUri() {
        String scheme = Config.<Boolean> getValue(ConfigValues.ImageProxySSLEnabled)?  HTTPS_SCHEME : HTTP_SCHEME;
        String address = Config.getValue(ConfigValues.ImageProxyAddress);
        return scheme + address + IMAGES_PATH;
    }

    private String getImageDaemonUri(String daemonHostname) {
        String port = Config.getValue(ConfigValues.ImageDaemonPort);
        return HTTPS_SCHEME + daemonHostname + ":" + port;
    }

    protected ImageTransferDao getImageTransferDao() {
        return getDbFacade().getImageTransferDao();
    }


    protected void setAuditLogTypeFromPhase(ImageTransferPhase phase) {
        if (getParameters().getAuditLogType() != null) {
            // Some flows, e.g. cancellation, may set the log type more than once.
            // In this case, the first type is the most accurate.
            return;
        }

        if (phase == ImageTransferPhase.FINISHED_SUCCESS) {
            getParameters().setAuditLogType(AuditLogType.UPLOAD_IMAGE_SUCCEEDED);
        } else if (phase == ImageTransferPhase.CANCELLED) {
            getParameters().setAuditLogType(AuditLogType.UPLOAD_IMAGE_CANCELLED);
        } else if (phase == ImageTransferPhase.FINISHED_FAILURE) {
            getParameters().setAuditLogType(AuditLogType.UPLOAD_IMAGE_FAILED);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("DiskAlias", getImageAlias());
        return getActionState() == CommandActionState.EXECUTE
                ? AuditLogType.UPLOAD_IMAGE_INITIATED : getParameters().getAuditLogType();
    }

    // Return an alias for the image usable in logs (this may
    // be called before the new image is successfully created).
    protected abstract String getImageAlias();

    protected abstract String getUploadType();

    private String getImageIdNullSafe() {
        return getParameters().getImageId() != null ?
                getParameters().getImageId().toString() : "(null)";
    }

    // Return a string describing the upload, safe for use before the new image
    // is successfully created; e.g. "disk 'NewDisk' (id '<uuid>')".
    protected String getUploadDescription() {
        return String.format("%s '%s' (id '%s')",
                getUploadType(), getImageAlias(), getImageIdNullSafe());
    }


    public void onSucceeded() {
        updateEntityPhase(ImageTransferPhase.FINISHED_SUCCESS);
        log.debug("Removing ImageUpload id {}", getCommandId());
        getImageTransferDao().remove(getCommandId());
        endSuccessfully();
        log.info("Successfully uploaded {} (command id '{}')",
                getUploadDescription(), getCommandId());
    }

    public void onFailed() {
        updateEntityPhase(ImageTransferPhase.FINISHED_FAILURE);
        log.debug("Removing ImageUpload id {}", getCommandId());
        getImageTransferDao().remove(getCommandId());
        endWithFailure();
        log.error("Failed to upload {} (command id '{}')",
                getUploadDescription(), getCommandId());
    }

    @Override
    public CommandCallback getCallback() {
        return new UploadImageCommandCallback();
    }
}
