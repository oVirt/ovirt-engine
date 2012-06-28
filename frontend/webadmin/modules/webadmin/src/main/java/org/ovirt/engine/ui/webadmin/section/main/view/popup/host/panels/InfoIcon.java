package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class InfoIcon extends FocusPanel{
    private final static ApplicationResources resources = ClientGinjectorProvider.instance().getApplicationResources();
    private final Image infoImage = new Image(resources.dialogIconHelp());
    private final Image infoImageHover = new Image(resources.dialogIconHelpRollover());
    private final DecoratedPopupPanel infoPanel = new DecoratedPopupPanel(true);

    public InfoIcon(SafeHtml text) {
        super();

        setWidget(infoImage);
        infoPanel.setWidget(new HTML(text));
        infoPanel.getElement().getStyle().setZIndex(1);

        addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                setWidget(infoImage);
                infoPanel.hide(true);
            }
        });

        addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                setWidget(infoImageHover);
                infoPanel.showRelativeTo(InfoIcon.this);
            }
        });
    }
}
