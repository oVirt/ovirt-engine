package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Renderer;

/**
 * ListModel bound CheckBoxGroup Editor with Label that uses {@link ListModelCheckBoxGroup}.
 */

public class ListModelCheckBoxGroupEditor<T> extends AbstractValidatedWidgetWithLabel<List<T>, ListModelCheckBoxGroup<T>> implements IsEditor<WidgetWithLabelEditor<List<T>, ListModelCheckBoxGroupEditor<T>>>{

    private final WidgetWithLabelEditor<List<T>, ListModelCheckBoxGroupEditor<T>> editor;

    /**
     * Constructor of ListModel bound CheckBoxGroup Editor with Label that uses {@link ListModelCheckBoxGroup} It uses
     * the default String renderer to render the values as CheckBox labels in the CheckBoxGroup.
     */
    public ListModelCheckBoxGroupEditor() {
        this(new StringRenderer<T>());
    }

    /**
     * Constructor of ListModel bound CheckBoxGroup Editor with Label that uses {@link ListModelCheckBoxGroup}
     * @param renderer
     *            to render the values passed to ListModel's setItems and hence setAcceptableValues
     */
    public ListModelCheckBoxGroupEditor(Renderer<T> renderer) {
        super(new ListModelCheckBoxGroup<>(renderer), new VisibilityRenderer.SimpleVisibilityRenderer());
        editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.NONE);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        getValidatedWidgetStyle().setDisplay(Display.INLINE_BLOCK);
    }

    @Override
    public WidgetWithLabelEditor<List<T>, ListModelCheckBoxGroupEditor<T>> asEditor() {
        return editor;
    }

    /**
     * Api the provides handle to CheckBoxGroup and hence its public methods.
     * @return CheckBoxGroup from the ListModelCheckBoxGroupEditor
     */
    public CheckBoxGroup<T> asCheckBoxGroup() {
        return getContentWidget();
    }
}
