package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Arrays;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class NumberEntityModelTextBoxOnlyEditor<T extends Number> extends EntityModelTextBoxOnlyEditor<T> {

    public NumberEntityModelTextBoxOnlyEditor(EntityModelTextBox<T> textBox, VisibilityRenderer visibilityRenderer) {
        super(textBox, visibilityRenderer);
    }

    public NumberEntityModelTextBoxOnlyEditor(Renderer<T> renderer, Parser<T> parser) {
        super(renderer, parser);
    }

    @Override
    protected void handleInvalidState() {
        //Be sure to call super.handleInvalidstate to make sure the editor valid state is properly updated.
        super.handleInvalidState();
        markAsInvalid(Arrays.asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainNumberInvalidReason()));
    }

}
