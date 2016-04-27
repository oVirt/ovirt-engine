package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.AbstractValueBoxWithIconEditor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class EntityModelPasswordBoxIconEditor<T> extends AbstractValueBoxWithIconEditor<T, EntityModelPasswordBox<T>> {

    public EntityModelPasswordBoxIconEditor(Renderer<T> renderer, Parser<T> parser, String iconName) {
        super(new EntityModelPasswordBox<T>(renderer, parser), iconName);
    }
}
