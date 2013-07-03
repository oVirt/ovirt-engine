package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class EntityModelTextAreaLabelEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelTextAreaLabel> {

    /**
     * A ValueBoxWithLabelEditor that has a Label as the widget
     */
    public EntityModelTextAreaLabelEditor() {
        super(new EntityModelTextAreaLabel());
    }

    public EntityModelTextAreaLabelEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelTextAreaLabel(renderer, parser));
    }

    public EntityModelTextAreaLabelEditor(final boolean showBorder, final boolean disableResizing) {
        super(new EntityModelTextAreaLabel() {
            @Override
            public void setText(String text) {
                super.setText(new EmptyValueRenderer<String>().render(text));

                if (showBorder) {
                    getElement().getStyle().setBorderWidth(1, Unit.PX);
                }
                if (disableResizing) {
                    getElement().getStyle().setProperty("resize", "none"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        });
    }
}
