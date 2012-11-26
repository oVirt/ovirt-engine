package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
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

    ApplicationResources resources = ClientGinjectorProvider.instance().getApplicationResources();;

    @Override
    public void render(Context context, VM vm, SafeHtmlBuilder sb) {
        // Nothing to render if no vm is provided:
        if (vm == null) {
            return;
        }

        // Find the image corresponding to the status of the vm:
        VMStatus status = vm.getStatus();
        ImageResource statusImage = null;
        switch (status) {
        case Up:
            statusImage = resources.upImage();
            break;
        case PoweringUp:
        case RebootInProgress:
            statusImage = resources.playImage();
            break;
        case WaitForLaunch:
        case ImageLocked:
        case MigratingFrom:
        case MigratingTo:
            statusImage = resources.waitImage();
            break;
        case Suspended:
        case Paused:
            statusImage = resources.pauseImage();
            break;
        case Unknown:
            statusImage = resources.questionMarkImage();
            break;
        default:
            statusImage = resources.stopImage();
            break;
        }

        // Generate the HTML for status image
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());

        // Find the image corresponding to the alert
        SafeHtml alertImageHtml = getResourceImage(vm);

        ApplicationTemplates applicationTemplates = ClientGinjectorProvider.instance().getApplicationTemplates();

        if (alertImageHtml != null) {
            sb.append(applicationTemplates.statusWithAlertTemplate(statusImageHtml, alertImageHtml));
        } else {
            sb.append(applicationTemplates.statusTemplate(statusImageHtml));
        }

    }

    SafeHtml getResourceImage(VM vm) {

        if (vm.getVmPauseStatus() != VmPauseStatus.NONE || vm.getVmPauseStatus() != VmPauseStatus.NOERR)
        {
            return null;
        }
        else
        {
            // Create Image from the alert resource
            ImageResource alertImageResource = resources.alertImage();

            // Get the image html
            AbstractImagePrototype imagePrototype = AbstractImagePrototype.create(alertImageResource);
            String html = imagePrototype.getHTML();

            // Append tooltip
            Translator translator = EnumTranslator.Create(VmPauseStatus.class);
            String toolTip = translator.get(vm.getVmPauseStatus());
            html = html.replaceFirst("img", "img " + "title='" + toolTip + "' "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            return SafeHtmlUtils.fromTrustedString(html);
        }

    }
}
