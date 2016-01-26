package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.Arrays;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

public class InterfaceLabel extends Composite {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    private static final SafeHtml labelImage =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.tagImage()).getHTML());

    private final HTML label;
    private WidgetTooltip tooltip;

    public InterfaceLabel(VdsNetworkInterface iface) {
        label = createInterfaceLabel(iface);
        initWidget(label);
    }

    private HTML createInterfaceLabel(VdsNetworkInterface iface) {
        HTML label = null;
        if (iface.getLabels() != null && !iface.getLabels().isEmpty()) {
            label = new HTML(templates.textImageLabels(iface.getName(), labelImage));
        }
        else {
            label = new HTML(iface.getName());
        }

        tooltip = new WidgetTooltip(label);
        tooltip.setHtml(getTooltip(iface.getLabels()));

        return label;
    }

    private SafeHtml getTooltip(Set<String> labels) {
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
