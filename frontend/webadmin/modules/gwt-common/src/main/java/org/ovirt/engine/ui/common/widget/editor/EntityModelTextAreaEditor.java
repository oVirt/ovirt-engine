package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

/**
 * @deprecated use the org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextAreaEditor instead
 */
@Deprecated
public class EntityModelTextAreaEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelTextArea> {

    public EntityModelTextAreaEditor() {
        super(new EntityModelTextArea());

        registerEnterIgnoringHandlers();
    }

    public EntityModelTextAreaEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelTextArea(renderer, parser));

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
