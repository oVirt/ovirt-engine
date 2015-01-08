package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VmStatusCell extends AbstractCell<VM> {

    ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();
    CommonApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    @Override
    public void render(Context context, VM vm, SafeHtmlBuilder sb) {
        // Nothing to render if no vm is provided:
        if (vm == null) {
            return;
        }

        // Find the image corresponding to the status of the vm:
        VMStatus status = vm.getStatus();
        ImageResource statusImage;
        String tooltip;

        switch (status) {
        case Up:
            if (vm.isRunOnce()) {
                tooltip = constants.runOnce();
                statusImage = resources.runOnceUpImage();
            } else {
                tooltip = constants.up();
                statusImage = resources.vmStatusRunning();
            }
            break;
        case SavingState:
            tooltip = constants.vmStatusSaving();
            statusImage = resources.vmStatusWait();
            break;
        case RestoringState:
            tooltip = constants.restoring();
            statusImage = resources.vmStatusWait();
            break;
        case PoweringUp:
            tooltip = constants.poweringUp();
            statusImage = resources.vmStatusStarting();
            break;
        case PoweringDown:
            tooltip = constants.poweringDown();
            statusImage = resources.vmStatusPoweringDown();
            break;
        case RebootInProgress:
            tooltip = constants.rebooting();
            statusImage = resources.rebootInProgress();
            break;
        case WaitForLaunch:
            tooltip = constants.waitForLaunchStatus();
            statusImage = resources.waitForLaunch();
            break;
        case ImageLocked:
            tooltip = constants.imageLocked();
            statusImage = resources.vmStatusWait();
            break;
        case MigratingFrom:
        case MigratingTo:
            tooltip = constants.migrating();
            statusImage = resources.migrationImage();
            break;
        case Suspended:
            tooltip = constants.suspended();
            statusImage = resources.suspendedImage();
            break;
        case Paused:
            tooltip = constants.paused();
            statusImage = resources.pauseImage();
            break;
        case Unknown:
        case Unassigned:
            tooltip = constants.unknown();
            statusImage = resources.questionMarkImage();
            break;
        case NotResponding:
            tooltip = constants.notResponding();
            statusImage = resources.questionMarkImage();
            break;
        default:
            tooltip = constants.down();
            statusImage = resources.downStatusImage();
            break;
        }

        // Generate the HTML for status image
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());

        // Find the image corresponding to the alert
        SafeHtml alertImageHtml = getResourceImage(vm);

        ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

        if (alertImageHtml != null) {
            // this already has the tooltip set
            sb.append(applicationTemplates.statusWithAlertTemplate(statusImageHtml, alertImageHtml));
        } else {
            sb.append(applicationTemplates.statusTemplate(statusImageHtml, tooltip));
        }

    }

    SafeHtml getResourceImage(VM vm) {
        boolean updateNeeded = vm.getStatus() == VMStatus.Up && vm.getGuestAgentStatus() == GuestAgentStatus.UpdateNeeded;
        if (!updateNeeded && (vm.getVmPauseStatus() != VmPauseStatus.NONE || vm.getVmPauseStatus() != VmPauseStatus.NOERR)) {
            return null;
        }
        else {
            // Create Image from the alert resource
            ImageResource alertImageResource = resources.alertImage();

            // Get the image html
            AbstractImagePrototype imagePrototype = AbstractImagePrototype.create(alertImageResource);
            String html = imagePrototype.getHTML();

            // Append tooltip
            Translator translator = EnumTranslator.getInstance();
            String toolTip = updateNeeded ? constants.newtools() : translator.get(vm.getVmPauseStatus());
            html = html.replaceFirst("img", "img " + "title='" + toolTip + "' "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            return SafeHtmlUtils.fromTrustedString(html);
        }

    }
}
