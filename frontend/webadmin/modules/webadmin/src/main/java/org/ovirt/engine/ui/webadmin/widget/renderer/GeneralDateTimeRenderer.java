package org.ovirt.engine.ui.webadmin.widget.renderer;

import java.util.Date;

import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Date values using {@code GeneralDateTimeFormat}.
 */
public class GeneralDateTimeRenderer extends AbstractRenderer<Date> {

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public String render(Date object) {
        if (object == null) {
            return constants.notAvailableLabel();
        }
        return FullDateTimeRenderer.getLocalizedDateTimeFormat().format(object);
    }

}
