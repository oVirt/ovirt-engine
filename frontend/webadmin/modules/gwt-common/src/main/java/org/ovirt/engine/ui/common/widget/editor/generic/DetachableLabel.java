package org.ovirt.engine.ui.common.widget.editor.generic;

import org.gwtbootstrap3.client.ui.constants.ColumnSize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Takes a Label and decorates it with the detachable icon
 */
public class DetachableLabel extends BaseEntityModelDetachableWidget {

    interface WidgetUiBinder extends UiBinder<Widget, DetachableLabel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends BaseStyle {
    }

    @UiField
    Image attachedSeparatedImage;

    @UiField(provided = true)
    Label decorated;

    @UiField
    Style style;

    @UiChild(tagname = "decorated", limit = 1)
    public void setHeader(Label decorated) {
        this.decorated = decorated;

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        initialize(decorated, attachedSeparatedImage, style);
    }

    public void setLabelStyleName(String labelStyleName) {
        decorated.setStyleName(labelStyleName);
    }

    @Override
    public void setLabelColSize(ColumnSize size) {
    }

    public void setWidgetColSize(ColumnSize size) {
    }

    @Override
    public void setUsePatternFly(boolean use) {
    }

}
