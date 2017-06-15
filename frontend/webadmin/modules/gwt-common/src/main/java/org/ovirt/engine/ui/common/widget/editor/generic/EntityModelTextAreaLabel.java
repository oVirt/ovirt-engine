package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.editor.EditorWidget;
import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * An {@link org.ovirt.engine.ui.common.widget.editor.EditorWidget} that only shows a Label (readonly)
 */
public class EntityModelTextAreaLabel<T> extends ValueBoxBase<T> implements EditorWidget<T, ValueBoxEditor<T>> {

    public EntityModelTextAreaLabel(Renderer<T> renderer, Parser<T> parser) {
        super(Document.get().createTextAreaElement(), renderer, parser);
    }

    @Override
    public void setText(String text) {
        super.setText(new EmptyValueRenderer<String>().render(text));
        setReadOnly(true);
        getElement().getStyle().setBorderWidth(0, Unit.PX);
    }

    public void addContentWidgetStyleName(String style) {
        if (style != null) {
            getElement().getElementsByTagName("textarea").getItem(0).addClassName(style); //$NON-NLS-1$
        }
    }
}
