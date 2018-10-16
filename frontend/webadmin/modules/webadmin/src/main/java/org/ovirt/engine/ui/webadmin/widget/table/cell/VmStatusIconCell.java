package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusIconColumn;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VmStatusIconCell extends AbstractCell<VM> {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    @Override
    public void render(Context context, VM vm, SafeHtmlBuilder sb, String id) {

        // Nothing to render if no vm is provided:
        if (vm == null) {
            return;
        }

        // Find the image corresponding to the status of the vm:
        VMStatus status = vm.getStatus();
        ImageResource statusImage;

        switch (status) {
        case Up:
            if (vm.isRunOnce()) {
                statusImage = resources.runOnceUpImage();
            } else {
                statusImage = resources.vmStatusRunning();
            }
            break;
        case SavingState:
            statusImage = resources.vmStatusWait();
            break;
        case RestoringState:
            statusImage = resources.vmStatusWait();
            break;
        case PoweringUp:
            statusImage = resources.vmStatusStarting();
            break;
        case PoweringDown:
            statusImage = resources.vmStatusPoweringDown();
            break;
        case RebootInProgress:
            statusImage = resources.rebootInProgress();
            break;
        case WaitForLaunch:
            statusImage = resources.waitForLaunch();
            break;
        case ImageLocked:
            statusImage = resources.vmStatusWait();
            break;
        case MigratingFrom:
        case MigratingTo:
            statusImage = resources.migrationImage();
            break;
        case Suspended:
            statusImage = resources.suspendedImage();
            break;
        case Paused:
            statusImage = resources.pauseImage();
            break;
        case Unknown:
        case Unassigned:
            statusImage = resources.questionMarkImage();
            break;
        case NotResponding:
            statusImage = resources.questionMarkImage();
            break;
        default:
            statusImage = resources.downStatusImage();
            break;
        }

        // Generate the HTML for status image
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());

        if (vm.getLockInfo() != null && vm.getLockInfo().isExclusive()) {
            ImageResource lockImageResource = resources.vmLocked();
            // Get the image html
            AbstractImagePrototype imagePrototype = AbstractImagePrototype.create(lockImageResource);
            SafeHtml lockImageHtml = SafeHtmlUtils.fromTrustedString(imagePrototype.getHTML());
            statusImageHtml = templates.lockedStatusTemplate(lockImageHtml, statusImageHtml);
        }

        if (VmStatusIconColumn.needsAlert(vm)) {
            sb.append(templates.statusWithAlertTemplate(statusImageHtml, getAlertImageResource(vm), id, status.toString()));
        } else {
            sb.append(templates.statusTemplate(statusImageHtml, id, status.toString()));
        }

    }

    private static SafeHtml getAlertImageResource(VM vm) {
        ImageResource alertImageResource = resources.alertImage();
        AbstractImagePrototype imagePrototype = AbstractImagePrototype.create(alertImageResource);
        String html = imagePrototype.getHTML();
        return SafeHtmlUtils.fromTrustedString(html);
    }

}
