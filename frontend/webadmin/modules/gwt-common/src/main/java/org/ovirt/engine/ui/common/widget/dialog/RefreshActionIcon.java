package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class RefreshActionIcon extends FocusPanel {
    private Image refreshImage;
    private final DecoratedPopupPanel refreshPanel = new DecoratedPopupPanel(true);
    private ClickHandler refreshIconClickListener;

    public RefreshActionIcon(SafeHtml text, CommonApplicationResources resources) {
        super();
        initInfoImages(resources);
        setWidget(refreshImage);
        refreshPanel.setWidget(new HTML(text));
        refreshPanel.getElement().getStyle().setZIndex(1);
    }

    private void initInfoImages(CommonApplicationResources resources) {
        setRefreshImage(new Image(resources.refreshButtonImage()));
    }

    public Image getRefreshImage() {
        return refreshImage;
    }

    public void setRefreshImage(Image refreshImage) {
        this.refreshImage = refreshImage;
    }

    public ClickHandler getRefreshIconClickListener() {
        return refreshIconClickListener;
    }

    public void setRefreshIconClickListener(ClickHandler refreshIconClickListener) {
        this.refreshIconClickListener = refreshIconClickListener;
        addClickHandler(refreshIconClickListener);
    }
}
