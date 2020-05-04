package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;

public class DiskTransferProgressColumn extends AbstractProgressBarColumn<Disk> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();
    private static final ProgressBarColors color = ProgressBarColors.GREEN;

    public DiskTransferProgressColumn() {
    }

    @Override
    protected Integer getProgressValue(Disk disk) {
        if (disk.getImageTransferBytesSent() == null) {
            return null;
        }

        switch (disk.getImageTransferPhase()) {
            // Fall-through is intentional
        case TRANSFERRING:
        case RESUMING:
        case PAUSED_SYSTEM:
        case PAUSED_USER:
        case FINALIZING_SUCCESS:
            if (disk.getImageTransferBytesTotal() != null && disk.getImageTransferBytesTotal() > 0) {
                return (int) (disk.getImageTransferBytesSent() * 100 / disk.getImageTransferBytesTotal());
            }
            return 0;
        }
        return null;
    }

    @Override
    public SafeHtml getValue(Disk object) {
        return object.getImageTransferPhase() != null ? super.getValue(object) : null;
    }

    @Override
    protected String getStyle() {
        return "engine-progress-box-migration"; //$NON-NLS-1$
    }

    @Override
    protected String getProgressText(Disk disk) {
        if (disk.getImageTransferPhase() != null) {
            switch (disk.getImageTransferPhase()) {
            case UNKNOWN:
                return constants.imageTransferUnknown();
            case INITIALIZING:
                return constants.imageTransferInitializing();
            case RESUMING:
                return constants.imageTransferResuming();
            case TRANSFERRING:
                if (disk.getImageTransferBytesTotal() != 0
                        && disk.getImageTransferBytesSent() == 0) {
                    return constants.imageTransferring();
                } else if (disk.getImageTransferBytesTotal() == 0) {
                    return disk.getTransferType() == TransferType.Upload ?
                            constants.uploadingImageViaAPI() :
                            constants.downloadingImageViaAPI();
                } else if (disk.getImageTransferBytesSent() == null) {
                    return disk.getTransferType() == TransferType.Upload ?
                            constants.imageUploadTransferring() :
                            constants.imageDownloadTransferring();
                } else if (disk.getImageTransferBytesTotal() == null
                        || disk.getImageTransferBytesTotal() == 0) {
                    int bytesSent = (int) (disk.getImageTransferBytesSent() / SizeConverter.BYTES_IN_MB);
                    return disk.getTransferType() == TransferType.Upload ?
                            messages.imageUploadProgress(bytesSent) :
                            messages.imageDownloadProgress(bytesSent);
                } else {
                    int bytesSent = (int) (disk.getImageTransferBytesSent() / SizeConverter.BYTES_IN_MB);
                    int bytesTotal = (int) (disk.getImageTransferBytesTotal() / SizeConverter.BYTES_IN_MB);
                    return disk.getTransferType() == TransferType.Upload ?
                            messages.imageUploadProgressWithTotal(bytesSent, bytesTotal) :
                            messages.imageDownloadProgressWithTotal(bytesSent, bytesTotal);
                }
            case PAUSED_SYSTEM:
                return constants.imageTransferPausedSystem();
            case PAUSED_USER:
                return constants.imageTransferPausedUser();
            case CANCELLED_SYSTEM:
                return constants.imageTransferCancelledSystem();
            case CANCELLED_USER:
                return constants.imageTransferCancelledUser();
            case FINALIZING_SUCCESS:
                return constants.imageTransferFinalizingSuccess();
            case FINALIZING_FAILURE:
                return constants.imageTransferFinalizingFailure();
            case FINALIZING_CLEANUP:
                return constants.imageTransferFinalizingCleanup();
            case FINISHED_SUCCESS:
                return constants.imageTransferFinishedSuccess();
            case FINISHED_FAILURE:
                return constants.imageTransferFinishedFailure();
            case FINISHED_CLEANUP:
                return constants.imageTransferFinishedCleanup();
            }
        }
        return constants.imageTransferUnknown();
    }

    @Override
    protected String getColorByProgress(int progress) {
        return color.asCode();
    }
}
