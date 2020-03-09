package org.ovirt.engine.ui.common.widget;

import static java.util.Collections.emptyList;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.editor.EditorStateUpdateEvent;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;
import org.ovirt.engine.ui.common.widget.label.HasWidgetLabels;
import org.ovirt.engine.ui.common.widget.label.LabelWithTooltip;
import org.ovirt.engine.ui.common.widget.label.WidgetLabel;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
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
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
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
public abstract class AbstractValidatedWidgetWithLabel<T, W extends EditorWidget<T, ?> & TakesValue<T> &
    HasValueChangeHandlers<T>> extends AbstractValidatedWidget
        implements HasLabel, HasEnabledWithHints, HasWidgetLabels, HasAccess, HasAllKeyHandlers, HasElementId, Focusable,
        FocusableComponentsContainer, HasCleanup {

    interface WidgetUiBinder extends UiBinder<Widget, AbstractValidatedWidgetWithLabel<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {

        // TODO: remove these when all usages are PatternFly-based
        String label_legacy();

        String wrapper_legacy();

        String contentWidgetContainer_legacy();

        String maxWidth();
    }

    //We need to store the valid state of the editor so that when the model validator
    //runs and the editor is not valid (due to a parsing error), the editor doesn't get
    //reset by the model.
    private boolean editorStateValid = true;

    private final W contentWidget;

    @UiField
    FlowPanel wrapperPanel;

    @UiField
    WidgetLabel label;

    @UiField
    FlowPanel contentWidgetContainer;

    SimplePanel sizeContainer;

    @UiField
    WidgetTooltip contentWidgetContainerTooltip;

    @UiField
    Style style;

    protected String contentWidgetContainerConfiguredTooltip = null;

    protected boolean removeFormGroup = false;

    // width in PX -- only used in legacy mode
    public static final int CONTENT_WIDTH_LEGACY = 230;

    private VisibilityRenderer renderer;

    public AbstractValidatedWidgetWithLabel(W contentWidget, VisibilityRenderer renderer) {
        this.contentWidget = contentWidget;
        this.renderer = renderer;

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        setUsePatternFly(false);
        addStateUpdateHandler();
    }

    public AbstractValidatedWidgetWithLabel(W contentWidget) {
        this(contentWidget, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);
        contentWidgetContainer.add(contentWidget);

        // Assign ID to content widget element if it's missing or empty
        Element contentWidgetElement = getContentWidgetElement();
        if (contentWidgetElement.getId() == null || contentWidgetElement.getId().isEmpty()) {
            setElementId(DOM.createUniqueId());
        }
    }

    protected LabelWithTooltip getFormLabel() {
        if (label instanceof LabelWithTooltip) {
            return (LabelWithTooltip)label;
        }
        throw new IllegalStateException("No label defined in widget that requires a label"); //$NON-NLS-1$
    }

    /**
     * set for="" for better accessibility
     */
    protected void updateLabelElementId(String elementId) {
        label.setFor(elementId);
    }

    public void setRemoveFormGroup(final boolean removeFormGroup) {
        this.removeFormGroup = removeFormGroup;
    }

    @Override
    public void setUsePatternFly(final boolean usePatternfly) {
        super.setUsePatternFly(usePatternfly);
        // toggle styles -- remove both PatternFly and non-PatternFly styles
        removeContentWidgetStyleName(style.maxWidth());
        removeContentWidgetStyleName(Styles.FORM_CONTROL);
        removeContentWidgetContainerStyleName(style.contentWidgetContainer_legacy());
        removeContentWidgetContainerStyleName("avw_contentWidgetContainer_pfly_fix"); //$NON-NLS-1$
        removeWrapperStyleName(Styles.FORM_GROUP);
        removeWrapperStyleName(style.wrapper_legacy());
        removeWrapperStyleName("avw_wrapper_pfly_fix"); //$NON-NLS-1$

        // add the proper styles
        if (usePatternfly) {
            addContentWidgetStyleName(Styles.FORM_CONTROL);
            addContentWidgetContainerStyleName(style.maxWidth());
            if (!removeFormGroup) {
                addWrapperStyleName(Styles.FORM_GROUP);
            }
            wrapperPanel.remove(contentWidgetContainer);
            if (sizeContainer == null) {
                sizeContainer = new SimplePanel();
                sizeContainer.setWidget(contentWidgetContainer);
                wrapperPanel.insert(sizeContainer, 1);
            }
        } else {
            addContentWidgetStyleName(style.maxWidth());
            addContentWidgetContainerStyleName(style.contentWidgetContainer_legacy());
            addContentWidgetContainerStyleName("avw_contentWidgetContainer_pfly_fix"); //$NON-NLS-1$
            addWrapperStyleName(style.wrapper_legacy());
            addWrapperStyleName("avw_wrapper_pfly_fix"); //$NON-NLS-1$
        }
    }

    public void setUnitString(String unitString) {
        SimplePanel unitAddOn = new SimplePanel();
        unitAddOn.getElement().setInnerHTML(unitString);
        unitAddOn.addStyleName(Styles.INPUT_GROUP_ADDON);
        contentWidgetContainer.addStyleName(Styles.INPUT_GROUP);
        contentWidgetContainer.add(unitAddOn);
    }

    public void addLabelColSize(ColumnSize size) {
        getFormLabel().addStyleName(size.getCssName());
    }

    public void removeLabelColSize(ColumnSize size) {
        getFormLabel().removeStyleName(size.getCssName());
    }

    public void setLabelColSize(ColumnSize size) {
        getFormLabel().addStyleName(size.getCssName());
    }

    public void addWidgetColSize(ColumnSize size) {
        if (sizeContainer != null) {
            sizeContainer.addStyleName(size.getCssName());
        }
    }

    public void removeWidgetColSize(ColumnSize size) {
        if (sizeContainer != null) {
            sizeContainer.removeStyleName(size.getCssName());
        }
    }

    public void setWidgetColSize(ColumnSize size) {
        if (sizeContainer != null) {
            sizeContainer.addStyleName(size.getCssName());
        }
    }

    /**
     * Insert a widget that will be place directly after the {@link #getContentWidgetContainer()}.
     */
    @UiChild(tagname="WidgetDecorator", limit=1)
    public void addWidgetDecorator(Widget widget) {
        wrapperPanel.insert(widget, wrapperPanel.getWidgetIndex(contentWidgetContainerTooltip) + 1);
    }

    /**
     * Render widget more responsive, by firing {@link ValueChangeEvent} on each {@link KeyDownEvent}.
     */
    public void fireValueChangeOnKeyDown() {
        getContentWidget().addKeyDownHandler(event -> {
            // deferring is required to allow the widget's internal value to update according to key press
            Scheduler.get().scheduleDeferred(() -> ValueChangeEvent.fire(getContentWidget(), getContentWidget().getValue()));
        });
    }

    protected W getContentWidget() {
        return contentWidget;
    }

    protected Element getContentWidgetElement() {
        return contentWidget.asWidget().getElement();
    }

    public FlowPanel getContentWidgetContainer() {
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
    public void setLabel(String labelText) {
        getFormLabel().setText(labelText);
    }

    public void setLabelTooltip(String tooltip) {
        getFormLabel().setTooltip(tooltip);
    }

    @Override
    public String getLabel() {
        return getFormLabel().getText();
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

        getFormLabel().setEnabled(enabled);
        if (enabled) {
            setWidgetTooltip("");
            label.setEnabled(true);
        }
    }

    @Override
    public void disable(String disabilityHint) {
        setEnabled(false);
        setWidgetTooltip(disabilityHint);
        label.disable(disabilityHint);
    }

    @Override
    public void markAsValid() {
        if (!editorStateValid) {
            // prevent overriding editor-level validation
            return;
        }
        super.markAsValid();
        label.setEnabled(true);
        contentWidgetContainerTooltip.setText(contentWidgetContainerConfiguredTooltip);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        if (!editorStateValid) {
            // if both editor-level and model-level validation failed then prefer editor-level
            return;
        }
        super.markAsInvalid(validationHints);
        String tooltipText = getValidationTooltipText(validationHints);
        label.disable(tooltipText);
        contentWidgetContainerTooltip.setText(tooltipText);
    }

    public void setWidgetTooltip(String text) {
        setContentWidgetContainerTooltip(text);
    }

    @Override
    public void removeLabel(WidgetLabel label) {
        label.setFor(null);
    }

    @Override
    public void addLabel(WidgetLabel label) {
        label.setFor(getContentWidgetElement().getId());
        this.label = label;
    }

    public void setContentWidgetContainerTooltip(String tooltipText) {
        contentWidgetContainerConfiguredTooltip = tooltipText;
        contentWidgetContainerTooltip.setText(tooltipText);
    }

    // start style name methods on my components
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

    /**
     * @param styleName space or comma-delimited list of style names
     */
    public void addLabelStyleName(String styleName) {
        for (String name : styleName.split("[,\\s]+")) { //$NON-NLS-1$
            getFormLabel().addStyleName(name);
        }
    }

    public void setAddLabelStyleName(String styleName) {
        getFormLabel().addStyleName(styleName);
    }

    public void setLabelStyleName(String styleName) {
        getFormLabel().setStyleName(styleName);
    }

    public void removeLabelStyleName(String styleName) {
        getFormLabel().removeStyleName(styleName);
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
    // end style name methods on my components

    public void hideLabel() {
        getFormLabel().asWidget().setVisible(false);
    }

    // sometimes hideLabel() results in broken layout, in that case setting Display.NONE could help
    public void setLabelDisplay(Display display) {
        getFormLabel().asWidget().getElement().getStyle().setDisplay(display);
    }

    public void clearLabelDisplay() {
        getFormLabel().asWidget().getElement().getStyle().clearDisplay();
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

    protected List<String> getValidationHints() {
        return emptyList();
    }

    private void addStateUpdateHandler() {
        this.getContentWidget().asWidget().addHandler(event -> {
            if (event.isValid()) {
                // Mark the editor as valid.
                editorStateValid = true;
                markAsValid();
            } else {
                // Mark the editor as invalid.
                markAsInvalid(getValidationHints());
                editorStateValid = false;
            }
        }, EditorStateUpdateEvent.getType());
    }

    public boolean isUsePatternfly() {
        return this.usePatternfly;
    }

    @Override
    public void cleanup() {
        W contentWidget = getContentWidget();
        if (contentWidget instanceof HasCleanup) {
            ((HasCleanup) contentWidget).cleanup();
        }

        contentWidgetContainerTooltip.cleanup();
    }

}
