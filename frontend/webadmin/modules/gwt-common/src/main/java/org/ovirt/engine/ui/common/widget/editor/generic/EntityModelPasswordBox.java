package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.editor.EditorWidget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class EntityModelPasswordBox<T> extends ValueBoxBase<T> implements EditorWidget<T, ValueBoxEditor<T>> {

    private ObservableValueBoxEditor<T> editor;


    public EntityModelPasswordBox(Renderer<T> renderer, Parser<T> parser) {
        super(Document.get().createPasswordInputElement(), renderer, parser);
    }

    @Override
    public ValueBoxEditor<T> asEditor() {
        if (editor == null) {
            editor = new ObservableValueBoxEditor(this);
        }
        return editor;
    }

}
