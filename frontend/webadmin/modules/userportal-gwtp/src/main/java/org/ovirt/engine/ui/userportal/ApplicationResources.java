package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.resources.client.ImageResource;

public interface ApplicationResources extends CommonApplicationResources {
    // Basic icons

    @Source("images/general/tvlayout_disabled_mask.png")
    ImageResource disabledMask();

    @Source("images/general/tvlayout_disabled_mask_small.png")
    ImageResource disabledSmallMask();

    @Source("images/general/basic_cpu_icon.png")
    ImageResource basicCpuIcon();

    @Source("images/general/basic_memory_icon.png")
    ImageResource basicMemoryIcon();

    @Source("images/general/basic_os_icon.png")
    ImageResource basicOsIcon();

    @Source("images/general/basic_drive_icon.png")
    ImageResource basicDriveIcon();

    @Source("images/general/basic_console_icon.png")
    ImageResource basicConsoleIcon();

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

    @Source("images/actions/power.png")
    ImageResource powerIcon();

    @Source("images/actions/power_Disabled.png")
    ImageResource powerDisabledIcon();

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

    @Source("css/SideTabExtendedVmStyle.css")
    SideTabExtendedVmStyle sideTabExtendedVmStyle();

    @Source("css/SideTabWithDetailsViewStyle.css")
    SideTabWithDetailsViewStyle sideTabWithDetailsViewStyle();

    // resources tab
    @Source("images/general/vm_icon.png")
    ImageResource vmIcon();

    @Source("images/general/memory_icon.png")
    ImageResource memoryIcon();

    @Source("images/general/cpu_icon.png")
    ImageResource cpuIcon();

    @Source("images/general/network_icon.png")
    ImageResource networkIcon();

    @Source("images/general/storage_icon.png")
    ImageResource storageIcon();

    @Source("images/general/vm_icon_with_vm_text_inside.gif")
    ImageResource vmIconWithVmTextInside();

    @Source("images/general/disk_icon.gif")
    ImageResource vmDiskIcon();

    // splitter images
    @Source("images/splitter/basicViewSplitterTop.png")
    ImageResource basicViewSplitterTop();

    @Source("images/splitter/basicViewSplitterSnap.png")
    ImageResource basicViewSplitterSnap();

    @Source("images/splitter/extendedViewSplitterSnap.png")
    ImageResource extendedViewSplitterSnap();
}
