package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer that uses an existing renderer for all non null values but shows text "System default"
 * and provided default value for null.
 *
 */
public class SystemDefaultRenderer<T> extends AbstractDefaultValueRenderer<T> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public SystemDefaultRenderer(AbstractRenderer<T> renderer, T value) {
        super(renderer);
        setDefaultValue(value);
    }

    @Override
    protected String getDefaultValueLabel() {
        return constants.systemDefaultOption();
    }
}
