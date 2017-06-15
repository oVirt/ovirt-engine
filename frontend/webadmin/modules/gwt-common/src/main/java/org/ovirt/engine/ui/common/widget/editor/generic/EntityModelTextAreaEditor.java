package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.editor.AbstractValueBoxWithLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EnterIgnoringFocusHandler;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class EntityModelTextAreaEditor<T> extends AbstractValueBoxWithLabelEditor<T, EntityModelTextArea<T>> {

    public EntityModelTextAreaEditor(Renderer<T> renderer, Parser<T> parser) {
        this(new EntityModelTextArea<>(renderer, parser));

        registerEnterIgnoringHandlers();
    }

    public EntityModelTextAreaEditor(EntityModelTextArea<T> widget) {
        super(widget);

        registerEnterIgnoringHandlers();
    }

    private void registerEnterIgnoringHandlers() {
        EnterIgnoringFocusHandler enterIgnoringFocusHandler = new EnterIgnoringFocusHandler() {
            @Override
            protected void enterPressed() {
                super.enterPressed();
                int pos = getContentWidget().getCursorPos();
                getContentWidget().setText(getContentWidget().getText().substring(0, pos) + '\n'
                        + getContentWidget().getText().substring(pos + getContentWidget().getSelectionLength())); //$NON-NLS-1$
                getContentWidget().setCursorPos(pos + 1);
            }
        };

        getContentWidget().addFocusHandler(enterIgnoringFocusHandler);
        getContentWidget().addBlurHandler(enterIgnoringFocusHandler);
    }
}
