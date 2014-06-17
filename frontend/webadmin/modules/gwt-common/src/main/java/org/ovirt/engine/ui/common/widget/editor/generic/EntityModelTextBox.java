package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;

public class EntityModelTextBox<T> extends ValueBox<T> implements EditorWidget<T, ValueBoxEditor<T>> {

    private ObservableValueBoxEditor<T> editor;

    public EntityModelTextBox(Renderer<T> renderer, Parser<T> parser) {
        super(Document.get().createTextInputElement(), renderer, parser);
    }

    @Override
    public ValueBoxEditor<T> asEditor() {
        if (editor == null) {
            editor = new ObservableValueBoxEditor<T>(this);
        }
        return editor;
    }

}
