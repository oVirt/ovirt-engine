package org.ovirt.engine.ui.common;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;

public interface CommonApplicationResources extends ClientBundle {

    @Source("images/page_prev_enable.png")
    ImageResource pagePrevEnable();

    @Source("images/page_prev_disable.png")
    ImageResource pagePrevDisable();

    @Source("images/page_next_enable.png")
    ImageResource pageNextEnable();

    @Source("images/page_next_disable.png")
    ImageResource pageNextDisable();

    @Source("images/refresh.png")
    ImageResource refreshButtonImage();

    // Slider

    @Source("images/slider.gif")
    ImageResource slider();

    @Source("images/sliderDisabled.gif")
    ImageResource sliderDisabled();

    @Source("images/sliderSliding.gif")
    ImageResource sliderSliding();

    @NotStrict
    @Source("css/SliderBar.css")
    CssResource sliderBarCss();

    @Source("images/expander.png")
    ImageResource expanderImage();

    @Source("images/button_down.png")
    ImageResource expanderDownImage();

    // Dialogs
    @Source("images/dialog/progress.gif")
    ImageResource dialogProgressImage();

    @Source("images/dialog/progress_dots.gif")
    ImageResource progressDotsImage();

    @Source("images/arrows/tri_down_normal.png")
    ImageResource arrowDownNormal();

    @Source("images/arrows/tri_down_click.png")
    ImageResource arrowDownClick();

    @Source("images/arrows/tri_down_over.png")
    ImageResource arrowDownOver();

    @Source("images/arrows/tri_down_disabled.png")
    ImageResource arrowDownDisabled();

    @Source("images/arrows/tri_up_normal.png")
    ImageResource arrowUpNormal();

    @Source("images/arrows/tri_up_click.png")
    ImageResource arrowUpClick();

    @Source("images/arrows/tri_up_over.png")
    ImageResource arrowUpOver();

    @Source("images/arrows/tri_up_disabled.png")
    ImageResource arrowUpDisabled();

    @Source("images/arrows/tri_left_normal.png")
    ImageResource arrowLeftNormal();

    @Source("images/arrows/tri_left_click.png")
    ImageResource arrowLeftClick();

    @Source("images/arrows/tri_left_over.png")
    ImageResource arrowLeftOver();

    @Source("images/arrows/tri_left_disabled.png")
    ImageResource arrowLeftDisabled();

    @Source("images/arrows/tri_right_normal.png")
    ImageResource arrowRightNormal();

    @Source("images/arrows/tri_right_click.png")
    ImageResource arrowRightClick();

    @Source("images/arrows/tri_right_over.png")
    ImageResource arrowRightOver();

    @Source("images/arrows/tri_right_disabled.png")
    ImageResource arrowRightDisabled();

    // Table image columns

    @Source("images/comment.png")
    ImageResource commentImage();

    @Source("images/log_normal.gif")
    ImageResource logNormalImage();

    @Source("images/log_warning.gif")
    ImageResource logWarningImage();

    @Source("images/log_error.gif")
    ImageResource logErrorImage();

    @Source("images/ok_small.gif")
    ImageResource okSmallImage();

    @Source("images/icon_alert_configure.png")
    ImageResource alertConfigureImage();

    @Source("images/up.gif")
    ImageResource upImage();

    @Source("images/up_disabled.gif")
    ImageResource upDisabledImage();

    @Source("images/down.gif")
    ImageResource downImage();

    @Source("images/down_disabled.gif")
    ImageResource downDisabledImage();

    @Source("images/host_activating.png")
    ImageResource hostActivating();

    @Source("images/prepare_for_maintenance.png")
    ImageResource prepareForMaintenance();

    @Source("images/admin.png")
    ImageResource adminImage();

    @Source("images/user.png")
    ImageResource userImage();

    @Source("images/user_tree.png")
    ImageResource userImage_tree();

    @Source("images/volume_bricks_down_warning.png")
    ImageResource volumeBricksDownWarning();

    @Source("images/volume_all_bricks_down_warning.png")
    ImageResource volumeAllBricksDownWarning();

    @Source("images/volume_georep_master.png")
    ImageResource volumeGeoRepMaster();

    @Source("images/volume_georep_slave.png")
    ImageResource volumeGeoRepSlave();

    @Source("images/snapshot_scheduled.png")
    ImageResource snapshotScheduledImage();

    // Model-bound widgets

    @Source("images/snapshot.png")
    ImageResource snapshotImage();

    @Source("images/storage.png")
    ImageResource storageImage();

    @Source("images/disk.png")
    ImageResource diskImage();

    @Source("images/wait.png")
    ImageResource waitImage();

    @Source("images/question_mark.png")
    ImageResource questionMarkImage();

    @Source("images/disk_shareable.png")
    ImageResource shareableDiskIcon();

    @Source("images/disk_readonly.png")
    ImageResource readOnlyDiskIcon();

    @Source("images/disk_lun.png")
    ImageResource externalDiskIcon();

    @Source("images/icon_increase.png")
    ImageResource increaseIcon();

    @Source("images/icon_decrease.png")
    ImageResource decreaseIcon();

    @Source("images/disk_bootable.png")
    ImageResource bootableDiskIcon();

    @Source("images/templates.png")
    ImageResource templatesImage();

    @Source("images/vms.png")
    ImageResource vmsImage();

    // Network
    @Source("images/network/icn_plugged.png")
    ImageResource pluggedNetworkImage();

    @Source("images/network/icn_un_plugged.png")
    ImageResource unpluggedNetworkImage();

    @Source("images/network/icn_network_linked.png")
    ImageResource linkedNetworkImage();

    @Source("images/network/icn_network_unlinked.png")
    ImageResource unlinkedNetworkImage();

    @Source("images/comboBoxDropDownIcon.png")
    ImageResource comboBoxDropDownIcon();

    @Source("images/left_scroll_arrow.png")
    ImageResource leftScrollArrow();

    @Source("images/left_scroll_arrow_disabled.png")
    ImageResource leftScrollArrowDisabled();

    @Source("images/right_scroll_arrow.png")
    ImageResource rightScrollArrow();

    @Source("images/right_scroll_arrow_disabled.png")
    ImageResource rightScrollArrowDisabled();

    @Source("images/dropdown_arrow.png")
    ImageResource dropdownArrow();

    @Source("images/triangle_down.gif")
    ImageResource triangle_down();

    @Source("images/memory_icon.png")
    ImageResource memorySmallIcon();

    @Source("images/disk_icon.png")
    ImageResource diskIcon();

    @Source("images/vm_conf_icon.png")
    ImageResource vmConfIcon();

    @Source("images/separator.gif")
    ImageResource separator();

    @Source("images/joined.png")
    ImageResource joinedIcon();

    @Source("images/separated.png")
    ImageResource separatedIcon();

    //Collapsible panel
    @Source("images/collapsed_header_icon.png")
    ImageResource collapsedHeaderArrow();

    @Source("images/expanded_header_icon.png")
    ImageResource expandedHeaderArrow();

    //Numa popup
    @Source("images/numa/vnuma_icon.png")
    ImageResource vNumaTitleIcon();

    @Source("images/numa/partial_vnuma_light_icon.png")
    ImageResource partialVNumaIcon();

    @Source("images/numa/pinned_partial_vnuma_light_icon.png")
    ImageResource pinnedPartialVNumaIcon();

    @Source("images/numa/vnuma_light_icon.png")
    ImageResource vNumaIcon();

    @Source("images/numa/pinned_vnuma_light_icon.png")
    ImageResource pinnedVNumaIcon();

    @Source("images/numa/drag_handle.png")
    ImageResource dragHandleIcon();

    @Source("images/numa/pinned_partial_vnuma_dark_icon.png")
    ImageResource darkPinnedPartialVNumaIcon();

    @Source("images/numa/pinned_vnuma_dark_icon.png")
    ImageResource darkPinnedVNumaIcon();

    @Source("images/numa/partial_vnuma_dark_icon.png")
    ImageResource darkPartialVNumaIcon();

    @Source("images/numa/vnuma_dark_icon.png")
    ImageResource darkVNumaIcon();

    // vm device general type

    @Source("images/disk.png")
    ImageResource diskDeviceGeneralTypeIcon();

    @Source("images/nic.png")
    ImageResource interfaceDeviceGeneralTypeIcon();

    @Source("images/device_video.png")
    ImageResource videoDeviceGeneralTypeIcon();

    @Source("images/device_graphics.png")
    ImageResource graphicsDeviceGeneralTypeIcon();

    @Source("images/device_sound.png")
    ImageResource soundDeviceGeneralTypeIcon();

    @Source("images/device_controller.png")
    ImageResource controllerDeviceGeneralTypeIcon();

    @Source("images/device_cpu_ballooning.png")
    ImageResource balloonDeviceGeneralTypeIcon();

    @Source("images/device_channel.png")
    ImageResource channelDeviceGeneralTypeIcon();

    @Source("images/device_usb.png")
    ImageResource redirDeviceGeneralTypeIcon();

    @Source("images/device_console.png")
    ImageResource consoleDeviceGeneralTypeIcon();

    @Source("images/device_rng.png")
    ImageResource rngDeviceGeneralTypeIcon();

    @Source("images/device_smartcard.png")
    ImageResource smartcardDeviceGeneralTypeIcon();

    @Source("images/device_tpm.png")
    ImageResource tpmDeviceGeneralTypeIcon();

    @Source("images/device_watchdog.png")
    ImageResource watchdogDeviceGeneralTypeIcon();

    @Source("images/device_hostdev.png")
    ImageResource hostdevDeviceGeneralTypeIcon();

    @Source("images/memory_icon.png")
    ImageResource memoryDeviceGeneralTypeIcon();

    @Source("images/errata.png")
    ImageResource errataImage();

}
