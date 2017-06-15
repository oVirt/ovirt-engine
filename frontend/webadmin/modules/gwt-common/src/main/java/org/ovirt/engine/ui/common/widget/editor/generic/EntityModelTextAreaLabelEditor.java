package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.editor.AbstractValueBoxWithLabelEditor;
import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;


public class EntityModelTextAreaLabelEditor<T> extends AbstractValueBoxWithLabelEditor<T, EntityModelTextAreaLabel<T>> {

    /**
     * A ValueBoxWithLabelEditor that has a Label as the widget
     */
    public EntityModelTextAreaLabelEditor(Renderer<T> renderer, Parser<T> parser) {
        this(new EntityModelTextAreaLabel<>(renderer, parser));
    }

    public EntityModelTextAreaLabelEditor(EntityModelTextAreaLabel<T> widget) {
        super(widget);
    }

    public EntityModelTextAreaLabelEditor(final boolean showBorder, final boolean disableResizing, Renderer<T> renderer, Parser<T> parser) {
        super(new EntityModelTextAreaLabel<T>(renderer, parser) {
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

    public void setCustomStyle(final String customStyle) {
        if (customStyle != null) {
            getElement().getElementsByTagName("textarea").getItem(0).addClassName(customStyle); //$NON-NLS-1$
        }
    }
}
