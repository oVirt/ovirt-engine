package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Renderer;

/**
 * ListModel bound CheckBoxGroup Editor that uses {@link ListModelCheckBoxGroup}.
 */

public class ListModelCheckBoxGroupEditor<T> extends AbstractValidatedWidgetWithLabel<List<T>, ListModelCheckBoxGroup<T>> implements IsEditor<WidgetWithLabelEditor<List<T>, ListModelCheckBoxGroupEditor<T>>>{

    private final WidgetWithLabelEditor<List<T>, ListModelCheckBoxGroupEditor<T>> editor;

    public ListModelCheckBoxGroupEditor() {
        this(new StringRenderer<T>());
    }

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
    }

    @Override
    public WidgetWithLabelEditor<List<T>, ListModelCheckBoxGroupEditor<T>> asEditor() {
        return editor;
    }

    public CheckBoxGroup<T> asCheckBoxGroup() {
        return getContentWidget();
    }
}
