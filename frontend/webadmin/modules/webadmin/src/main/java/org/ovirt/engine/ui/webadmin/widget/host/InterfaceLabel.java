package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.Arrays;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

public class InterfaceLabel extends Composite {
    private final static ApplicationResources resources = GWT.create(ApplicationResources.class);
    private final static ApplicationTemplates templates = GWT.create(ApplicationTemplates.class);
    private final static SafeHtml labelImage =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.tagImage()).getHTML());

    private final HTML label;

    public InterfaceLabel(VdsNetworkInterface iface) {
        label = createInterfaceLabel(iface);
        initWidget(label);
    }

    private HTML createInterfaceLabel(VdsNetworkInterface iface) {
        boolean hasLabels = iface.getLabels() != null
                && !iface.getLabels().isEmpty();
        HTML interfaceNameWithLabel =
                new HTML(hasLabels ? templates.textImageLabels(iface.getName(), labelImage)
                        : SafeHtmlUtils.fromString(iface.getName()));

        // TODO tt set tooltip of createLabelToolTip(iface.getLabels())

        return interfaceNameWithLabel;
    }

    private SafeHtml createLabelToolTip(Set<String> labels) {
        SafeHtmlBuilder tooltip = new SafeHtmlBuilder();
        boolean isFirst = true;

        if (labels == null) {
            return null;
        }

        String[] sortedLabels = labels.toArray(new String[] {});
        Arrays.sort(sortedLabels, new LexoNumericComparator());

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
}
