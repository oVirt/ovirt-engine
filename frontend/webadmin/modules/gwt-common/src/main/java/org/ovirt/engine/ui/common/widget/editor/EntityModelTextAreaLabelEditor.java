package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class EntityModelTextAreaLabelEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelTextAreaLabel> {

    /**
     * A ValueBoxWithLabelEditor that has a Label as the widget
     */
    public EntityModelTextAreaLabelEditor() {
        super(new EntityModelTextAreaLabel() {
            @Override
            public void setText(String text) {
                super.setText(new EmptyValueRenderer<String>().render(text));
            }
        });
    }

    public EntityModelTextAreaLabelEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelTextAreaLabel(renderer, parser));
    }

    public void setCustomStyle(final String customStyle) {
        if (customStyle != null) {
            getElement().getElementsByTagName("textarea").getItem(0).addClassName(customStyle); //$NON-NLS-1$
        }
    }
}
