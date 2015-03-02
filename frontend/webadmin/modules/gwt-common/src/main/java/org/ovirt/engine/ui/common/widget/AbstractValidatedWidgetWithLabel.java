package org.ovirt.engine.ui.common.widget;

import java.util.List;

import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
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
 * <p>
 * Base class for validated widgets that have a label associated with them.
 * </p>
 * <p>
 * This widget can run in legacy mode or PatternFly mode. Legacy mode uses absolute positioning
 * with hardcoded pixels, and should be avoided. PatternFly mode makes use of PatternFly (Bootstrap)
 * grid positioning, and is preferred.
 * </p>
 * <p>
 * To enable PatternFly mode,  call setUsePatternFly(true). You'll also probably want to set
 * PatternFly grid classes on both the label and the widget container. E.g: <br/>
 * addLabelStyleName(Styles.SM_2);<br/>
 * addContentWidgetContainerStyleName(Styles.SM_10);<br/>
 * </p>
 * @param <W>
 *            Content widget type.
 */
public abstract class AbstractValidatedWidgetWithLabel<T, W extends EditorWidget<T, ?> & TakesValue<T> & HasValueChangeHandlers<T>> extends AbstractValidatedWidget
        implements HasLabel, HasEnabledWithHints, HasAccess, HasAllKeyHandlers, HasElementId, Focusable, FocusableComponentsContainer {

    interface WidgetUiBinder extends UiBinder<Widget, AbstractValidatedWidgetWithLabel<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {

        // TODO: remove these when all usages are PatternFly-based
        String label_legacy();

        String wrapper_legacy();

        String contentWidgetContainer_legacy();

        String contentWidget_legacy();
    }

    private final W contentWidget;

    @UiField
    HTMLPanel wrapperPanel;

    @UiField
    FormLabel label;

    @UiField
    SimplePanel contentWidgetContainer;

    @UiField
    WidgetTooltip labelTooltip;

    @UiField
    WidgetTooltip contentWidgetContainerTooltip;

    @UiField
    Style style;

    protected String labelConfiguredTooltip = null;

    protected String contentWidgetContainerConfiguredTooltip = null;

    // width in PX -- only used in legacy mode
    public static final int CONTENT_WIDTH_LEGACY = 230;

    private VisibilityRenderer renderer;

    public AbstractValidatedWidgetWithLabel(W contentWidget, VisibilityRenderer renderer) {
        this.contentWidget = contentWidget;
        this.renderer = renderer;

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        setUsePatternFly(false);
    }

    public AbstractValidatedWidgetWithLabel(W contentWidget) {
        this(contentWidget, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);
        contentWidgetContainer.setWidget(contentWidget);

        label.addStyleName(OvirtCss.LABEL_ENABLED);

        // Assign ID to content widget element if it's missing or empty
        Element contentWidgetElement = getContentWidgetElement();
        if (contentWidgetElement.getId() == null || contentWidgetElement.getId().isEmpty()) {
            setElementId(DOM.createUniqueId());
        }
    }

    protected FormLabel getFormLabel() {
        return label;
    }

    /**
     * set for="" for better accessibility
     */
    protected void updateLabelElementId(String elementId) {
        label.setFor(elementId);
    }

    public void setUsePatternFly(final boolean usePatternFly) {

        // toggle styles -- remove both PatternFly and non-PatternFly styles
        removeLabelStyleName(style.label_legacy());
        removeContentWidgetStyleName(style.contentWidget_legacy());
        removeContentWidgetStyleName(Styles.FORM_CONTROL);
        removeContentWidgetContainerStyleName(style.contentWidgetContainer_legacy());
        removeContentWidgetContainerStyleName("avw_contentWidgetContainer_pfly_fix"); //$NON-NLS-1$
        removeWrapperStyleName(Styles.FORM_GROUP);
        removeWrapperStyleName(style.wrapper_legacy());
        removeWrapperStyleName("avw_wrapper_pfly_fix"); //$NON-NLS-1$

        // add the proper styles
        if (usePatternFly) {
            addContentWidgetStyleName(Styles.FORM_CONTROL);
            addWrapperStyleName(Styles.FORM_GROUP);
        }
        else {
            addLabelStyleName(style.label_legacy());
            addContentWidgetStyleName(style.contentWidget_legacy());
            addContentWidgetContainerStyleName(style.contentWidgetContainer_legacy());
            addContentWidgetContainerStyleName("avw_contentWidgetContainer_pfly_fix"); //$NON-NLS-1$
            addWrapperStyleName(style.wrapper_legacy());
            addWrapperStyleName("avw_wrapper_pfly_fix"); //$NON-NLS-1$
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

    public SimplePanel getContentWidgetContainer() {
        return contentWidgetContainer;
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
        return label.getElement().getInnerText();
    }

    @Override
    public void setLabel(String labelText) {
        label.getElement().setInnerText(labelText);
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
            getFormLabel().getElement().replaceClassName(OvirtCss.LABEL_DISABLED, OvirtCss.LABEL_ENABLED);
        } else {
            getFormLabel().getElement().replaceClassName(OvirtCss.LABEL_ENABLED, OvirtCss.LABEL_DISABLED);
        }
    }

    @Override
    public void disable(String disabilityHint) {
        setEnabled(false);
        setWidgetTooltip(disabilityHint);
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        labelTooltip.setText(labelConfiguredTooltip);
        labelTooltip.reconfigure();
        contentWidgetContainerTooltip.setText(contentWidgetContainerConfiguredTooltip);
        contentWidgetContainerTooltip.reconfigure();
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        String tooltipText = getValidationTooltipText(validationHints);
        labelTooltip.setText(tooltipText);
        labelTooltip.reconfigure();
        contentWidgetContainerTooltip.setText(tooltipText);
        contentWidgetContainerTooltip.reconfigure();
    }

    public void setWidgetTooltip(String text) {
        setContentWidgetContainerTooltip(text);
        setLabelTooltip(text);
    }

    public void setContentWidgetContainerTooltip(String tooltipText) {
        contentWidgetContainerConfiguredTooltip = tooltipText;
        contentWidgetContainerTooltip.setText(tooltipText);
        contentWidgetContainerTooltip.reconfigure();
    }

    public void setLabelTooltip(String tooltipText) {
        labelConfiguredTooltip = tooltipText;
        labelTooltip.setText(tooltipText);
        labelTooltip.reconfigure();
    }

    // set styleNames on my components

    public void addContentWidgetStyleName(String styleName) {
        getContentWidget().asWidget().addStyleName(styleName);
    }

    public void setContentWidgetStyleName(String styleName) {
        getContentWidget().asWidget().setStyleName(styleName);
    }

    public void removeContentWidgetStyleName(String styleName) {
        getContentWidget().asWidget().removeStyleName(styleName);
    }

    public void addContentWidgetContainerStyleName(String styleName) {
        contentWidgetContainer.addStyleName(styleName);
    }

    public void setContentWidgetContainerStyleName(String styleName) {
        contentWidgetContainer.setStyleName(styleName);
    }

    public void removeContentWidgetContainerStyleName(String styleName) {
        contentWidgetContainer.removeStyleName(styleName);
    }

    public void addLabelStyleName(String styleName) {
        getFormLabel().addStyleName(styleName);
    }

    public void removeLabelStyleName(String styleName) {
        getFormLabel().removeStyleName(styleName);
    }

    public void setLabelStyleName(String styleName) {
        getFormLabel().setStyleName(styleName);
    }

    /**
     * @param styleNames space or comma-delimited list of style names
     */
    public void addLabelStyleNames(String styleNames) {
        for (String name : styleNames.split("[,\\s]+")) { //$NON-NLS-1$
            addLabelStyleName(name);
        }
    }

    // UIBinder-capable alias for addLabelStyleNames
    public void setAddLabelStyleNames(String styleNames) {
        addLabelStyleNames(styleNames);
    }

    public void addWrapperStyleName(String styleName) {
        wrapperPanel.addStyleName(styleName);
    }

    public void setWrapperStyleName(String styleName) {
        wrapperPanel.setStyleName(styleName);
    }

    public void removeWrapperStyleName(String styleName) {
        wrapperPanel.removeStyleName(styleName);
    }

    // end set styleNames on my components


    public void hideLabel() {
        getFormLabel().setVisible(false);
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
