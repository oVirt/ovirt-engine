package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

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

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    public RefreshActionIcon(SafeHtml text) {
        super();
        initInfoImages();
        setWidget(refreshImage);
        refreshPanel.setWidget(new HTML(text));
        refreshPanel.getElement().getStyle().setZIndex(1);
    }

    private void initInfoImages() {
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
