package org.ovirt.engine.ui.webadmin.widget.editor;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;

public class EntityModelTextBox extends ValueBox<Object> implements EditorWidget<Object, ValueBoxEditor<Object>> {

    private ObservableValueBoxEditor editor;

    public EntityModelTextBox() {
        super(Document.get().createTextInputElement(), new EntityModelRenderer(), new EntityModelParser());
    }

    public EntityModelTextBox(Renderer<Object> renderer, Parser<Object> parser) {
        super(Document.get().createTextInputElement(), renderer, parser);
    }

    @Override
    public ValueBoxEditor<Object> asEditor() {
        if (editor == null) {
            editor = ObservableValueBoxEditor.of(this);
        }
        return editor;
    }

}
