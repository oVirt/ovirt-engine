package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.parser.generic.ToShortEntityModelParser;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ValueEventArgs;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;

/**
 * Composite Editor that uses {@link EntityModelTextBox}.
 */
public class ShortEntityModelTextBoxEditor extends NumberEntityModelTextBoxEditor<Short> {

    private Event<ValueEventArgs<Boolean>> validityChangedEvent = new Event<>("ValidityChanged", ShortEntityModelTextBoxEditor.class); //$NON-NLS-1$

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

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        if (validityChangedEvent != null) {
            validityChangedEvent.raise(this, new ValueEventArgs<>(false));
        }
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        if (validityChangedEvent != null) {
            validityChangedEvent.raise(this, new ValueEventArgs<>(true));
        }
    }

    public Event getValidityChangedEvent() {
        return validityChangedEvent;
    }
}
