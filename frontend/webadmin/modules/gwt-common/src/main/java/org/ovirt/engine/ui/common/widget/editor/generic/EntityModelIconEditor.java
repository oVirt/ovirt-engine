package org.ovirt.engine.ui.common.widget.editor.generic;

import java.text.ParseException;

import org.ovirt.engine.ui.common.widget.AbstractValueBoxWithIconEditor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class EntityModelIconEditor<T> extends AbstractValueBoxWithIconEditor<T, EntityModelLabel<T>> {

    /**
     * A ValueBoxWithIconEditor that has a Icon as the widget
     */
    public EntityModelIconEditor(Renderer<T> renderer, Parser<T> parser, String iconName) {
        this(new EntityModelLabel<T>(renderer, parser), iconName);
    }

    /**
     * A ValueBoxWithIconEditor that should be readonly, so the parser is not needed.
     *
     * @param renderer
     *            The renderer.
     */
    public EntityModelIconEditor(Renderer<T> renderer, String iconName) {
        this(new EntityModelLabel<T>(renderer, new Parser<T>() {
            @Override
            public T parse(CharSequence text) throws ParseException {
                // Parser is not needed as its a read only field and value is not used.
                return null;
            }
        }), iconName);
    }

    public EntityModelIconEditor(EntityModelLabel<T> widget, String iconName) {
        super(widget, iconName);
    }
}
