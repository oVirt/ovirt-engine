package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;

public class VolumeCapacityRenderer<T extends Number> extends AbstractRenderer<T> {

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();
    private final CommonApplicationConstants constants;

    public VolumeCapacityRenderer(CommonApplicationConstants constants) {
        this.constants = constants;
    }

    @Override
    public String render(T size) {
        if (size != null) {
            Pair<SizeUnit, Double> sizeWithUnits = SizeConverter.autoConvert(size.longValue(), SizeUnit.BYTES);
            return messages.sizeUnitString(formatSize(sizeWithUnits.getSecond()),
                    EnumTranslator.getInstance().translate(sizeWithUnits.getFirst()));
        } else {
            return constants.notAvailableLabel();
        }
    }

    public String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }
}
