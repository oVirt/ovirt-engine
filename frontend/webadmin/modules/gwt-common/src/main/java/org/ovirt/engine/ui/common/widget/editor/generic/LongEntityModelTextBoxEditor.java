package org.ovirt.engine.ui.common.widget.editor.generic;

import static java.util.Arrays.asList;

import java.util.List;

import org.ovirt.engine.ui.common.widget.parser.generic.ToLongEntityParser;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class LongEntityModelTextBoxEditor extends NumberEntityModelTextBoxEditor<Long> {

    public LongEntityModelTextBoxEditor() {
        super(new ToStringEntityModelRenderer<Long>(), ToLongEntityParser.newTrimmingParser());
    }

    @Override
    protected List<String> getValidationHints() {
        //Even though this is a long, the validator will return the integer message, so that is the one we are using here.
        return asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainIntegerNumberInvalidReason());
    }
}
