package org.ovirt.engine.ui.common.widget.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SimpleActionButton extends AbstractActionButton {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleActionButton> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Style style;

    public SimpleActionButton() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                button.removeStyleName(style.buttonMouseOver());
                button.addStyleName(style.buttonMouseOut());
            }
        });

        button.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                button.removeStyleName(style.buttonMouseOut());
                button.addStyleName(style.buttonMouseOver());
            }
        });

        button.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                button.removeStyleName(style.buttonMouseOver());
                button.addStyleName(style.buttonMouseOut());
            }
        });
    }

    interface Style extends CssResource {
        String buttonMouseOver();

        String buttonMouseOut();
    }

}
