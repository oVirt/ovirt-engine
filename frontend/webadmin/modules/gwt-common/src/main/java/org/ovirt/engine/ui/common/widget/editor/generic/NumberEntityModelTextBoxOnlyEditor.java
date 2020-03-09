package org.ovirt.engine.ui.common.widget.editor.generic;

import static java.util.Arrays.asList;

import java.util.List;

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
    protected List<String> getValidationHints() {
        return asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainNumberInvalidReason());
    }
}
