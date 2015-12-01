package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Arrays;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToShortEntityModelParser;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Composite Editor that uses {@link EntityModelTextBox}.
 */
public class ShortEntityModelTextBoxEditor extends NumberEntityModelTextBoxEditor<Short> {

    public ShortEntityModelTextBoxEditor(VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox<>(new ToStringEntityModelRenderer<Short>(), new ToShortEntityModelParser()), visibilityRenderer);
    }

    public ShortEntityModelTextBoxEditor() {
        super(new ToStringEntityModelRenderer<Short>(), new ToShortEntityModelParser());
    }

    @Override
    protected void handleInvalidState() {
        //Be sure to call super.handleInvalidstate to make sure the editor valid state is properly updated.
        super.handleInvalidState();
        markAsInvalid(Arrays.asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainIntegerNumberInvalidReason()));
    }
}
