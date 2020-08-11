package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer that uses an existing renderer for all non null values but shows text "Cluster default"
 * and provided default value for null.
 *
 */
public class ClusterDefaultRenderer<T> extends AbstractDefaultValueRenderer<T> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public ClusterDefaultRenderer(AbstractRenderer<T> renderer) {
        super(renderer);
    }

    public ClusterDefaultRenderer(AbstractRenderer<T> renderer, T defaultValuePlaceholder) {
        super(renderer, defaultValuePlaceholder);
    }

    @Override
    protected String getDefaultValueLabel() {
        return constants.clusterDefaultOption();
    }
}
