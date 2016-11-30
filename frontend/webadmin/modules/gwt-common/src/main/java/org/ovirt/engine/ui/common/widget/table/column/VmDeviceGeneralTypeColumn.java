package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VmDeviceGeneralTypeColumn<T> extends AbstractSafeHtmlColumn<T> {
    private final static CommonApplicationConstants constants = AssetProvider.getConstants();
    private final static CommonApplicationResources resources = AssetProvider.getResources();

    private ImageResource getImage(T object) {
        VmDevice device = getDeviceFromObject(object);
        if (device != null) {
            switch (device.getType()) {
            case DISK:
                return resources.diskDeviceGeneralTypeIcon();
            case INTERFACE:
                return resources.interfaceDeviceGeneralTypeIcon();
            case VIDEO:
                return resources.videoDeviceGeneralTypeIcon();
            case GRAPHICS:
                return resources.graphicsDeviceGeneralTypeIcon();
            case SOUND:
                return resources.soundDeviceGeneralTypeIcon();
            case CONTROLLER:
                return resources.controllerDeviceGeneralTypeIcon();
            case BALLOON:
                return resources.balloonDeviceGeneralTypeIcon();
            case CHANNEL:
                return resources.channelDeviceGeneralTypeIcon();
            case REDIR:
                return resources.redirDeviceGeneralTypeIcon();
            case CONSOLE:
                return resources.consoleDeviceGeneralTypeIcon();
            case RNG:
                return resources.rngDeviceGeneralTypeIcon();
            case SMARTCARD:
                return resources.smartcardDeviceGeneralTypeIcon();
            case WATCHDOG:
                return resources.watchdogDeviceGeneralTypeIcon();
            case HOSTDEV:
                return resources.hostdevDeviceGeneralTypeIcon();
            case MEMORY:
                return resources.memoryDeviceGeneralTypeIcon();
            case UNKNOWN:
                return resources.questionMarkImage();
            }
        }
        return null;
    }

    public void makeSortable() {
        makeSortable(new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                String type1 = (getDeviceFromObject(o1) == null) ? null : getDeviceFromObject(o1).getType().getValue();
                String type2 = (getDeviceFromObject(o2) == null) ? null : getDeviceFromObject(o2).getType().getValue();
                return LexoNumericComparator.comp(type1, type2);
            }
        });
    }

    private VmDevice getDeviceFromObject(T object) {
        VmDevice device = null;
        if (object instanceof VmDevice) {
            device = (VmDevice) object;
        } else if (object instanceof PairQueryable && ((PairQueryable) object).getFirst() instanceof VmDevice) {
            device = ((PairQueryable<VmDevice, VM>) object).getFirst();
        }
        return device;
    }

    @Override
    public SafeHtml getValue(T object) {
        ImageResource image = getImage(object);
        return (image == null) ? null : SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(image).getHTML());
    }

    @Override
    public String getTooltip(T object) {
        VmDevice device = getDeviceFromObject(object);
        if (device != null) {
            switch (device.getType()) {
            case DISK:
                return constants.diskDeviceGeneralType();
            case INTERFACE:
                return constants.interfaceDeviceGeneralType();
            case VIDEO:
                return constants.videoDeviceGeneralType();
            case GRAPHICS:
                return constants.graphicsDeviceGeneralType();
            case SOUND:
                return constants.soundDeviceGeneralType();
            case CONTROLLER:
                return constants.controllerDeviceGeneralType();
            case BALLOON:
                return constants.balloonDeviceGeneralType();
            case CHANNEL:
                return constants.channelDeviceGeneralType();
            case REDIR:
                return constants.redirDeviceGeneralType();
            case CONSOLE:
                return constants.consoleDeviceGeneralType();
            case RNG:
                return constants.rngDeviceGeneralType();
            case SMARTCARD:
                return constants.smartcardDeviceGeneralType();
            case WATCHDOG:
                return constants.watchdogDeviceGeneralType();
            case HOSTDEV:
                return constants.hostdevDeviceGeneralType();
            case MEMORY:
                return constants.memoryDeviceGeneralType();
            case UNKNOWN:
                return constants.unknownDeviceGeneralType();
            }
        }
        return null;
    }
}
