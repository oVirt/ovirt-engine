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
                return GraphicsType.SPICE;
            case VNC:
                return GraphicsType.VNC;
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
                return null;
        }
    }
}
