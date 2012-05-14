package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class DiskStatusCell extends AbstractCell<DiskImage> {

    private static final CommonApplicationResources commonResources = GWT.create(CommonApplicationResources.class);
    private static final CommonApplicationTemplates commonTemplates = GWT.create(CommonApplicationTemplates.class);

    @Override
    public void render(Context context, DiskImage disk, SafeHtmlBuilder sb) {
        // Nothing to render if no host is provided:
        if (disk == null) {
            return;
        }

        ImageResource plugStatus = (disk.getPlugged() != null && disk.getPlugged().booleanValue()) ?
                commonResources.upImage() : commonResources.downImage();

        ImageResource shraeableStatus = (disk.isShareable()) ?
                commonResources.shareableDiskIcon() : null;

        SafeHtml plugStatusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(plugStatus).getHTML());

        SafeHtml shraeableStatusImageHtml = shraeableStatus != null ?
                        SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(shraeableStatus).getHTML())
                : new SafeHtmlBuilder().toSafeHtml();

        sb.append(commonTemplates.dualImage(plugStatusImageHtml, shraeableStatusImageHtml));
    }
}
