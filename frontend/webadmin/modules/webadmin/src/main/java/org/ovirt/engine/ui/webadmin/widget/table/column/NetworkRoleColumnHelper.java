package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class NetworkRoleColumnHelper {

    private static final ApplicationTemplates templates = GWT.create(ApplicationTemplates.class);

    public static SafeHtml getValue(List<SafeHtml> imagesHtml) {
        String images = ""; //$NON-NLS-1$

        for (SafeHtml imageHtml : imagesHtml) {
            images = images.concat(imageHtml.asString());
        }

        return templates.image(SafeHtmlUtils.fromTrustedString(images));
    }

    public static SafeHtml getTooltip(Map<SafeHtml, String> imagesToText) {
        String tooltip = ""; //$NON-NLS-1$

        for (Map.Entry<SafeHtml, String> imageToText : imagesToText.entrySet()) {
            if (!tooltip.isEmpty()) {
                tooltip = tooltip.concat("<BR>"); //$NON-NLS-1$
            }
            tooltip = tooltip.concat(templates.imageTextSetupNetwork(imageToText.getKey(),
                    imageToText.getValue()).asString());
        }

        return SafeHtmlUtils.fromTrustedString(tooltip);
    }

}
