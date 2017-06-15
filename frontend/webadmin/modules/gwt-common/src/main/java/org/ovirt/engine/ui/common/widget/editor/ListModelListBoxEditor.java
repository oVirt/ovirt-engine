package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Renderer;

/**
 * Composite Editor that uses {@link ListModelListBox}.
 *
 * @param <T>
 *            List box item type.
 */
public class ListModelListBoxEditor<T> extends AbstractValidatedWidgetWithLabel<T, ListModelListBox<T>>
        implements IsEditor<WidgetWithLabelEditor<T, ListModelListBoxEditor<T>>> {

    private final WidgetWithLabelEditor<T, ListModelListBoxEditor<T>> editor;

    public ListModelListBoxEditor() {
        this(new StringRenderer<T>());
    }

    public ListModelListBoxEditor(VisibilityRenderer visibilityRenderer) {
        this(new StringRenderer<T>(), visibilityRenderer);
    }

    public ListModelListBoxEditor(Renderer<T> renderer, VisibilityRenderer visibilityRenderer) {
        super(new ListModelListBox<>(renderer), visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    public ListModelListBoxEditor(Renderer<T> renderer) {
        this(renderer, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public ListModelListBox<T> asListBox() {
        return getContentWidget();
    }

    @Override
    public WidgetWithLabelEditor<T, ListModelListBoxEditor<T>> asEditor() {
        return editor;
    }

    @Override
    public String getLabel() {
        return editor.getText();
    }
}
