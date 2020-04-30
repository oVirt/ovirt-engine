package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDeviceFeEntity;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VmDeviceGeneralTypeColumn<T> extends AbstractSafeHtmlColumn<T> {
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationResources resources = AssetProvider.getResources();

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
            case TPM:
                return resources.tpmDeviceGeneralTypeIcon();
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
        makeSortable(Comparator.comparing((T o) -> getDeviceFromObject(o) != null)
                .thenComparing(o -> getDeviceFromObject(o).getType().getValue(), new LexoNumericComparator()));
    }

    private VmDevice getDeviceFromObject(T object) {
        VmDevice device = null;
        if (object instanceof VmDeviceFeEntity) {
            device = ((VmDeviceFeEntity) object).getVmDevice();
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
    public SafeHtml getTooltip(T object) {
        VmDevice device = getDeviceFromObject(object);
        if (device != null) {
            switch (device.getType()) {
            case DISK:
                return SafeHtmlUtils.fromTrustedString(constants.diskDeviceGeneralType());
            case INTERFACE:
                return SafeHtmlUtils.fromTrustedString(constants.interfaceDeviceGeneralType());
            case VIDEO:
                return SafeHtmlUtils.fromTrustedString(constants.videoDeviceGeneralType());
            case GRAPHICS:
                return SafeHtmlUtils.fromTrustedString(constants.graphicsDeviceGeneralType());
            case SOUND:
                return SafeHtmlUtils.fromTrustedString(constants.soundDeviceGeneralType());
            case CONTROLLER:
                return SafeHtmlUtils.fromTrustedString(constants.controllerDeviceGeneralType());
            case BALLOON:
                return SafeHtmlUtils.fromTrustedString(constants.balloonDeviceGeneralType());
            case CHANNEL:
                return SafeHtmlUtils.fromTrustedString(constants.channelDeviceGeneralType());
            case REDIR:
                return SafeHtmlUtils.fromTrustedString(constants.redirDeviceGeneralType());
            case CONSOLE:
                return SafeHtmlUtils.fromTrustedString(constants.consoleDeviceGeneralType());
            case RNG:
                return SafeHtmlUtils.fromTrustedString(constants.rngDeviceGeneralType());
            case SMARTCARD:
                return SafeHtmlUtils.fromTrustedString(constants.smartcardDeviceGeneralType());
            case WATCHDOG:
                return SafeHtmlUtils.fromTrustedString(constants.watchdogDeviceGeneralType());
            case HOSTDEV:
                return SafeHtmlUtils.fromTrustedString(constants.hostdevDeviceGeneralType());
            case MEMORY:
                return SafeHtmlUtils.fromTrustedString(constants.memoryDeviceGeneralType());
            case UNKNOWN:
                return SafeHtmlUtils.fromTrustedString(constants.unknownDeviceGeneralType());
            }
        }
        return null;
    }
}
