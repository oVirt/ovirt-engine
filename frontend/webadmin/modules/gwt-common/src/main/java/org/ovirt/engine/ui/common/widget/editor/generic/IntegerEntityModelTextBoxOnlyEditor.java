package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToIntEntityModelParser;

public class IntegerEntityModelTextBoxOnlyEditor extends EntityModelTextBoxOnlyEditor<Integer> {

    public IntegerEntityModelTextBoxOnlyEditor(VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox<Integer>(
                new ToStringEntityModelRenderer<Integer>(),
                new ToIntEntityModelParser()),
             visibilityRenderer);
    }

    public IntegerEntityModelTextBoxOnlyEditor() {
        super(new EntityModelTextBox<Integer>(
                new ToStringEntityModelRenderer<Integer>(),
                new ToIntEntityModelParser()),
              new VisibilityRenderer.SimpleVisibilityRenderer());
    }
}
