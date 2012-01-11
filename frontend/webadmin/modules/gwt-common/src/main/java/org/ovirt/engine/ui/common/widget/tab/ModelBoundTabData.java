package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.gwtplatform.mvp.client.TabDataBasic;

/**
 * Implementation of {@link com.gwtplatform.mvp.client.TabData TabData} interface for use with UiCommon models bound to
 * tab widgets.
 */
public class ModelBoundTabData extends TabDataBasic {

    private final ModelProvider<? extends EntityModel> modelProvider;
    private final Align align;

    public ModelBoundTabData(String label, float priority,
            ModelProvider<? extends EntityModel> modelProvider) {
        this(label, priority, modelProvider, Align.LEFT);
    }

    public ModelBoundTabData(String label, float priority,
            ModelProvider<? extends EntityModel> modelProvider,
            Align align) {
        super(label, priority);
        this.modelProvider = modelProvider;
        this.align = align;
    }

    public ModelProvider<? extends EntityModel> getModelProvider() {
        return modelProvider;
    }

    public Align getAlign() {
        return align;
    }

}
