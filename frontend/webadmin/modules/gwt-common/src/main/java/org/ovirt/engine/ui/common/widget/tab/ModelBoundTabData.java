package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;

import com.google.gwt.dom.client.Style.HasCssName;

/**
 * Implementation of {@link com.gwtplatform.mvp.client.TabData TabData} interface for use with UiCommon models bound to
 * tab widgets.
 */
public class ModelBoundTabData extends GroupedTabData {

    private final ModelProvider<? extends HasEntity> modelProvider;

    public ModelBoundTabData(String label, int priority,
            ModelProvider<? extends HasEntity> modelProvider) {
        this(label, priority, null, -1, modelProvider, null);
    }

    public ModelBoundTabData(String label, int priority, String groupTitle, int groupPriority,
            ModelProvider<? extends HasEntity> modelProvider, HasCssName icon) {
        super(label, groupTitle, priority, groupPriority, icon);
        this.modelProvider = modelProvider;
    }

    public ModelProvider<? extends HasEntity> getModelProvider() {
        return modelProvider;
    }

}
