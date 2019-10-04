package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.ui.common.utils.JqueryUtils;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class HostStatusCell extends AbstractCell<VDS> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();
    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public void render(Context context, VDS vds, SafeHtmlBuilder sb, String id) {
        // Nothing to render if no host is provided:
        if (vds == null) {
            return;
        }

        // Find the image corresponding to the status of the host:
        VDSStatus status = vds.getStatus();
        ImageResource statusImage;

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
        SafeHtml statusImageHtml = AbstractImagePrototype.create(statusImage).getSafeHtml();
        SafeHtml alertImageHtml = AbstractImagePrototype.create(alertImage).getSafeHtml();

        // Generate the HTML for the cell, wrapped inside <div> element:
        sb.appendHtmlConstant("<div id=\"" + id + "\" style=\"text-align: center;\">"); //$NON-NLS-1$ //$NON-NLS-2$

        // Append status image:
        sb.append(statusImageHtml);

        // Append alert image under following conditions:
        // - power management is not enabled
        // - there are network configuration changes that haven't been saved yet
        // - there are Gluster related issues
        // - host reinstall is required
        // - CPU configuration is not compatible with configured values (vds has missing flags)
        if (hasPMAlert(vds) || hasNetconfigDirty(vds) || hasGlusterAlert(vds)
                || vds.getStaticData().isReinstallRequired()
                || hasDefaultRouteAlert(vds)
                || cpuFlagsMissing(vds)) {
            sb.append(alertImageHtml);
        }
        sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
    }

    @Override
    public SafeHtml getTooltip(VDS vds, Element parent, NativeEvent event) {
        Element target = event.getEventTarget().cast();
        if (ImageElement.TAG.equalsIgnoreCase(target.getTagName())) {
            // determine image based on its position within the wrapper <div>
            switch (JqueryUtils.index(target)) {
            case 0:
                // status image
                return SafeHtmlUtils.fromSafeConstant(getStatusTooltipText(vds.getStatus()));
            case 1:
                // alert image
                return getAlertTooltipText(vds);
            }
        }
        return null;
    }

    private String getStatusTooltipText(VDSStatus status) {
        switch (status) {
        case Up:
            return constants.up();
        case Down:
            return constants.down();
        case Unassigned:
            return constants.unassigned();
        case Maintenance:
            return constants.maintenance();
        case NonResponsive:
            return constants.nonResponsive();
        case Error:
            return constants.error();
        case Installing:
            return constants.installing();
        case InstallFailed:
            return constants.installFailed();
        case Reboot:
            return constants.reboot();
        case PreparingForMaintenance:
            return constants.preparingForMaintenance();
        case NonOperational:
            return constants.nonOperational();
        case PendingApproval:
            return constants.pendingApproval();
        case Initializing:
            return constants.initializing();
        case Connecting:
            return constants.connecting();
        case InstallingOS:
            return constants.installingOS();
        case Kdumping:
            return constants.kdumping();
        default:
            return null;
        }
    }

    private SafeHtml getAlertTooltipText(VDS vds) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();

        if (hasPMAlert(vds)) {
            appendLine(sb, constants.hostHasDisabledPowerManagment());
        }

        if (hasNetconfigDirty(vds)) {
            appendLine(sb, constants.hostNetConfigurationDirty());
        }

        if (hasGlusterAlert(vds)) {
            appendLine(sb, constants.hostGlusterIssues());
        }

        if (vds.getStaticData().isReinstallRequired()) {
            appendLine(sb, constants.hostReinstallRequired());
        }

        if (hasDefaultRouteAlert(vds)) {
            appendLine(sb, constants.hostHasNoDefaultRoute());
        }

        if (hasSmtAlert(vds)) {
            appendLine(sb, constants.hostSmtAlert());
        }

        if (cpuFlagsMissing(vds)) {
            appendLine(sb,
                    messages.hostHasMissingCpuFlagsTooltipAlert(
                            String.join(", ", vds.getCpuFlagsMissing())));//$NON-NLS-1$);
        }

        return sb.toSafeHtml();
    }

    private void appendLine(SafeHtmlBuilder sb, String text) {
        sb.append(templates.hostAlertTooltip(text));
    }

    private boolean hasNetconfigDirty(VDS vds) {
        return vds.getNetConfigDirty() != null && vds.getNetConfigDirty();
    }

    private boolean hasPMAlert(VDS vds) {
        return vds.getClusterSupportsVirtService() && !vds.isPmEnabled() && vds.isFencingEnabled();
    }

    private boolean hasGlusterAlert(VDS vds) {
        return vds.getClusterSupportsGlusterService() && vds.getGlusterPeerStatus() != PeerStatus.CONNECTED;
    }

    private boolean hasDefaultRouteAlert(VDS vds) {
        return !vds.isDefaultRouteRoleNetworkAttached();
    }

    private boolean cpuFlagsMissing(VDS vds) {
        return !vds.getCpuFlagsMissing().isEmpty();
    }

    private boolean hasSmtAlert(VDS vds) {
        return vds != null && (vds.hasSmtDiscrepancyAlert() || vds.hasSmtClusterDiscrepancyAlert());
    }
}
