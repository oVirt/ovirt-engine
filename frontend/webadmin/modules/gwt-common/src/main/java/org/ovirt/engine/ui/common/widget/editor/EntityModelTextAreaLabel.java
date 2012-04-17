package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.parser.EntityModelParser;
import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * An {@link EditorWidget} that only shows a Label (readonly)
 */
public class EntityModelTextAreaLabel extends ValueBoxBase<Object> implements EditorWidget<Object, ValueBoxEditor<Object>> {

    public EntityModelTextAreaLabel() {
        super(Document.get().createTextAreaElement(), new EntityModelRenderer(), new EntityModelParser());
    }

    public EntityModelTextAreaLabel(Renderer<Object> renderer, Parser<Object> parser) {
        super(Document.get().createTextAreaElement(), renderer, parser);
    }

    @Override
    public void setText(String text) {
        super.setText(new EmptyValueRenderer<String>().render(text));
        setReadOnly(true);
        getElement().getStyle().setBorderWidth(0, Unit.PX);
    }

}
