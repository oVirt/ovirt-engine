package org.ovirt.engine.ui.common.widget.tooltip;

import org.gwtbootstrap3.client.ui.Popover;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class OvirtPopover extends Popover {
    protected static final String HASH = "#"; // $NON-NLS-1$
    private static final String CONTAINER = "container"; // $NON-NLS-1$

    private FlowPanel contentContainer = new FlowPanel();
    private String contentId;
    private IsWidget content;
    private boolean isVisible;

    public OvirtPopover() {
        super();
    }

    public OvirtPopover(String title, String content) {
        super(title, content);
    }

    public OvirtPopover(String title) {
        super(title);
    }

    public OvirtPopover(Widget w, String title, String content) {
        super(w, title, content);
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

    @Override
    public void show() {
        Scheduler.get().scheduleDeferred( ()-> {
            isVisible = true;
        });
        super.show();
    }

    @Override
    public void hide() {
        isVisible = false;
        super.hide();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void addTitle(IsWidget titleWidget) {
        contentContainer.add(titleWidget);
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
        popover(element, baseOptions, getContent(), HASH + contentId, HASH + contentContainer.getElement().getId());
        bindJavaScriptEvents(element);
        setInitialized(true);
    }

    /**
     * Create the popover.
     */
    private native void popover(Element e, JavaScriptObject options, String content, String contentId, String containerId) /*-{
        var target = this;
        var dataTarget = target.@org.gwtbootstrap3.client.ui.base.AbstractTooltip::dataTarget;
        var content;
        options['content'] = function() {
            return $wnd.jQuery(contentId);
        };
        $wnd.jQuery(e).popover(options).on('hide.' + dataTarget, function (evt) {
            content = $wnd.jQuery(contentId);
        }).on('hidden.' + dataTarget, function (evt) {
            $wnd.jQuery(containerId).append(content);
        });
    }-*/;

}
