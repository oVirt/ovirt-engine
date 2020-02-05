package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

/**
 * Composite Editor that uses {@link org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBox}.
 */
public class StringEntityModelTextBoxEditor extends EntityModelTextBoxEditor<String> {

    public StringEntityModelTextBoxEditor(VisibilityRenderer visibilityRenderer) {
        this(new StringEntityModelTextBox(), visibilityRenderer);
    }

    public StringEntityModelTextBoxEditor() {
        this(new StringEntityModelTextBox(), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    private StringEntityModelTextBoxEditor(EntityModelTextBox<String> contentWidget,
            VisibilityRenderer visibilityRenderer) {
        super(contentWidget, visibilityRenderer);
    }

    public static StringEntityModelTextBoxEditor newTrimmingEditor() {
        return new StringEntityModelTextBoxEditor(
                new StringEntityModelTextBox(ToStringEntityModelParser.newTrimmingParser()),
                new VisibilityRenderer.SimpleVisibilityRenderer());
    }
}
