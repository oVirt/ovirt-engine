package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class HostStatusCell extends AbstractCell<VDS> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public void render(Context context, VDS vds, SafeHtmlBuilder sb, String id) {
        // Nothing to render if no host is provided:
        if (vds == null) {
            return;
        }

        // Find the image corresponding to the status of the host:
        VDSStatus status = vds.getStatus();
        ImageResource statusImage = null;
        switch (status) {
        case Unassigned:
        case NonResponsive:
        case InstallFailed:
        case Connecting:
        case Down:
            statusImage = resources.downImage();
            break;
        case PreparingForMaintenance:
            statusImage = resources.prepareImage();
            break;
        case Maintenance:
            statusImage = resources.maintenanceImage();
            break;
        case Up:
            statusImage = resources.upImage();
            break;
        case Error:
            statusImage = resources.errorImage();
            break;
        case Installing:
            statusImage = resources.hostInstallingImage();
            break;
        case Reboot:
            statusImage = resources.waitImage();
            break;
        case NonOperational:
            statusImage = resources.nonOperationalImage();
            break;
        case PendingApproval:
        case InstallingOS:
            statusImage = resources.unconfiguredImage();
            break;
        case Initializing:
            statusImage = resources.waitImage();
            break;
        case Kdumping:
            statusImage = resources.waitImage();
            break;
        default:
            statusImage = resources.downImage();
        }

        // Find the image corresponding to the alert:
        ImageResource alertImage = resources.alertImage();

        // Generate the HTML for the images:
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());
        SafeHtml alertImageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(alertImage).getHTML());

        // Generate the HTML for the cell including the exclamation mark only if
        // power management is not enabled or there are network configuration
        // changes that haven't been saved yet:
        sb.appendHtmlConstant("<div id=\"" + id + "\" style=\"text-align: center;\">"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append(statusImageHtml);
        boolean getnet_config_dirty =
                vds.getNetConfigDirty() == null ? false : vds.getNetConfigDirty().booleanValue();
        boolean showPMAlert = vds.getClusterSupportsVirtService() && !vds.isPmEnabled();
        boolean showGlusterAlert = vds.getClusterSupportsGlusterService() && vds.getGlusterPeerStatus() != PeerStatus.CONNECTED;
        if (showPMAlert || getnet_config_dirty || showGlusterAlert) {
            sb.append(alertImageHtml);
        }
        sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
    }
}
