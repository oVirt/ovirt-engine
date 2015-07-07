package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import com.gwtplatform.mvp.client.TabDataBasic;

/**
 * Implementation of {@link com.gwtplatform.mvp.client.TabData TabData} interface for use with UiCommon models bound to
 * tab widgets.
 */
public class ModelBoundTabData extends TabDataBasic {

    private final ModelProvider<? extends HasEntity> modelProvider;
    private final Align align;

    public ModelBoundTabData(String label, float priority,
            ModelProvider<? extends HasEntity> modelProvider) {
        this(label, priority, modelProvider, Align.LEFT);
    }

    public ModelBoundTabData(String label, float priority,
            ModelProvider<? extends HasEntity> modelProvider,
            Align align) {
        super(label, priority);
        this.modelProvider = modelProvider;
        this.align = align;
    }

    public ModelProvider<? extends HasEntity> getModelProvider() {
        return modelProvider;
    }

    public Align getAlign() {
        return align;
    }

}
