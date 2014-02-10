package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import org.ovirt.engine.ui.common.widget.editor.AbstractValueBoxWithLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EnterIgnoringFocusHandler;

public class EntityModelTextAreaEditor<T> extends AbstractValueBoxWithLabelEditor<T, EntityModelTextArea<T>> {

    public EntityModelTextAreaEditor(Renderer<T> renderer, Parser<T> parser) {
        this(new EntityModelTextArea<T>(renderer, parser));

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
                getContentWidget().setText(getContentWidget().getText() + '\n'); //$NON-NLS-1$
            }
        };

        getContentWidget().addFocusHandler(enterIgnoringFocusHandler);
        getContentWidget().addBlurHandler(enterIgnoringFocusHandler);
    }
}
