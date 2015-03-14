package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;


public class InfoIcon extends FocusPanel {
    private Image infoImage;
    private Image infoImageHover;
    private final DecoratedPopupPanel infoPanel = new DecoratedPopupPanel(true);

    private final static CommonApplicationResources resources = AssetProvider.getResources();

    public InfoIcon(SafeHtml text) {
        super();

        initInfoImages();

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

    private void initInfoImages() {
        infoImage = new Image(resources.dialogIconHelp());
        infoImageHover = new Image(resources.dialogIconHelpRollover());
    }

    public void setText(SafeHtml text) {
        infoPanel.setWidget(new HTML(text));
    }
}
