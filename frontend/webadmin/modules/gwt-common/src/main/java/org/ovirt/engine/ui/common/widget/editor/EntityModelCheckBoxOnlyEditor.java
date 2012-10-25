package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.user.client.ui.Widget;

public class EntityModelCheckBoxOnlyEditor extends EntityModelCheckBoxEditor {
    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);

        getLabelElement().getStyle().setDisplay(Display.NONE);
        getContentWidget().getElement().getStyle().setFloat(Float.NONE);
    }
}
