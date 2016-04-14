package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.base.HasId;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditor;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidget;
import org.ovirt.engine.ui.common.widget.HasAccess;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.label.HasWidgetLabels;
import org.ovirt.engine.ui.common.widget.label.WidgetLabel;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract model editor that unlike {@link org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel} doesn't
 * contain attached label.
 *
 * <p>
 *     It implements {@link UiCommonEditor} only to be compatible with {@link org.ovirt.engine.ui.common.editor.UiCommonEditorVisitor}
 * </p>
 *
 * @param <E> type of edited value
 * @param <W> type of editing widget
 */
public abstract class AbstractLabelableEntityModelEditor<E, W extends Widget & Focusable & HasVisibility & HasId & HasEnabled & HasValueChangeHandlers<E> & HasKeyDownHandlers & HasKeyUpHandlers & HasKeyPressHandlers>
            implements HasElementId, HasAccess, Focusable, HasEnabledWithHints, HasValue<E>, HasWidgetLabels,
                       LeafValueEditor<E>, UiCommonEditor<E>, HasValidation, IsWidget {

    private final W editorWidget;

    private final WidgetTooltip tooltip;

    private final AbstractValidatedWidget validationDelegate;

    final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private Set<WidgetLabel> labels = new HashSet<>();

    public AbstractLabelableEntityModelEditor(final W editorWidget) {
        this.editorWidget = editorWidget;
        tooltip = new WidgetTooltip(editorWidget);
        validationDelegate = new AbstractValidatedWidget() {
            @Override
            protected Widget getValidatedWidget() {
                return AbstractLabelableEntityModelEditor.this.getValidatedWidget();
            }
        };
    }

    protected W getEditorWidget() {
        return editorWidget;
    }

    protected Widget getValidatedWidget() {
        return editorWidget;
    }

    @Override
    public int getTabIndex() {
        return editorWidget.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        editorWidget.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        editorWidget.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        editorWidget.setTabIndex(index);
    }

    @Override
    public boolean isAccessible() {
        return editorWidget.isVisible();
    }

    @Override
    public void setAccessible(boolean accessible) {
        editorWidget.setVisible(accessible);
    }

    @Override
    public void setElementId(String elementId) {
        editorWidget.setId(elementId);

        for(WidgetLabel label : labels) {
            label.setFor(elementId);
        }
    }

    @Override
    public void disable(String disabilityHint) {
        setEnabled(false);
        setTooltipText(disabilityHint);

        for (WidgetLabel label : labels) {
            label.disable(disabilityHint);
        }
    }

    @Override
    public boolean isEnabled() {
        return editorWidget.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        editorWidget.setEnabled(enabled);
        if (enabled) {
            clearTooltip();
        }

        for (WidgetLabel label : labels) {
            label.setEnabled(enabled);
        }
    }

    @Override
    public void setValue(E value, boolean fireEvents) {
        final E oldValue = getValue();
        setValue(value);
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<E> handler) {
        return editorWidget.addValueChangeHandler(handler);
    }

    @Override
    public void addLabel(WidgetLabel label) {
        label.setFor(editorWidget.getId());
        labels.add(label);
    }

    @Override
    public void removeLabel(WidgetLabel label) {
        labels.remove(label);
    }

    @Override
    public LeafValueEditor<E> getActualEditor() {
        return this;
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return editorWidget.addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return editorWidget.addKeyPressHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return editorWidget.addKeyUpHandler(handler);
    }

    @Override
    public void markAsValid() {
        validationDelegate.markAsValid();
        clearTooltip();
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        validationDelegate.markAsInvalid(validationHints);
        SafeHtml htmlHints = hintsToSafeHtml(validationHints);
        setTooltipHtml(htmlHints);
    }

    @Override
    public boolean isValid() {
        return validationDelegate.isValid();
    }

    @Override
    public Widget asWidget() {
        return tooltip.getWidget();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        asWidget().fireEvent(event
        );
    }

    private SafeHtml hintsToSafeHtml(List<String> validationHints) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        for (String hint : validationHints) {
            final SafeHtml htmlHint = templates.paragraph(hint);
            builder.append(htmlHint);
        }
        return builder.toSafeHtml();
    }

    void setTooltipText(String tooltipText) {
        tooltip.setText(tooltipText);
        tooltip.reconfigure();
    }

    void setTooltipHtml(SafeHtml tooltipHtml) {
        tooltip.setHtml(tooltipHtml);
        tooltip.reconfigure();
    }

    void clearTooltip() {
        tooltip.setText("");
        tooltip.reconfigure();
    }

    /**
     * Method name is creepy to allow standard usage in UiBinder .ui.xml files
     * <pre>
     *     &lt;AbstractLabelableEntityModelEditor addStyleName="..." />
     * </pre>
     */
    public void setAddStyleNames(String style) {
        getEditorWidget().addStyleName(style);
    }
}
