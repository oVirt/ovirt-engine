package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;
import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;

/**
 * An {@link org.ovirt.engine.ui.common.widget.editor.EditorWidget} that only shows a Label (readonly)
 */
public class EntityModelLabel<T> extends ValueBox<T> implements EditorWidget<T, ValueBoxEditor<T>> {

    private ObservableValueBoxEditor<T> editor;

    public EntityModelLabel(Renderer<T> renderer, Parser<T> parser) {
        super(Document.get().createTextInputElement(), renderer, parser);
    }

    // @Override
    // public void setText(String text) {
    // super.setText(new EmptyValueRenderer<String>().render(text));
    // setReadOnly(true);
    // getElement().getStyle().setBorderWidth(0, Unit.PX);
    // }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            getElement().replaceClassName(OvirtCss.LABEL_DISABLED, OvirtCss.LABEL_ENABLED);
        } else {
            getElement().replaceClassName(OvirtCss.LABEL_ENABLED, OvirtCss.LABEL_DISABLED);
        }
    }

    @Override
    public ValueBoxEditor<T> asEditor() {
        if (editor == null) {
            editor = new ObservableValueBoxEditor(this);
        }
        return editor;
    }
}
