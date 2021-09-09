package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.event.InputEvent;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class EntityModelTextArea<T> extends ValueBoxBase<T> implements EditorWidget<T, ValueBoxEditor<T>> {

    private ObservableValueBoxEditor<T> editor;
    private HandlerRegistration inputEventRegistration;

    public EntityModelTextArea(Renderer<T> renderer, Parser<T> parser) {
        super(Document.get().createTextAreaElement(), renderer, parser);
    }

    @Override
    public ValueBoxEditor<T> asEditor() {
        if (editor == null) {
            editor = new ObservableValueBoxEditor(this);
        }
        return editor;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        removeInputHandlerIfPresent();
        inputEventRegistration = addDomHandler(event -> {
            ValueChangeEvent.fire(EntityModelTextArea.this, getValue());
        }, InputEvent.getType());
    }

    private void removeInputHandlerIfPresent() {
        if (inputEventRegistration != null) {
            inputEventRegistration.removeHandler();
        }
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        removeInputHandlerIfPresent();
    }
}
