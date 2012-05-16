package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class DiskStatusCell extends AbstractCell<Disk> {

    private static final CommonApplicationResources commonResources = GWT.create(CommonApplicationResources.class);
    private static final CommonApplicationTemplates commonTemplates = GWT.create(CommonApplicationTemplates.class);

    @Override
    public void render(Context context, Disk disk, SafeHtmlBuilder sb) {
        // Nothing to render if no host is provided:
        if (disk == null) {
            return;
        }

        ImageResource plugStatus = (disk.getPlugged() != null && disk.getPlugged().booleanValue()) ?
                commonResources.upImage() : commonResources.downImage();

        ImageResource shraeable = (disk.isShareable()) ?
                commonResources.shareableDiskIcon() : null;

        ImageResource externalDisk = (disk.getDiskStorageType() == DiskStorageType.LUN) ?
                commonResources.externalDiskIcon() : null;

        SafeHtml plugStatusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(plugStatus).getHTML());

        SafeHtml shraeableImageHtml = shraeable != null ?
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(shraeable).getHTML())
                : new SafeHtmlBuilder().toSafeHtml();

        SafeHtml externalDiskImageHtml = externalDisk != null ?
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(externalDisk).getHTML())
                : new SafeHtmlBuilder().toSafeHtml();

        sb.append(commonTemplates.tripleImage(plugStatusImageHtml, shraeableImageHtml, externalDiskImageHtml));
    }
}
