package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;

public class RebalanceFileSizeRenderer<T extends Number> extends AbstractRenderer<T> {

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    @Override
    public String render(T size) {
        Pair<SizeUnit, Double>  sizePair = SizeConverter.autoConvert(size.longValue(), SizeUnit.BYTES);
        return messages.sizeUnitString(formatSize(sizePair.getSecond()),
                EnumTranslator.getInstance().translate(sizePair.getFirst()));
    }

    public String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }
}
