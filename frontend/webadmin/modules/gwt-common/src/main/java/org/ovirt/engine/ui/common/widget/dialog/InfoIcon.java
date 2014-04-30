package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.TooltipPanel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class InfoIcon extends FocusPanel {
    private Image infoImage;
    private Image infoImageHover;
    private final TooltipPanel infoPanel;

    public InfoIcon(SafeHtml text, CommonApplicationResources resources) {
        super();

        initInfoImages(resources);

        setWidget(infoImage);
        infoPanel = new TooltipPanel(true, this) {

            @Override
            protected void onTooltipSourceMouseOver() {
                InfoIcon.this.setWidget(infoImageHover);
                infoPanel.showRelativeTo(InfoIcon.this);
            }

            @Override
            protected void onTooltipSourceMouseOut() {
                InfoIcon.this.setWidget(infoImage);
            }

        };
        setText(text);
    }

    private void initInfoImages(CommonApplicationResources resources) {
        infoImage = new Image(resources.dialogIconHelp());
        infoImageHover = new Image(resources.dialogIconHelpRollover());
    }

    public void setText(SafeHtml text) {
        infoPanel.setWidget(new HTML(text));
    }
}
