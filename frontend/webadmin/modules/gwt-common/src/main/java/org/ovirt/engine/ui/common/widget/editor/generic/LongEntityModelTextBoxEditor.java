package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Arrays;

import org.ovirt.engine.ui.common.widget.parser.generic.ToLongEntityParser;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class LongEntityModelTextBoxEditor extends NumberEntityModelTextBoxEditor<Long> {

    public LongEntityModelTextBoxEditor() {
        super(new ToStringEntityModelRenderer<Long>(), new ToLongEntityParser());
    }

    @Override
    protected void handleInvalidState() {
        //Be sure to call super.handleInvalidstate to make sure the editor valid state is properly updated.
        super.handleInvalidState();
        //Even though this is a long, the validator will return the integer message, so that is the one we are using here.
        markAsInvalid(Arrays.asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainIntegerNumberInvalidReason()));
    }
}
