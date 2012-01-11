package org.ovirt.engine.ui.webadmin;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;

public interface ApplicationResources extends ClientBundle {

    @Source("images/admin.png")
    ImageResource adminImage();

    @Source("images/icon_alert_configure.png")
    ImageResource alertConfigureImage();

    @Source("images/icon_alert_configure.png")
    ImageResource alertsImage();

    @Source("images/bg.png")
    ImageResource bgImage();

    @Source("images/bookmark.gif")
    ImageResource bookmarkImage();

    @Source("images/cluster.png")
    ImageResource clusterImage();

    @Source("images/clusters.png")
    ImageResource clustersImage();

    @Source("images/console_disabled.png")
    ImageResource consoleDisabledImage();

    @Source("images/console.png")
    ImageResource consoleImage();

    @Source("images/datacenter.png")
    ImageResource dataCenterImage();

    @Source("images/desktop.gif")
    ImageResource desktopImage();

    /* Dialog resources */
    @Source("images/dialog/dialog_header_image.png")
    ImageResource dialogHeaderImage();

    @Source("images/dialog/error.png")
    ImageResource dialogLogoErrorImage();

    @Source("images/dialog/info.png")
    ImageResource dialogLogoInfoImage();

    @Source("images/dialog/warning.png")
    ImageResource dialogLogoWarningImage();

    @Source("images/dialog/guide.png")
    ImageResource dialogLogoGuideImage();

    @Source("images/dialog/progress.gif")
    ImageResource dialogProgressImage();

    @Source("images/disk.png")
    ImageResource diskImage();

    @Source("images/down.gif")
    ImageResource downImage();

    @Source("images/error.gif")
    ImageResource errorImage();

    @Source("images/events_gray.png")
    ImageResource eventsGrayImage();

    @Source("images/events.png")
    ImageResource eventsImage();

    @Source("images/expander.png")
    ImageResource expanderImage();

    @Source("images/button_down.png")
    ImageResource expanderDownImage();

    @Source("images/btn_guide_hover.png")
    ImageResource guideHoverMediumImage();

    @Source("images/guide.png")
    ImageResource guideImage();

    @Source("images/btn_guide.png")
    ImageResource guideMediumImage();

    @Source("images/btn_guide_pressed.png")
    ImageResource guidePressedMediumImage();

    @Source("images/icn_guide_disabled.png")
    ImageResource guideSmallDisabledImage();

    @Source("images/icn_guide.png")
    ImageResource guideSmallImage();

    @Source("images/bookmark.gif")
    ImageResource headerSearchBookmarkImage();

    @Source("images/search_button.png")
    ImageResource headerSearchButtonImage();

    @Source("images/host_error.gif")
    ImageResource hostErrorImage();

    @Source("images/host.png")
    ImageResource hostImage();

    @Source("images/host_installing.png")
    ImageResource hostInstallingImage();

    @Source("images/hosts.png")
    ImageResource hostsImage();

    @Source("images/Lock.png")
    ImageResource lockImage();

    @Source("images/log_error.gif")
    ImageResource logErrorImage();

    /* Login popup resources */
    @Source("images/login/login_page_header_image.png")
    ImageResource loginPopupHeaderImage();

    @Source("images/login/login_page_header_logo.png")
    ImageResource loginPopupHeaderLogoImage();

    @Source("images/login/login_page_header_title.png")
    ImageResource loginPopupHeaderTitleImage();

    @Source("images/log_normal.gif")
    ImageResource logNormalImage();

    @Source("images/logo.png")
    ImageResource logoImage();

    @Source("images/log_warning.gif")
    ImageResource logWarningImage();

    @Source("images/host_maintenance.png")
    ImageResource maintenanceImage();

    @Source("images/many_desktops.png")
    ImageResource manyDesktopsImage();

    @Source("images/minus.png")
    ImageResource minusImage();

    @Source("images/monitor.png")
    ImageResource monitorImage();

    @Source("images/nonoperational.png")
    ImageResource nonOperationalImage();

    @Source("images/ok_small.gif")
    ImageResource okSmallImage();

    @Source("images/pause.gif")
    ImageResource pauseImage();

    @Source("images/icn_pause_disabled.png")
    ImageResource pauseVmDisabledImage();

    @Source("images/icn_pause.png")
    ImageResource pauseVmImage();

    @Source("images/play.gif")
    ImageResource playImage();

    @Source("images/plus_disabled.png")
    ImageResource plusDisabledImage();

    @Source("images/plus.png")
    ImageResource plusImage();

    @Source("images/question_mark.png")
    ImageResource questionMarkImage();

    @Source("images/log_warning.gif")
    ImageResource alertImage();

    @Source("images/tag_locked.png")
    ImageResource readOnlyTagImage();

    @Source("images/refresh.png")
    ImageResource refreshButtonImage();

    @Source("images/icn_play_disabled.png")
    ImageResource runVmDisabledImage();

    @Source("images/icn_play.png")
    ImageResource runVmImage();

    @Source("images/search_button.png")
    ImageResource searchButtonImage();

    @Source("images/server.png")
    ImageResource serverImage();

    @Source("images/snapshot.png")
    ImageResource snapshotImage();

    @Source("images/split.png")
    ImageResource splitImage();

    @Source("images/split-rotate.png")
    ImageResource splitRotateImage();

    @Source("images/stop.gif")
    ImageResource stopImage();

    @Source("images/icn_stop_disabled.png")
    ImageResource stopVmDisabledImage();

    @Source("images/icn_stop.png")
    ImageResource stopVmImage();

    @Source("images/storage.png")
    ImageResource storageImage();

    @Source("images/storages.png")
    ImageResource storagesImage();

    @Source("images/system.png")
    ImageResource systemImage();

    @Source("images/tag.png")
    ImageResource tagImage();

    @Source("images/icn_tag_link_disabled.gif")
    ImageResource tagLinkDisabledImage();

    @Source("images/icn_tag_link.png")
    ImageResource tagLinkImage();

    @Source("images/tag_pin_green.png")
    ImageResource tagPinGreenImage();

    @Source("images/tag_pin.png")
    ImageResource tagPinImage();

    @Source("images/templates.png")
    ImageResource templatesImage();

    @Source("images/torn_chain.png")
    ImageResource tornChainImage();

    @Source("images/unconfigured.png")
    ImageResource unconfiguredImage();

    @Source("images/up.gif")
    ImageResource upImage();

    @Source("images/upalert.png")
    ImageResource upalertImage();

    @Source("images/user_group.png")
    ImageResource userGroupImage();

    @Source("images/user.png")
    ImageResource userImage();

    @Source("images/vm.png")
    ImageResource vmImage();

    @Source("images/vms.png")
    ImageResource vmsImage();

    @Source("images/wait.png")
    ImageResource waitImage();

    @Source("images/window_bg.png")
    ImageResource windowBgImage();

    @Source("images/window_header.png")
    ImageResource windowHeaderImage();

    @Source("images/button_up.png")
    ImageResource bootSequenceListBoxButtonUp();

    @Source("images/button_down.png")
    ImageResource bootSequenceListBoxButtonDown();

    @Source("images/wrench.png")
    ImageResource wrenchImage();

    @Source("images/plusButton.png")
    ImageResource plusButtonImage();

    @Source("images/enlarge_bottom_panel.png")
    ImageResource enlargeFooterPanelImage();

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

    // Slider
    /**
     * An image used for the sliding knob.
     *
     * @return a prototype of this image
     */
    @Source("images/slider.gif")
    ImageResource slider();

    /**
     * An image used for the sliding knob.
     *
     * @return a prototype of this image
     */
    @Source("images/sliderDisabled.gif")
    ImageResource sliderDisabled();

    /**
     * An image used for the sliding knob while sliding.
     *
     * @return a prototype of this image
     */
    @Source("images/sliderSliding.gif")
    ImageResource sliderSliding();

    @NotStrict
    @Source("css/SliderBar.css")
    CssResource sliderBarCss();
}
