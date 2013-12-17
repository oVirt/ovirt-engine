package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.user.client.ui.Widget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

public class EntityModelCheckBoxOnlyEditor extends EntityModelCheckBoxEditor {

    public EntityModelCheckBoxOnlyEditor() {
        super(Align.LEFT); // align not important since label is not shown
    }

    public EntityModelCheckBoxOnlyEditor(VisibilityRenderer visibilityRenderer) {
        super(Align.LEFT, visibilityRenderer);
    }

    public EntityModelCheckBoxOnlyEditor(VisibilityRenderer visibilityRenderer, boolean useFullWidthIfAvailable) {
        super(Align.LEFT, visibilityRenderer, useFullWidthIfAvailable);
    }

    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);

        getLabelElement().getStyle().setDisplay(Display.NONE);
        getContentWidget().getElement().getStyle().setFloat(Float.NONE);
    }
}
