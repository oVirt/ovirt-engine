package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.TransferClientType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

public class DownloadImageHandler {

    private static final Logger log = Logger.getLogger(DownloadImageHandler.class.getName());

    private Guid transferId;

    public DownloadImageHandler(Guid transferId) {
        this.transferId = transferId;
    }

    static TransferDiskImageParameters createInitParams(DiskImage diskImage) {
        TransferDiskImageParameters parameters = new TransferDiskImageParameters();
        parameters.setTransferType(TransferType.Download);
        parameters.setImageGroupID(diskImage.getId());
        String fileExtension = diskImage.getVolumeFormat() == VolumeFormat.COW ?
                ".qcow2" : ".raw"; //$NON-NLS-1$ //$NON-NLS-2$
        parameters.setDownloadFilename(diskImage.getDiskAlias() + fileExtension); //$NON-NLS-1$
        parameters.setTransferSize(diskImage.getActualSizeInBytes());
        parameters.setTransferClientType(TransferClientType.TRANSFER_VIA_BROWSER);

        return parameters;
    }

    public void start() {
        Frontend.getInstance().runQuery(QueryType.GetImageTransferById,
                new IdQueryParameters(transferId),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    ImageTransfer imageTransfer = returnValue.getReturnValue();
                    initiateDownload(imageTransfer);
                }));
    }

    private void initiateDownload(ImageTransfer imageTransfer) {
        // Use close=y to inform proxy and daemon that we are done and the
        // connection must be closed at the end of the download. Avoids failure
        // in deactivaing a volume after download.
        String url = imageTransfer.getProxyUri() + "/" + imageTransfer.getImagedTicketId() + "?close=y"; //$NON-NLS-1$ //$NON-NLS-2$

        log.info("Initiating download: " + url); //$NON-NLS-1$

        // Invoke download
        Frame frame = new Frame(url);
        frame.addLoadHandler(loadEvent -> Scheduler.get().scheduleDeferred(() ->
                RootPanel.get().remove(frame)));
        frame.getElement().getStyle().setDisplay(Style.Display.NONE);
        RootPanel.get().add(frame);
    }

    public static boolean isDownloadAllowed(List<? extends Disk> disks) {
        return disks != null && !disks.isEmpty() && disks.stream()
                .allMatch((Predicate<Disk>) disk ->
                        disk instanceof DiskImage
                        && disk.getImageTransferPhase() == null
                        && ((DiskImage) disk).getImageStatus() == ImageStatus.OK
                        && ((DiskImage) disk).getActualSizeInBytes() > 0
                        && ((DiskImage) disk).getParentId().equals(Guid.Empty));
    }
}
