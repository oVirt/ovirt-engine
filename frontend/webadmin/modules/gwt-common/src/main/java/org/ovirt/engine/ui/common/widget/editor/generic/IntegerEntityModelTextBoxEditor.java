package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToIntEntityModelParser;

/**
 * Composite Editor that uses {@link org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBox}.
 */
public class IntegerEntityModelTextBoxEditor extends EntityModelTextBoxEditor<Integer> {

    public IntegerEntityModelTextBoxEditor(VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox<Integer>(
                new ToStringEntityModelRenderer<Integer>(),
                new ToIntEntityModelParser()),
              visibilityRenderer);
    }

    public IntegerEntityModelTextBoxEditor() {
        super(new ToStringEntityModelRenderer<Integer>(),
              new ToIntEntityModelParser()
        );
    }

}
