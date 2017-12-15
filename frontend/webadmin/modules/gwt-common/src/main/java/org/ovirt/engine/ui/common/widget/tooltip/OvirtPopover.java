package org.ovirt.engine.ui.common.widget.tooltip;

import org.gwtbootstrap3.client.ui.Popover;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class OvirtPopover extends Popover {

    protected static final String HASH = "#"; // $NON-NLS-1$
    private static final String CONTAINER = "container"; // $NON-NLS-1$

    private FlowPanel contentContainer = new FlowPanel();
    private String contentId;
    private IsWidget content;
    private boolean isVisible;
    private HandlerRegistration autoCloseHandler;
    private boolean autoClose = false;

    public OvirtPopover() {
        super();
    }

    public OvirtPopover(Widget w) {
        super(w);
    }

    /**
     * Set the widget into which the content for this pop-over will be put while the pop-over is NOT visible. The
     * content will be set to invisible so it will not interfere with the layout of the container. This needs to be
     * called BEFORE addContent otherwise the content will never get attached to the DOM.
     */
    public void setContainer(ForIsWidget container) {
        container.add(this.contentContainer);
    }

    public void setAutoClose(boolean value) {
        this.autoClose = value;
    }

    @Override
    public void show() {
        Scheduler.get().scheduleDeferred( ()-> {
            isVisible = true;
        });
        if (autoClose) {
            attachAutoCloseHandler();
        }
        super.show();
    }

    @Override
    public void hide() {
        isVisible = false;
        removeAutoCloseHandler();
        super.hide();
    }

    public boolean isVisible() {
        return isVisible;
    }

    private void attachAutoCloseHandler() {
        removeAutoCloseHandler();
        autoCloseHandler = RootPanel.get().addDomHandler(e -> {
            if (isVisible()) {
                int top = content.asWidget().getElement().getAbsoluteTop();
                int left = content.asWidget().getElement().getAbsoluteLeft();
                int right = content.asWidget().getElement().getAbsoluteRight();
                int bottom = content.asWidget().getElement().getAbsoluteBottom();
                if (e.getY() < top || e.getY() > bottom || e.getX() < left || e.getX() > right) {
                    hide();
                }
            }
        }, ClickEvent.getType());
    }

    public void removeAutoCloseHandler() {
        if (autoCloseHandler != null) {
            autoCloseHandler.removeHandler();
            autoCloseHandler = null;
        }
    }

    public void addContent(IsWidget content, String contentId) {
        contentContainer.add(content);
        contentContainer.getElement().setId(contentId + CONTAINER);
        this.content = content;
        content.asWidget().getElement().setId(contentId);
        this.contentId = contentId;
    }

    public IsWidget getContentWidget() {
        return content;
    }

    @Override
    public void init() {
        this.contentContainer.setVisible(false);
        if (contentContainer.getWidgetCount() > 0) {
            setIsHtml(true);
        }
        addShowHandler(e -> {
            for (int i = 0; i < contentContainer.getWidgetCount(); i++) {
                contentContainer.getWidget(0).setVisible(true);
            }
        });

        Element element = getWidget().getElement();
        JavaScriptObject baseOptions = createOptions(element, isAnimated(), isHtml(), getSelector(),
                getTrigger().getCssName(), getShowDelayMs(), getHideDelayMs(), getContainer(), prepareTemplate(),
                getViewportSelector(), getViewportPadding());
        popover(element, baseOptions, HASH + contentId, HASH + contentContainer.getElement().getId());
        bindJavaScriptEvents(element);
        setInitialized(true);
    }

    /**
     * Create the popover.
     */
    private native void popover(Element e, JavaScriptObject options, String contentId, String containerId) /*-{
        var dataTarget = this.@org.gwtbootstrap3.client.ui.base.AbstractTooltip::dataTarget;
        var content;

        options['content'] = function() {
            return $wnd.jQuery(contentId);
        };

        $wnd.jQuery(e).popover(options)
            .on('hide.' + dataTarget, function () {
                content = $wnd.jQuery(contentId);
            })
            .on('hidden.' + dataTarget, function () {
                $wnd.jQuery(containerId).append(content);
                content = null;
            });
    }-*/;

}
