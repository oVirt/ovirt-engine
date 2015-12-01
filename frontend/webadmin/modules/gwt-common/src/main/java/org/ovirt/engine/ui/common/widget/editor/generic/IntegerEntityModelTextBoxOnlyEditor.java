package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Arrays;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToIntEntityModelParser;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class IntegerEntityModelTextBoxOnlyEditor extends NumberEntityModelTextBoxOnlyEditor<Integer> {

    public IntegerEntityModelTextBoxOnlyEditor(VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox<>(new ToStringEntityModelRenderer<Integer>(), new ToIntEntityModelParser()),
             visibilityRenderer);
    }

    public IntegerEntityModelTextBoxOnlyEditor() {
        super(new EntityModelTextBox<>(new ToStringEntityModelRenderer<Integer>(),
                new ToIntEntityModelParser()), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    @Override
    protected void handleInvalidState() {
        //Be sure to call super.handleInvalidstate to make sure the editor valid state is properly updated.
        super.handleInvalidState();
        markAsInvalid(Arrays.asList(ConstantsManager.getInstance().getConstants()
                .thisFieldMustContainIntegerNumberInvalidReason()));
    }

}
