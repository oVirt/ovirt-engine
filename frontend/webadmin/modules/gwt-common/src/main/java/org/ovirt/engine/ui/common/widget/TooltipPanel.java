package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Tool-tip panel that gets attached to a {@code Widget} when certain mouse events happen the tool-tip will either
 * be shown or hidden.
 */
public class TooltipPanel extends ElementAwareDecoratedPopupPanel {

    /**
     * The Native Event preview handler registration.
     */
    private HandlerRegistration tooltipNativePreviewHandlerRegistration;
    private boolean nonWidget = false;

    /**
     * Constructor.
     * @param autoHide Auto-hide the panel when leaving it.
     * @param tooltipSource The {@code Widget} that the tool-tip should be applied to.
     */
    public TooltipPanel(boolean autoHide, final Widget tooltipSource) {
        this(autoHide);
        applyTo(tooltipSource);
    }

    /**
     * Constructor.
     * @param autoHide Auto-hide the panel when leaving it.
     */
    public TooltipPanel(boolean autoHide) {
        super(autoHide);
        getElement().getStyle().setZIndex(1);
    }

    /**
     * Default constructor, auto-hide is false.
     */
    public TooltipPanel() {
        this(true);
    }

    /**
     * Set the tool-tip text, if the owner hasn't provided its own widget. If you pass null the text will be a
     * blank string
     * @param text The tool-tip text as a {@code String}.
     */
    public void setText(String text) {
        if (text != null) {
            SafeHtml safeText = SafeHtmlUtils.fromSafeConstant(text);
            setText(safeText);
        } else {
            setText((SafeHtml)null);
        }
    }

    /**
     * Set the tool-tip text, if the owner hasn't provided its own widget. If you pass null the text will be a
     * blank string
     * @param text The tool-tip text as an {@code SafeHtml}.
     */
    public void setText(SafeHtml text) {
        if (!isTextEmpty(text)) {
            setWidget(new HTML(text));
        } else {
            setWidget(null);
        }
    }

    /**
     * Determine if the passed in {@code SafeHtml} text is not empty.
     * @param text The text to check.
     * @return {@code true} if the text is not empty, false otherwise.
     */
    private boolean isTextEmpty(SafeHtml text) {
        return text == null || "".equals(text.asString());
    }

    /**
     * Apply the tool-tip panel to the supplied source {@code Widget}.
     * @param tooltipSource The {@code Widget} that the panel should be applied to.
     */
    public void applyTo(final Widget tooltipSource) {
        tooltipSource.addAttachHandler(new AttachEvent.Handler() {

            @Override
            public void onAttachOrDetach(AttachEvent event) {
                //Only attach the preview handlers if the widget is attached to the DOM.
                if (event.isAttached()) {
                    registerPreviewHandler(tooltipSource);
                } else {
                    removeHandlerRegistration();
                }
            }
        });
    }

    /**
     * Register the event preview handler.
     * @param tooltipSource The {@code Widget} that needs to be compared against.
     */
    private void registerPreviewHandler(final Widget tooltipSource) {
        if (tooltipNativePreviewHandlerRegistration != null) {
            return;
        }
        tooltipNativePreviewHandlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (tooltipSource.isVisible()) {
                    Element tooltipSourceElement = tooltipSource.getElement();
                    handlePreviewEvent(event, tooltipSourceElement);
                }
            }
        });
    }

    /**
     * Apply the tool-tip panel to the supplied source {@code Element}. Use this one if you don't have a
     * {@code Widget} to pass into the tool-tip.
     * @param tooltipSource The {@code Element} that the panel should be applied to.
     */
    public void applyTo(final Element tooltipSource) {
        if (tooltipNativePreviewHandlerRegistration != null) {
            return;
        }
        //Mark that we are attached to a non widget, so we can't use the onDetachAttach handler to manage the
        //handlers.
        nonWidget = true;
        tooltipNativePreviewHandlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                handlePreviewEvent(event, tooltipSource);
            }
        });
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (nonWidget) {
            //We are attached to a non widget, remove the handlerRegistration.
            removeHandlerRegistration();
        }
    }

    /**
     * Remove the handler registration if it exists.
     */
    private void removeHandlerRegistration() {
        if (tooltipNativePreviewHandlerRegistration != null) {
            tooltipNativePreviewHandlerRegistration.removeHandler();
            tooltipNativePreviewHandlerRegistration = null;
        }
    };

    /**
     * Allow creators of the tool-tip panel to add extra functionality for mouse over events.
     */
    protected void onTooltipSourceMouseOver() {
        // No-op, override as necessary
    }

    /**
     * Allow creators of the tool-tip panel to add extra functionality for mouse out events.
     */
    protected void onTooltipSourceMouseOut() {
        // No-op, override as necessary
    }

    /**
     * Use handleNativeBrowserEvent, if you don't even have an {@code Element} to use as the source
     * of the tool-tip. For instance in a GWT CellTable cell.
     * @param tooltipSource The element that is the source of the event.
     * @param event The native event.
     */
    public void handleNativeBrowserEvent(final Element tooltipSource, NativeEvent event) {
        if (BrowserEvents.MOUSEOUT.equals(event.getType())) {
            hideTooltipPanel();
        } else if (BrowserEvents.MOUSEOVER.equals(event.getType())) {
            displayTooltipPanel(tooltipSource);
        }
    }

    /**
     * Display the tool-tip.
     * @param tooltipSource The {@code Element} to display the tool-tip for, the tool-tip is positioned relative to
     * the passed in element.
     */
    private void displayTooltipPanel(final Element tooltipSource) {
        if (getWidget() != null && !isShowing()) {
            onTooltipSourceMouseOver();
            TooltipPanel.this.showRelativeTo(tooltipSource);
        }
    }

    /**
     * Hide the tooltip if it is showing.
     */
    private void hideTooltipPanel() {
        onTooltipSourceMouseOut();
        TooltipPanel.this.hide(true);
    }

    /**
     * Handle the preview event.
     * @param event The {@code NativePreviewEvent} to preview.
     * @param tooltipSourceElement The source of the event.
     */
    private void handlePreviewEvent(NativePreviewEvent event, Element tooltipSourceElement) {
        //Don't do anything if we can't display anything anyway.
        if (getWidget() == null) {
            return;
        }
        int type = event.getTypeInt();
        switch (type) {
            case Event.ONMOUSEDOWN:
            case Event.ONMOUSEUP:
            case Event.ONMOUSEMOVE:
            case Event.ONCLICK:
            case Event.ONDBLCLICK:
                if (mouseOnSourceElement(event.getNativeEvent().getClientX(),
                        event.getNativeEvent().getClientY(), tooltipSourceElement)) {
                    EventTarget target = event.getNativeEvent().getEventTarget();
                    if (Element.is(target)) {
                        Element element = Element.as(target);
                        //Either the element or any of its children (like an image can be the target.
                        NodeList<Node> children = element.getChildNodes();
                        for (int i = 0; i < children.getLength(); i++) {
                            Node child = children.getItem(i);
                            if (child instanceof Element) {
                                if (child.equals(tooltipSourceElement)) {
                                    displayTooltipPanel(tooltipSourceElement);
                                    return;
                                }
                            }
                        }
                        if (element.equals(tooltipSourceElement)) {
                            displayTooltipPanel(tooltipSourceElement);
                        }
                    }
                } else {
                    hideTooltipPanel();
                }
                break;
        }
    }

    /**
     * Determine if the X and Y position passed in is inside the display box of the passed in {@code Element}. The
     * left side and bottom side are taking into account but not the top and right, so you don't have overlapping on
     * two elements next to each other.
     * @param clientX The client X position in pixels
     * @param clientY The client Y position in pixels
     * @param tooltipSource The {@code Element} to compare against.
     * @return {@code true} if the clientX and clientY are within the box of the element, false otherwise.
     */
    private boolean mouseOnSourceElement(int clientX, int clientY, Element tooltipSource) {
        return clientX >= tooltipSource.getAbsoluteLeft() && clientX < tooltipSource.getAbsoluteRight()
                && clientY <= tooltipSource.getAbsoluteBottom() && clientY > tooltipSource.getAbsoluteTop();
    }
}
