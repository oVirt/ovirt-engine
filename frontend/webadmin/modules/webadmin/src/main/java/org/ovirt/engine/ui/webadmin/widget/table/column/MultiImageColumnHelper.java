package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class MultiImageColumnHelper {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

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
                tooltip = tooltip.concat(constants.lineBreak()); //$NON-NLS-1$
            }
            tooltip = tooltip.concat(templates.imageWithText(imageToText.getKey(),
                    imageToText.getValue()).asString());
        }

        return SafeHtmlUtils.fromTrustedString(tooltip);
    }

    public static SafeHtml getTooltipFromSafeHtml(Map<SafeHtml, SafeHtml> imagesToText) {
        String tooltip = ""; //$NON-NLS-1$

        for (Map.Entry<SafeHtml, SafeHtml> imageToText : imagesToText.entrySet()) {
            if (!tooltip.isEmpty()) {
                tooltip = tooltip.concat(constants.lineBreak()); //$NON-NLS-1$
            }
            tooltip = tooltip.concat(templates.imageWithSafeHtml(imageToText.getKey(),
                    imageToText.getValue()).asString());
        }

        return SafeHtmlUtils.fromTrustedString(tooltip);
    }
}
