package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.user.client.ui.ValueBox;

/**
 * An {@link EditorWidget} that only shows a Label (readonly)
 */
public class EntityModelLabel extends ValueBox<Object> implements EditorWidget<Object, ValueBoxEditor<Object>> {

    public EntityModelLabel() {
        super(Document.get().createTextInputElement(), new EntityModelRenderer(), new EntityModelParser());
    }

    @Override
    public void setText(String text) {
        super.setText(new EmptyValueRenderer<String>().render(text));
        setReadOnly(true);
        getElement().getStyle().setBorderWidth(0, Unit.PX);
    }

}
