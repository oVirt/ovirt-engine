package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.PatternflyConstants;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for validated widgets that have a label associated with them.
 * @param <W>
 *            Content widget type.
 */
public abstract class AbstractValidatedWidgetWithLabel<T, W extends EditorWidget<T, ?> & TakesValue<T> & HasValueChangeHandlers<T>> extends AbstractValidatedWidget
        implements HasLabel, HasEnabledWithHints, HasAccess, HasAllKeyHandlers, HasElementId, Focusable, FocusableComponentsContainer {

    interface WidgetUiBinder extends UiBinder<Widget, AbstractValidatedWidgetWithLabel<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {

        String labelEnabled();

        String labelEnabledPatternFly();

        String labelDisabled();

        String labelHidden();

        String wrapper();

        String contentWidget();
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

    // width in PX
    public static final int CONTENT_WIDTH = 230;

    /**
     * By default the title gets erased, when the setEnabled is called
     * <p>
     * This switch disables it
     */
    private boolean keepTitleOnSetEnabled = false;

    private VisibilityRenderer renderer;

    private boolean usePatternFly;

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
        updateLabelElementId(getContentWidgetElement().getId());
    }

    protected void updateLabelElementId(String elementId) {
        labelElement.setHtmlFor(elementId);
    }

    public void setUsePatternFly(final boolean use) {
        this.usePatternFly = use;
        if (use) {
            // set the style to the bootstrap / patternfly style
            setContentWidgetStyleName(PatternflyConstants.FORM_CONTROL);
            // Set the content width back to default.
            addLabelStyleName("label col-sm-2 col-md-2 control-label"); //$NON-NLS-1$
            addContentWidgetContainerStyleName("col-sm-10 col-md-10"); //$NON-NLS-1$
            wrapperPanel.getElement().addClassName(PatternflyConstants.FORM_GROUP);
            wrapperPanel.getElement().removeClassName(style.wrapper());
            contentWidgetContainer.asWidget().removeStyleName(style.contentWidget());
        }
    }

    /**
     * Render widget more responsive, by firing {@link ValueChangeEvent} on each {@link KeyDownEvent}.
     */
    public void fireValueChangeOnKeyDown() {
        getContentWidget().addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                // deferring is required to allow the widget's internal value to update according to key press
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @Override
                    public void execute() {
                        ValueChangeEvent.fire(getContentWidget(), getContentWidget().getValue());
                    }
                });
            }
        });
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

    public void setLabelStyleNames(String styleNames) {
        for (String name : styleNames.split("[,\\s]+")) { //$NON-NLS-1$
            labelElement.addClassName(name);
        }
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
    public int setTabIndexes(int nextTabIndex) {
        setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    @Override
    public boolean isEnabled() {
        return contentWidget.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        contentWidget.setEnabled(enabled);

        if (enabled) {
            if (usePatternFly) {
                getLabelElement().replaceClassName(style.labelDisabled(), style.labelEnabledPatternFly());
            } else {
                getLabelElement().replaceClassName(style.labelDisabled(), style.labelEnabled());
            }
        } else {
            if (usePatternFly) {
                getLabelElement().replaceClassName(style.labelEnabledPatternFly(), style.labelDisabled());
                // In case the style got added somewhere else, remove it.
                getLabelElement().removeClassName(style.labelEnabled());
            } else {
                getLabelElement().replaceClassName(style.labelEnabled(), style.labelDisabled());
            }
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
        if (usePatternFly) {
            getContentWidgetElement().addClassName(styleName);
        } else {
            contentWidgetContainer.addStyleName(styleName);
        }
    }

    public void setContentWidgetStyleName(String styleName) {
        if (usePatternFly) {
            getContentWidgetElement().setClassName(styleName);
        } else {
            contentWidgetContainer.setStyleName(styleName);
        }
    }

    public void addContentWidgetContainerStyleName(String styleName) {
        contentWidgetContainer.addStyleName(styleName);
    }

    public void setContentWidgetContainerStyleName(String styleName) {
        contentWidgetContainer.setStyleName(styleName);
    }

    public void setLabelStyleName(String styleName) {
        getLabelElement().setClassName(styleName);
    }

    public void addLabelStyleName(String styleName) {
        getLabelElement().addClassName(styleName);
    }

    public void addWrapperStyleName(String styleName) {
        wrapperPanel.addStyleName(styleName);
    }

    public void hideLabel() {
        getLabelElement().addClassName(style.labelHidden());
    }

    public void setKeepTitleOnSetEnabled(boolean keepTitleOnSetEnabled) {
        this.keepTitleOnSetEnabled = keepTitleOnSetEnabled;
    }

    public VisibilityRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(VisibilityRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Force fire a change event on this field. This will trigger editor and model
     * population from the field without a user edit and blur.
     */
    public void fireChangeEvent() {
        ValueChangeEvent.fire(getContentWidget(), getContentWidget().getValue());
    }
}
