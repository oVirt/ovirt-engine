package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.client.NumberFormatRenderer;

public class NullableNumberRenderer extends NumberFormatRenderer {

    public NullableNumberRenderer() {
        super();
    }

    public NullableNumberRenderer(NumberFormat format) {
        super(format);
    }

    @Override
    public String render(Number object) {
        String formattedNumber = super.render(object);
        return new EmptyValueRenderer<String>(
                ClientGinjectorProvider.instance().getApplicationConstants().unAvailablePropertyLabel()).render(formattedNumber);
    }

}
