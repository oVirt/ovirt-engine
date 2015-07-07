package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Arrays;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class NumberEntityModelTextBoxEditor<T extends Number> extends EntityModelTextBoxEditor<T> {
    public NumberEntityModelTextBoxEditor(EntityModelTextBox<T> contentWidget, VisibilityRenderer visibilityRenderer) {
        super(contentWidget, visibilityRenderer);
    }

    public NumberEntityModelTextBoxEditor(Renderer<T> renderer, Parser<T> parser) {
        super(renderer, parser);
    }

    public NumberEntityModelTextBoxEditor(Renderer<T> renderer, Parser<T> parser, VisibilityRenderer visibilityRenderer) {
        super(renderer, parser, visibilityRenderer);
    }

    @Override
    protected void handleInvalidState() {
        //Be sure to call super.handleInvalidstate to make sure the editor valid state is properly updated.
        super.handleInvalidState();
        markAsInvalid(Arrays.asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainNumberInvalidReason()));
    }

}
