package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.parser.EntityModelParser;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class EntityModelPasswordBox extends ValueBoxBase<Object> implements EditorWidget<Object, ValueBoxEditor<Object>> {

    private ObservableValueBoxEditor editor;

    public EntityModelPasswordBox() {
        super(Document.get().createPasswordInputElement(), new EntityModelRenderer(), new EntityModelParser());
    }

    @Override
    public ValueBoxEditor<Object> asEditor() {
        if (editor == null) {
            editor = new ObservableValueBoxEditor(this);
        }
        return editor;
    }

}
