package org.ovirt.engine.ui.common.widget.editor.generic;

import static java.util.Arrays.asList;

import java.util.List;

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
    protected List<String> getValidationHints() {
        return asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainNumberInvalidReason());
    }
}
