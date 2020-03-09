package org.ovirt.engine.ui.common.widget.editor.generic;

import static java.util.Arrays.asList;

import java.util.List;

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
    protected List<String> getValidationHints() {
        return asList(ConstantsManager.getInstance().getConstants().thisFieldMustContainNumberInvalidReason());
    }
}
