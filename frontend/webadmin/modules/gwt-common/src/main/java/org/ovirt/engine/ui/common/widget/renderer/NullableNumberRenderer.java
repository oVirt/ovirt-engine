package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.client.NumberFormatRenderer;

public class NullableNumberRenderer extends NumberFormatRenderer {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private String nullString = constants.unAvailablePropertyLabel();

    public NullableNumberRenderer() {
        super();
    }

    public NullableNumberRenderer(NumberFormat format) {
        super(format);
    }

    public NullableNumberRenderer(String nullString) {
        this();
        this.nullString = nullString;
    }

    @Override
    public String render(Number object) {
        String formattedNumber = super.render(object);
        return new EmptyValueRenderer<String>(
                nullString).render(formattedNumber);
    }

}
