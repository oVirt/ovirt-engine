package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Collection;

import org.ovirt.engine.ui.common.widget.editor.BootstrapListModelListBox;
import org.ovirt.engine.ui.common.widget.editor.TakesConstrainedValueEditor;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.HasConstrainedValue;

public class BootstrapListBoxListModelEditor<T> extends AbstractLabelableEntityModelEditor<T, BootstrapListModelListBox<T>>
        implements HasConstrainedValue<T> {

    /**
     * This delegate is used because of {@link org.ovirt.engine.ui.common.editor.UiCommonEditorVisitor}
     */
    private TakesConstrainedValueEditor<T> editor;

    public BootstrapListBoxListModelEditor(Renderer<T> renderer) {
        super(new BootstrapListModelListBox<>(renderer));
        editor = TakesConstrainedValueEditor.of(this, this, this);
    }

    @Override
    public T getValue() {
        return getEditorWidget().getValue();
    }

    @Override
    public void setValue(T value) {
        getEditorWidget().setValue(value);
    }

    @Override
    public void setAcceptableValues(Collection<T> values) {
        getEditorWidget().setAcceptableValues(values);
    }

    @Override
    public LeafValueEditor<T> getActualEditor() {
        return editor;
    }
}
