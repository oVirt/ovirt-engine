package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Arrays;

import org.ovirt.engine.ui.uicompat.ConstantsManager;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class NumberEntityModelLabelEditor<T extends Number> extends EntityModelLabelEditor<T> {

    public NumberEntityModelLabelEditor(Renderer<T> renderer) {
        super(renderer);
    }

    public NumberEntityModelLabelEditor(Renderer<T> renderer, Parser<T> parser) {
        super(renderer, parser);
    }

    public NumberEntityModelLabelEditor(EntityModelLabel<T> widget) {
        super(widget);
    }

    @Override
    protected void handleInvalidState() {
        //Be sure to call super.handleInvalidstate to make sure the editor valid state is properly updated.
        super.handleInvalidState();
        markAsInvalid(Arrays.asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainNumberInvalidReason()));
    }
}
