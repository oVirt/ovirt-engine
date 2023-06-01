package org.ovirt.engine.api.restapi.types;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class DisplayMapper {

    @Mapping(from = GraphicsType.class, to = DisplayType.class)
    public static DisplayType map(GraphicsType graphicsType, DisplayType displayType) {
        if (graphicsType == GraphicsType.SPICE) {
            return DisplayType.SPICE;
        } else if (graphicsType == GraphicsType.VNC) {
            return DisplayType.VNC;
        }
        return null;
    }

    @Mapping(from = DisplayType.class, to = GraphicsType.class)
    public static GraphicsType map(DisplayType displayType, GraphicsType graphicsType) {
        switch (displayType) {
            case SPICE:
            case QXL:
                return GraphicsType.SPICE;
            case VNC:
            case CIRRUS:
            case VGA:
            case BOCHS:
                return GraphicsType.VNC;
            default:
                return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.DisplayType.class, to = DisplayType.class)
    public static DisplayType mapDisplay(org.ovirt.engine.core.common.businessentities.DisplayType displayType, DisplayType disType) {
        switch (displayType) {
            case cirrus:
                return DisplayType.CIRRUS;
            case qxl:
                return DisplayType.QXL;
            case vga:
                return DisplayType.VGA;
            case bochs:
                return DisplayType.BOCHS;
            case none:
                return DisplayType.NONE;
            default:
                return null;
        }
    }

    @Mapping(from = DisplayType.class, to = org.ovirt.engine.core.common.businessentities.DisplayType.class)
    public static org.ovirt.engine.core.common.businessentities.DisplayType mapDisplay(DisplayType displayType, org.ovirt.engine.core.common.businessentities.DisplayType disType) {
        switch (displayType) {
            case CIRRUS:
                return org.ovirt.engine.core.common.businessentities.DisplayType.cirrus;
            case QXL:
                return org.ovirt.engine.core.common.businessentities.DisplayType.qxl;
            case VGA:
                return org.ovirt.engine.core.common.businessentities.DisplayType.vga;
            case BOCHS:
                return org.ovirt.engine.core.common.businessentities.DisplayType.bochs;
            case NONE:
                return org.ovirt.engine.core.common.businessentities.DisplayType.none;
            default:
                return null;
        }
    }

    @Mapping(from = VmTemplate.class, to = Display.class)
    public static Display map(VmTemplate vmTemplate, Display display) {
        Display result = (display == null)
                ? new Display()
                : display;

        result.setMonitors(vmTemplate.getNumOfMonitors());
        result.setAllowOverride(vmTemplate.isAllowConsoleReconnect());
        result.setSmartcardEnabled(vmTemplate.isSmartcardEnabled());
        result.setKeyboardLayout(vmTemplate.getVncKeyboardLayout());
        result.setFileTransferEnabled(vmTemplate.isSpiceFileTransferEnabled());
        result.setCopyPasteEnabled(vmTemplate.isSpiceCopyPasteEnabled());

        return result;
    }

    @Mapping(from = InstanceType.class, to = Display.class)
    public static Display map(InstanceType instanceType, Display display) {
        Display result = (display == null)
                ? new Display()
                : display;

        result.setMonitors(instanceType.getNumOfMonitors());
        result.setSmartcardEnabled(instanceType.isSmartcardEnabled());

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
                Set<GraphicsType> graphics = new HashSet<>();
                switch (displayType) {
                    case SPICE:
                    case QXL:
                        graphics.add(GraphicsType.SPICE);
                        break;
                    case VNC:
                    case VGA:
                    case CIRRUS:
                    case BOCHS:
                        graphics.add(GraphicsType.VNC);
                        break;
                }
                params.setRunOnceGraphics(graphics);
            }
        }
    }
}
