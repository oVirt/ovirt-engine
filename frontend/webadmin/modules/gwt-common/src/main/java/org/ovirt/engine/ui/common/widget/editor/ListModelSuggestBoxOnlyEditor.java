package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.user.client.ui.Widget;

public class ListModelSuggestBoxOnlyEditor extends ListModelSuggestBoxEditor {

    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);
        hideLabel();
        getContentWidgetContainer().getElement().getStyle().setFloat(Float.NONE);
    }

}
