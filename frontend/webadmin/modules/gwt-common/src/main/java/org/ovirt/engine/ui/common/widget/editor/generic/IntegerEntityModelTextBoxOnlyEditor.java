package org.ovirt.engine.ui.common.widget.editor.generic;

import static java.util.Arrays.asList;

import java.util.List;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToIntEntityModelParser;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class IntegerEntityModelTextBoxOnlyEditor extends NumberEntityModelTextBoxOnlyEditor<Integer> {

    public IntegerEntityModelTextBoxOnlyEditor(VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox<>(new ToStringEntityModelRenderer<Integer>(),
                ToIntEntityModelParser.newTrimmingParser()),
                visibilityRenderer);
    }

    public IntegerEntityModelTextBoxOnlyEditor() {
        super(new EntityModelTextBox<>(new ToStringEntityModelRenderer<Integer>(),
                ToIntEntityModelParser.newTrimmingParser()), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    @Override
    protected List<String> getValidationHints() {
        return asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainIntegerNumberInvalidReason());
    }
}
