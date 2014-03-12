package org.ovirt.engine.ui.webadmin.widget.label;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class LabelWithToolTip extends Label {

    private final HTML tooltip = new HTML();
    private final DecoratedPopupPanel tooltipPanel = new DecoratedPopupPanel();
    private String title;

    public LabelWithToolTip() {
        this("", -1); //$NON-NLS-1$
    }

    public LabelWithToolTip(String text, int length) {
        super(text);

        if (length > -1 && text.length() > length) {
            setText(text.substring(0, length - 3) + "..."); //$NON-NLS-1$
        }
        tooltipPanel.setWidget(tooltip);
        tooltipPanel.getElement().getStyle().setZIndex(1);
        setTitle(text);
        registerHandlers();
    }

    private void registerHandlers() {
        addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                if(!"".equals(title)){ //$NON-NLS-1$
                    tooltip.setHTML(title);
                    tooltipPanel.showRelativeTo(LabelWithToolTip.this);
                }
            }
        });
        addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                tooltipPanel.hide(true);
            }
        });
    }

    public LabelWithToolTip(String text) {
        this(text, -1);
    }

    @Override
    public void setTitle(String text) {
        this.title = text;
    }
}
