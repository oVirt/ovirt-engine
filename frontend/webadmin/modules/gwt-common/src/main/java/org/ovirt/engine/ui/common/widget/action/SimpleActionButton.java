package org.ovirt.engine.ui.common.widget.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;

public class SimpleActionButton extends AbstractActionButton {

    interface WidgetUiBinder extends UiBinder<IsWidget, SimpleActionButton> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Style style;

    public SimpleActionButton() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this).asWidget());

        button.addClickHandler(event -> {
            button.removeStyleName(style.buttonMouseOver());
            button.addStyleName(style.buttonMouseOut());
        });

        button.addMouseOverHandler(event -> {
            button.removeStyleName(style.buttonMouseOut());
            button.addStyleName(style.buttonMouseOver());
        });

        button.addMouseOutHandler(event -> {
            button.removeStyleName(style.buttonMouseOver());
            button.addStyleName(style.buttonMouseOut());
        });
    }

    interface Style extends CssResource {
        String buttonMouseOver();

        String buttonMouseOut();
    }

}
