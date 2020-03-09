package org.ovirt.engine.ui.common.widget.editor.generic;

import java.text.ParseException;

import org.ovirt.engine.ui.common.widget.editor.EditorStateUpdateEvent;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;
import org.ovirt.engine.ui.common.widget.editor.HasEditorValidityState;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;

public class EntityModelTextBox<T> extends ValueBox<T> implements EditorWidget<T, ValueBoxEditor<T>>,
    HasEditorValidityState {

    private ObservableValueBoxEditor<T> editor;
    private boolean isValid = true;

    public EntityModelTextBox(Renderer<T> renderer, Parser<T> parser) {
        super(Document.get().createTextInputElement(), renderer, parser);
    }

    @Override
    public ValueBoxEditor<T> asEditor() {
        if (editor == null) {
            editor = new ObservableValueBoxEditor<>(this);
        }
        return editor;
    }

    /**
     * Return the parsed value, or null if the field is empty or parsing fails. If the validity of the box changes
     * fire an {@code EditorStateUpdateEvent}, so interested parties can handle it.
     */
    @Override
    public T getValue() {
        T value = null;
        boolean originalValidState = isValid;
        try {
            value = getValueOrThrow();
            isValid = true;
        } catch (ParseException e) {
            isValid = false;
        }
        if (originalValidState != isValid) {
            fireEvent(new EditorStateUpdateEvent(isValid));
        }
        return value;
    }

    @Override
    public boolean isStateValid() {
        return isValid;
    }

    @Override
    public void setValue(T value) {
        if (!isStateValid() && value == null) {
            // invalid value is reported to model as null
            // prevent replacing invalid user input with
            // that null value coming back from the model
            return;
        }
        super.setValue(value);
    }
}
