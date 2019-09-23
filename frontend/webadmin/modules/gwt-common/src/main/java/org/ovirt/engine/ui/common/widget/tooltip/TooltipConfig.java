package org.ovirt.engine.ui.common.widget.tooltip;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.core.compat.StringHelper;

public class TooltipConfig {

    public interface Defaults {

        /**
         * Tooltip HTML template.
         * <p>
         * <code>{0}</code> should contain {@value TEMPLATE_CLASS_OUTER} <br>
         * <code>{1}</code> should contain {@value TEMPLATE_CLASS_ARROW} <br>
         * <code>{2}</code> should contain {@value TEMPLATE_CLASS_INNER}
         */
        String TEMPLATE = "<div class=\"{0}\"><div class=\"{1}\"></div><div class=\"{2}\"></div></div>"; //$NON-NLS-1$

        String TEMPLATE_CLASS_OUTER = "tooltip"; //$NON-NLS-1$
        String TEMPLATE_CLASS_ARROW = "tooltip-arrow"; //$NON-NLS-1$
        String TEMPLATE_CLASS_INNER = "tooltip-inner"; //$NON-NLS-1$

        Placement PLACEMENT = Placement.TOP;

    }

    private List<Placement> placement = Collections.singletonList(Defaults.PLACEMENT);
    private Set<String> extraTooltipClassNames = new LinkedHashSet<>();
    private boolean forceShow = false;
    private boolean forCellWidgetElement = false;
    private boolean sanitizeContent = true;

    public TooltipConfig setPlacement(List<Placement> placementList) {
        if (placementList != null) {
            this.placement = placementList;
        }
        return this;
    }

    public List<Placement> getPlacement() {
        return placement;
    }

    public TooltipConfig addTooltipClassName(String className) {
        if (StringHelper.isNotNullOrEmpty(className)) {
            extraTooltipClassNames.add(className);
        }
        return this;
    }

    public String getTooltipTemplate() {
        String template = Defaults.TEMPLATE;
        template = template.replace("{0}", getOuterClassNames()); //$NON-NLS-1$
        template = template.replace("{1}", Defaults.TEMPLATE_CLASS_ARROW); //$NON-NLS-1$
        template = template.replace("{2}", Defaults.TEMPLATE_CLASS_INNER); //$NON-NLS-1$
        return template;
    }

    private String getOuterClassNames() {
        Set<String> classNames = new LinkedHashSet<>();
        classNames.add(Defaults.TEMPLATE_CLASS_OUTER);
        classNames.addAll(extraTooltipClassNames);
        return String.join(" ", classNames); //$NON-NLS-1$
    }

    public TooltipConfig setForceShow() {
        forceShow = true;
        return this;
    }

    public boolean isForceShow() {
        return forceShow;
    }

    public TooltipConfig markAsCellWidgetTooltip() {
        forCellWidgetElement = true;
        return this;
    }

    public boolean isForCellWidgetElement() {
        return forCellWidgetElement;
    }

    public boolean isSanitizeContent() {
        return sanitizeContent;
    }

    public void setSanitizeContent(boolean sanitizeContent) {
        this.sanitizeContent = sanitizeContent;
    }
}
