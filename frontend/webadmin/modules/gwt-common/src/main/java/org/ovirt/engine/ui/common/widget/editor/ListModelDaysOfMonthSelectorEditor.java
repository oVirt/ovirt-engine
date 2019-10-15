package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.IsEditor;

/**
 * ListModel bound DaysOfMonthSelector editor with Label that uses {@link ListModelDaysOfMonthSelector}.
 */

public class ListModelDaysOfMonthSelectorEditor extends AbstractValidatedWidgetWithLabel<String, ListModelDaysOfMonthSelector> implements IsEditor<WidgetWithLabelEditor<String, ListModelDaysOfMonthSelectorEditor>> {

    private final WidgetWithLabelEditor<String, ListModelDaysOfMonthSelectorEditor> editor;

    /**
     * ListModel bound DaysOfMonthSelector editor with Label Constructor
     */
    public ListModelDaysOfMonthSelectorEditor() {
        super(new ListModelDaysOfMonthSelector(), new VisibilityRenderer.SimpleVisibilityRenderer());
        editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    /**
     * Api the provides handle to DaysOfMonthSelector and hence its public methods.
     * @return DaysOfMonthSelector
     */
    public DaysOfMonthSelector asDaysOfMonthSelector() {
        return getContentWidget();
    }

    @Override
    public WidgetWithLabelEditor<String, ListModelDaysOfMonthSelectorEditor> asEditor() {
        return editor;
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.NONE);
        getValidatedWidgetStyle().setDisplay(Display.TABLE);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        getValidatedWidgetStyle().setWidth(75, Unit.PCT);
    }

}
