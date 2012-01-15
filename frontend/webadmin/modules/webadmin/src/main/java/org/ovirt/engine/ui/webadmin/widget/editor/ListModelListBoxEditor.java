package org.ovirt.engine.ui.webadmin.widget.editor;

import org.ovirt.engine.ui.webadmin.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.webadmin.widget.renderer.StringRenderer;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Composite Editor that uses {@link ListModelListBox}.
 *
 * @param <T>
 *            List box item type.
 */
public class ListModelListBoxEditor<T> extends AbstractValidatedWidgetWithLabel<T, ListModelListBox<T>>
        implements IsEditor<WidgetWithLabelEditor<T, TakesConstrainedValueEditor<T>, ListModelListBoxEditor<T>>> {

    private final WidgetWithLabelEditor<T, TakesConstrainedValueEditor<T>, ListModelListBoxEditor<T>> editor;

    public ListModelListBoxEditor() {
        this(new StringRenderer<T>());
    }

    public ListModelListBoxEditor(Renderer<T> renderer) {
        super(new ListModelListBox<T>(renderer));
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    public ListBox asListBox() {
        return getContentWidget().asListBox();
    }

    @Override
    public WidgetWithLabelEditor<T, TakesConstrainedValueEditor<T>, ListModelListBoxEditor<T>> asEditor() {
        return editor;
    }

}
