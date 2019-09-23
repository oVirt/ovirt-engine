package org.ovirt.engine.ui.common.widget.tooltip;

import java.util.Collections;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.WidgetDecorator;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

/**
 * Decorates a {@link Widget} with jQuery/Bootstrap tooltip.
 */
public class WidgetTooltip extends WidgetDecorator implements HasCleanup {

    private SafeHtml tooltip;

    private final TooltipConfig tooltipConfig = new TooltipConfig();

    private boolean widgetAttached = false;
    private boolean tooltipUpdateScheduled = false;

    public WidgetTooltip() {
        super();
    }

    public WidgetTooltip(Widget w) {
        super(w);
    }

    @Override
    protected void decorateWidget(Widget w) {
        w.addAttachHandler(event -> {
            WidgetTooltip.this.widgetAttached = event.isAttached();
            scheduleTooltipUpdate();
        });
    }

    private void scheduleTooltipUpdate() {
        if (tooltipUpdateScheduled) {
            return;
        }

        Scheduler.get().scheduleFinally(() -> {
            applyTooltip();
            tooltipUpdateScheduled = false;
        });

        tooltipUpdateScheduled = true;
    }

    private void applyTooltip() {
        if (getWidget() == null) {
            return;
        }

        // Apply tooltip if the widget is attached to live DOM.
        if (widgetAttached && tooltip != null) {
            ElementTooltipUtils.setOrReplaceTooltipOnElement(getWidget().getElement(), tooltip, tooltipConfig);
        } else if (!widgetAttached) {
            // Destroy tooltip if the widget is detached from live DOM.
            ElementTooltipUtils.destroyTooltip(getWidget().getElement());
        }
    }

    public void setHtml(SafeHtml html) {
        if (html == null) {
            return;
        }

        if (tooltip == null || !tooltip.asString().equals(html.asString())) {
            tooltip = html;
            scheduleTooltipUpdate();
        }
    }

    public void setText(String text) {
        String nullSafeText = (text == null) ? "" : text;
        setHtml(SafeHtmlUtils.fromString(nullSafeText));
    }

    public void setPlacement(Placement placement) {
        tooltipConfig.setPlacement(Collections.singletonList(placement));
    }

    public void setPlacementList(List<Placement> placementList) {
        tooltipConfig.setPlacement(placementList);
    }

    public void setMaxWidth(TooltipWidth width) {
        tooltipConfig.addTooltipClassName(width.getClassName());
    }

    public void setSanitizeContent(boolean sanitize) {
        tooltipConfig.setSanitizeContent(sanitize);
    }

    public void hide() {
        if (getWidget() != null) {
            ElementTooltipUtils.hideTooltip(getWidget().getElement());
        }
    }

    public void destroy() {
        if (getWidget() != null) {
            ElementTooltipUtils.destroyTooltip(getWidget().getElement());
        }
    }

    @Override
    public void cleanup() {
        destroy();
    }

}
