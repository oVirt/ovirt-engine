package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface ApplicationResources extends CommonApplicationResources {

    @Override
    @Source("css/DialogBox.css")
    CssResource dialogBoxStyle();

    @Source("images/button/button_start.png")
    ImageResource dialogButtonUpStart();

    @Source("images/button/button_stretch.png")
    ImageResource dialogButtonUpStretch();

    @Source("images/button/button_end.png")
    ImageResource dialogButtonUpEnd();

    @Source("images/button/button_Over_start.png")
    ImageResource dialogButtonOverStart();

    @Source("images/button/button_Over_stretch.png")
    ImageResource dialogButtonOverStretch();

    @Source("images/button/button_Over_end.png")
    ImageResource dialogButtonOverEnd();

    @Source("images/button/button_Down_start.png")
    ImageResource dialogButtonDownStart();

    @Source("images/button/button_Down_stretch.png")
    ImageResource dialogButtonDownStretch();

    @Source("images/button/button_Down_end.png")
    ImageResource dialogButtonDownEnd();

    @Source("images/button/button_Disabled_start.png")
    ImageResource dialogButtonUpDisabledStart();

    @Source("images/button/button_Disabled_stretch.png")
    ImageResource dialogButtonUpDisabledStretch();

    @Source("images/button/button_Disabled_end.png")
    ImageResource dialogButtonUpDisabledEnd();

    @Source("images/dialog/info.png")
    ImageResource dialogLogoInfoImage();

    @Source("images/dialog/dialog_header_image.png")
    ImageResource dialogHeaderImage();

    @Source("images/dialog/error.png")
    ImageResource dialogLogoErrorImage();

    @Source("images/dialog/progress.gif")
    ImageResource dialogProgressImage();

    /* Login popup resources */
    @Source("images/login/login_page_header_image.png")
    ImageResource loginPopupHeaderImage();

    @Source("images/login/login_page_header_logo.png")
    ImageResource loginPopupHeaderLogoImage();

    @Source("images/login/login_page_header_title.png")
    ImageResource loginPopupHeaderTitleImage();

}
