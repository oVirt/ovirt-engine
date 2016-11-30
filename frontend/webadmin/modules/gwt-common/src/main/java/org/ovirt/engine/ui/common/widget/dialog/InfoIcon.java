package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class InfoIcon extends FocusPanel {
    private Image infoImage;

    private final static CommonApplicationResources resources = AssetProvider.getResources();


    public InfoIcon(SafeHtml html) {
        this(html.asString());
    }

    @UiConstructor
    public InfoIcon(String text) {
        super();

        infoImage = new Image(resources.dialogIconHelp());
        setText(text);
        setWidget(infoImage);

        // mouse hover image swap
        addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                infoImage.setUrl(resources.dialogIconHelp().getSafeUri());
            }
        });
        addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                infoImage.setUrl(resources.dialogIconHelpRollover().getSafeUri());
            }
        });
    }

    public void setText(String text) {
        infoImage.setTitle(text);
    }
}
