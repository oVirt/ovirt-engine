package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.resources.client.ImageResource;

public interface ApplicationResources extends CommonApplicationResources {

    // Login popup resources

    @Source("images/login/login_page_header_image.png")
    ImageResource loginPopupHeaderImage();

    @Source("images/login/login_page_header_logo.png")
    ImageResource loginPopupHeaderLogoImage();

    @Source("images/login/login_page_header_title.png")
    ImageResource loginPopupHeaderTitleImage();

    // OS type small images

    @Source("images/os/Other.jpg")
    ImageResource otherOsSmallImage();

    @Source("images/os/OtherLinux.jpg")
    ImageResource otherLinuxSmallImage();

    @Source("images/os/RHEL3.jpg")
    ImageResource RHEL3SmallImage();

    @Source("images/os/RHEL3x64.jpg")
    ImageResource RHEL3x64SmallImage();

    @Source("images/os/RHEL4.jpg")
    ImageResource RHEL4SmallImage();

    @Source("images/os/RHEL4x64.jpg")
    ImageResource RHEL4x64SmallImage();

    @Source("images/os/RHEL5.jpg")
    ImageResource RHEL5SmallImage();

    @Source("images/os/RHEL5x64.jpg")
    ImageResource RHEL5x64SmallImage();

    @Source("images/os/RHEL6.jpg")
    ImageResource RHEL6SmallImage();

    @Source("images/os/RHEL6x64.jpg")
    ImageResource RHEL6x64SmallImage();

    @Source("images/os/Unassigned.jpg")
    ImageResource unassignedSmallImage();

    @Source("images/os/Windows2003.jpg")
    ImageResource Windows2003SmallImage();

    @Source("images/os/Windows2003x64.jpg")
    ImageResource Windows2003x64SmallImage();

    @Source("images/os/Windows2008.jpg")
    ImageResource Windows2008SmallImage();

    @Source("images/os/Windows2008R2x64.jpg")
    ImageResource Windows2008R2x64SmallImage();

    @Source("images/os/Windows2008x64.jpg")
    ImageResource Windows2008x64SmallImage();

    @Source("images/os/Windows7.jpg")
    ImageResource Windows7SmallImage();

    @Source("images/os/Windows7x64.jpg")
    ImageResource Windows7x64SmallImage();

    @Source("images/os/WindowsXP.jpg")
    ImageResource WindowsXPSmallImage();

    // Basic icons

    @Source("images/general/tvlayout_disabled_mask.png")
    ImageResource disabledMask();

    @Source("images/general/basic_cpu_icon.png")
    ImageResource basicCpuIcon();

    @Source("images/general/basic_memory_icon.png")
    ImageResource basicMemoryIcon();

    @Source("images/general/basic_os_icon.png")
    ImageResource basicOsIcon();

    @Source("images/vmtypes/desktop_vm_icon.png")
    ImageResource desktopVmIcon();

    @Source("images/vmtypes/pool_icon.png")
    ImageResource poolVmIcon();

    @Source("images/vmtypes/server_vm_icon.png")
    ImageResource serverVmIcon();

    // Action buttons

    @Source("images/actions/play.png")
    ImageResource playIcon();

    @Source("images/actions/play_Disabled.png")
    ImageResource playDisabledIcon();

    @Source("images/actions/pause.png")
    ImageResource pauseIcon();

    @Source("images/actions/pause_Disabled.png")
    ImageResource pauseDisabledIcon();

    @Source("images/actions/stop.png")
    ImageResource stopIcon();

    @Source("images/actions/stop_Disabled.png")
    ImageResource stopDisabledIcon();

    // status images
    @Source("images/status/pause_icon.png")
    ImageResource pausedIcon();

    // VM status icons

    @Source("images/status/PAUSED.png")
    ImageResource vmStatusPaused();

    @Source("images/status/QUESTIONMARK.png")
    ImageResource vmStatusUnknown();

    @Source("images/status/RUNNING.png")
    ImageResource vmStatusRunning();

    @Source("images/status/STARTING.png")
    ImageResource vmStatusStarting();

    @Source("images/status/STOPPED.png")
    ImageResource vmStatusStopped();

    @Source("images/status/STOPPING.png")
    ImageResource vmStatusStopping();

    @Source("images/status/WAITING.png")
    ImageResource vmStatusWaiting();

}
