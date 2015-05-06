package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class HostAdditionalStatusCell extends AbstractCell<VDS> {

    private final static ApplicationResources resources = AssetProvider.getResources();
    private final static ApplicationTemplates templates = AssetProvider.getTemplates();

    @Override
    public void render(Context context, VDS host, SafeHtmlBuilder sb, String id) {
        // Nothing to render if no host is provided or if no updates are available:
        if (host == null || !host.isUpdateAvailable()) {
            return;
        }

        ImageResource statusImage = resources.updateAvailableImage();
        // Generate the HTML for the image:
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());

        sb.append(templates.hostAdditionalStatusIcon(id, statusImageHtml));
    }
}
