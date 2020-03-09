package org.ovirt.engine.ui.common.widget.editor.generic;

import static java.util.Arrays.asList;

import java.util.List;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToIntEntityModelParser;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Composite Editor that uses {@link org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBox}.
 */
public class IntegerEntityModelTextBoxEditor extends NumberEntityModelTextBoxEditor<Integer> {

    public IntegerEntityModelTextBoxEditor(VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox<>(new ToStringEntityModelRenderer<Integer>(), ToIntEntityModelParser.newTrimmingParser()), visibilityRenderer);
    }

    public IntegerEntityModelTextBoxEditor() {
        super(new ToStringEntityModelRenderer<Integer>(), ToIntEntityModelParser.newTrimmingParser());
    }

    @Override
    protected List<String> getValidationHints() {
        return asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainIntegerNumberInvalidReason());
    }
}
