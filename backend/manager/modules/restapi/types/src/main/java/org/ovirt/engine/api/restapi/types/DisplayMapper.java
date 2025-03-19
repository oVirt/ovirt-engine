package org.ovirt.engine.api.restapi.types;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.VideoType;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmBase;

public class DisplayMapper {

    @Mapping(from = GraphicsType.class, to = DisplayType.class)
    public static DisplayType map(GraphicsType graphicsType, DisplayType displayType) {
        if (graphicsType == GraphicsType.SPICE) {
            return DisplayType.SPICE;
        } else if (graphicsType == GraphicsType.VNC) {
            return DisplayType.VNC;
        }
        return displayType;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.DisplayType.class, to = VideoType.class)
    public static VideoType map(org.ovirt.engine.core.common.businessentities.DisplayType displayType, VideoType videoType) {
        switch (displayType) {
            case vga:
                return VideoType.VGA;
            case bochs:
                return VideoType.BOCHS;
            case cirrus:
                return VideoType.CIRRUS;
            case qxl:
                return VideoType.QXL;
            default:
                return videoType;
        }
    }

    @Mapping(from = VideoType.class, to = GraphicsType.class)
    public static GraphicsType map(VideoType videoType, GraphicsType graphicsType) {
        switch (videoType) {
            case VGA:
            case CIRRUS:
            case BOCHS:
                return GraphicsType.VNC;
            case QXL:
                return GraphicsType.SPICE;
            default:
                return graphicsType;
        }
    }

    @Mapping(from = DisplayType.class, to = GraphicsType.class)
    public static GraphicsType map(DisplayType displayType, GraphicsType graphicsType) {
        switch (displayType) {
            case SPICE:
                return GraphicsType.SPICE;
            case VNC:
                return GraphicsType.VNC;
            default:
                return graphicsType;
        }
    }

    @Mapping(from = VideoType.class, to = org.ovirt.engine.core.common.businessentities.DisplayType.class)
    public static org.ovirt.engine.core.common.businessentities.DisplayType map(VideoType videoType, org.ovirt.engine.core.common.businessentities.DisplayType displayType) {
        switch (videoType) {
            case VGA:
                return org.ovirt.engine.core.common.businessentities.DisplayType.vga;
            case BOCHS:
                return org.ovirt.engine.core.common.businessentities.DisplayType.bochs;
            case CIRRUS:
                return org.ovirt.engine.core.common.businessentities.DisplayType.cirrus;
            case QXL:
                return org.ovirt.engine.core.common.businessentities.DisplayType.qxl;
            default:
                return displayType;
        }
    }

    @Mapping(from = VmBase.class, to = Display.class)
    public static Display map(VmBase vmBase, Display display) {
        Display result = (display == null)
                ? new Display()
                : display;

        result.setMonitors(vmBase.getNumOfMonitors());
        result.setAllowOverride(vmBase.isAllowConsoleReconnect());
        result.setSmartcardEnabled(vmBase.isSmartcardEnabled());
        result.setKeyboardLayout(vmBase.getVncKeyboardLayout());
        result.setFileTransferEnabled(vmBase.isSpiceFileTransferEnabled());
        result.setCopyPasteEnabled(vmBase.isSpiceCopyPasteEnabled());
        result.setDisconnectAction(VmBaseMapper.map(vmBase.getConsoleDisconnectAction(), null).toString());
        result.setDisconnectActionDelay(vmBase.getConsoleDisconnectActionDelay());
        if (vmBase.getDefaultDisplayType() != null) {
            result.setVideoType(DisplayMapper.map(vmBase.getDefaultDisplayType(), null));
        }
        return result;
    }

    /**
     * For backwards compatibility. Derives graphics type (backend) from display (rest).
     */
    public static void fillDisplayInParams(Vm vm, RunVmOnceParams params) {
        if (params == null) {
            return;
        }

        if (vm.isSetDisplay() && vm.getDisplay().isSetType()) {
            DisplayType displayType = vm.getDisplay().getType();
            if (displayType != null) {
                org.ovirt.engine.core.common.businessentities.DisplayType display = mapDisplayType(displayType, null);
                if (display != null) {
                    Set<GraphicsType> graphics = new HashSet<>();
                    switch (display) {
                        case qxl:
                            graphics.add(GraphicsType.SPICE);
                            break;
                        case vga:
                        case cirrus:
                        case bochs:
                            graphics.add(GraphicsType.VNC);
                            break;
                    }
                    params.setRunOnceGraphics(graphics);
                }
            }
        }
    }

    @Mapping(from = DisplayType.class, to = org.ovirt.engine.core.common.businessentities.DisplayType.class)
    public static org.ovirt.engine.core.common.businessentities.DisplayType mapDisplayType(DisplayType type, org.ovirt.engine.core.common.businessentities.DisplayType incoming) {
        switch (type) {
            case VNC:
                return org.ovirt.engine.core.common.businessentities.DisplayType.vga;
            case SPICE:
                return org.ovirt.engine.core.common.businessentities.DisplayType.qxl;
            default:
                return incoming;
        }
    }
}
