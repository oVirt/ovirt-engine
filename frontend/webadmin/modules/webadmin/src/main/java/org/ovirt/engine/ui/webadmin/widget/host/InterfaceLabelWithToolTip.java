package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.Arrays;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.widget.label.LabelWithToolTip;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class InterfaceLabelWithToolTip {
    private final static ApplicationResources resources = GWT.create(ApplicationResources.class);
    private final static ApplicationTemplates templates = GWT.create(ApplicationTemplates.class);
    private final static SafeHtml labelImage =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.tagImage()).getHTML());

    private final LabelWithToolTip label;

    public InterfaceLabelWithToolTip(VdsNetworkInterface iface) {
        label = createInterfaceLabel(iface);
    }

    private LabelWithToolTip createInterfaceLabel(VdsNetworkInterface iface) {
        boolean hasLabels = iface.getLabels() != null
                && !iface.getLabels().isEmpty();
        LabelWithToolTip interfaceNameWithLabel =
                new LabelWithToolTip(hasLabels ? templates.textImageLabels(iface.getName(), labelImage)
                        : SafeHtmlUtils.fromString(iface.getName()));

        interfaceNameWithLabel.setTitle(createLabelToolTip(iface.getLabels()));

        return interfaceNameWithLabel;
    }

    private SafeHtml createLabelToolTip(Set<String> labels) {
        SafeHtmlBuilder tooltip = new SafeHtmlBuilder(); //$NON-NLS-1$
        boolean isFirst = true;

        if (labels == null) {
            return null;
        }

        String[] sortedLabels = labels.toArray(new String[] {});
        Arrays.sort(sortedLabels);

        for (String label : sortedLabels) {
            if (isFirst) {
                isFirst = false;
            } else {
                tooltip = tooltip.appendHtmlConstant("<BR>"); //$NON-NLS-1$
            }

            tooltip = tooltip.appendEscaped(label);
        }

        return tooltip.toSafeHtml();
    }

    public LabelWithToolTip getLabel() {
        return label;
    }
}
