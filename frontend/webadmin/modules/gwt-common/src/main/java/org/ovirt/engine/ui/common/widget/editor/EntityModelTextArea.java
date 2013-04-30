package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBoxBase;
import org.ovirt.engine.ui.common.widget.parser.EntityModelParser;

public class EntityModelTextArea extends ValueBoxBase<Object> implements EditorWidget<Object, ValueBoxEditor<Object>> {

    private ObservableValueBoxEditor editor;

    public EntityModelTextArea() {
        super(Document.get().createTextAreaElement(), new EntityModelRenderer(), new EntityModelParser());
    }

    public EntityModelTextArea(Renderer<Object> renderer, Parser<Object> parser) {
        super(Document.get().createTextAreaElement(), renderer, parser);
    }

    @Override
    public ValueBoxEditor<Object> asEditor() {
        if (editor == null) {
            editor = new ObservableValueBoxEditor(this);
        }
        return editor;
    }

}
