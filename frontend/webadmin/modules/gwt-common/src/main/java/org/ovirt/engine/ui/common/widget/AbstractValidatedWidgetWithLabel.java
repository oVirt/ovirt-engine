package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for validated widgets that have a label associated with them.
 * @param <W>
 *            Content widget type.
 */
public abstract class AbstractValidatedWidgetWithLabel<T, W extends EditorWidget<T, ?>> extends AbstractValidatedWidget
        implements HasLabel, HasEnabledWithHints, HasAccess, HasAllKeyHandlers, Focusable, HasElementId {

    interface WidgetUiBinder extends UiBinder<Widget, AbstractValidatedWidgetWithLabel<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {

        String labelEnabled();

        String labelDisabled();

        String labelHidden();
    }

    private final W contentWidget;

    @UiField
    HTMLPanel wrapperPanel;

    @UiField
    LabelElement labelElement;

    @UiField
    SimplePanel contentWidgetContainer;

    @UiField
    Style style;

    /**
     * By default the title gets erased, when the setEnabled is called
     * <p>
     * This switch disables it
     */
    private boolean keepTitleOnSetEnabled = false;

    private final VisibilityRenderer renderer;

    public AbstractValidatedWidgetWithLabel(W contentWidget, VisibilityRenderer renderer) {
        this.contentWidget = contentWidget;
        this.renderer = renderer;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public AbstractValidatedWidgetWithLabel(W contentWidget) {
        this(contentWidget, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);
        contentWidgetContainer.setWidget(contentWidget);

        // Adjust content widget width
        contentWidget.asWidget().setWidth("100%"); //$NON-NLS-1$

        // Assign ID to content widget element if it's missing or empty
        Element contentWidgetElement = getContentWidgetElement();
        if (contentWidgetElement.getId() == null || contentWidgetElement.getId().isEmpty()) {
            contentWidgetElement.setId(DOM.createUniqueId());
        }

        // Connect label with content widget for better accessibility
        updateLabelElementId(contentWidgetElement.getId());
    }

    protected void updateLabelElementId(String elementId) {
        labelElement.setHtmlFor(elementId);
    }

    protected W getContentWidget() {
        return contentWidget;
    }

    protected Element getContentWidgetElement() {
        return contentWidget.asWidget().getElement();
    }

    // TODO temporarily public, should be protected
    public SimplePanel getContentWidgetContainer() {
        return contentWidgetContainer;
    }

    protected LabelElement getLabelElement() {
        return labelElement;
    }

    @Override
    public void setElementId(String elementId) {
        getContentWidgetElement().setId(elementId);
        updateLabelElementId(elementId);
    }

    @Override
    protected Widget getValidatedWidget() {
        return getContentWidget().asWidget();
    }

    @Override
    public String getLabel() {
        return labelElement.getInnerText();
    }

    @Override
    public void setLabel(String label) {
        labelElement.setInnerText(label);
    }

    @Override
    public boolean isAccessible() {
        return wrapperPanel.isVisible();
    }

    @Override
    public void setAccessible(boolean accessible) {
        wrapperPanel.setVisible(renderer.render(this, accessible));
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return contentWidget.addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return contentWidget.addKeyPressHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return contentWidget.addKeyUpHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return contentWidget.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        contentWidget.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        contentWidget.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        contentWidget.setTabIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return contentWidget.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        contentWidget.setEnabled(enabled);

        if (enabled) {
            labelElement.replaceClassName(style.labelDisabled(), style.labelEnabled());
        } else {
            labelElement.replaceClassName(style.labelEnabled(), style.labelDisabled());
        }

        if (!keepTitleOnSetEnabled) {
            updateWidgetTitle(null);
        }
    }

    @Override
    public void disable(String disabilityHint) {
        setEnabled(false);
        updateWidgetTitle(disabilityHint);
    }

    private void updateWidgetTitle(String title) {
        contentWidget.asWidget().setTitle(title);
    }

    public void addContentWidgetStyleName(String styleName) {
        contentWidgetContainer.addStyleName(styleName);
    }

    public void setContentWidgetStyleName(String styleName) {
        contentWidgetContainer.setStyleName(styleName);
    }

    public void setLabelStyleName(String styleName) {
        labelElement.setClassName(styleName);
    }

    public void addLabelStyleName(String styleName) {
        labelElement.addClassName(styleName);
    }

    public void addWrapperStyleName(String styleName) {
        wrapperPanel.addStyleName(styleName);
    }

    public void hideLabel() {
        labelElement.addClassName(style.labelHidden());
    }

    public void setKeepTitleOnSetEnabled(boolean keepTitleOnSetEnabled) {
        this.keepTitleOnSetEnabled = keepTitleOnSetEnabled;
    }
}
