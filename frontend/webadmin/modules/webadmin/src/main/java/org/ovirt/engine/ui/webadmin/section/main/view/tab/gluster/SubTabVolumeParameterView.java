package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOption;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeParameterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.inject.Inject;

public class SubTabVolumeParameterView extends AbstractSubTabTableView<GlusterVolumeEntity, GlusterVolumeOption, VolumeListModel, VolumeParameterListModel> implements SubTabVolumeParameterPresenter.ViewDef {

    @Inject
    public SubTabVolumeParameterView(SearchableDetailModelProvider<GlusterVolumeOption, VolumeListModel, VolumeParameterListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<GlusterVolumeOption> optionKeyColumn = new TextColumnWithTooltip<GlusterVolumeOption>() {
            @Override
            public String getValue(GlusterVolumeOption option) {
                return option.getKey();
            }
        };
        getTable().addColumn(optionKeyColumn, "Option Key");
        TextColumnWithTooltip<GlusterVolumeOption> optionValueColumn =
                new TextColumnWithTooltip<GlusterVolumeOption>() {
                    @Override
                    public String getValue(GlusterVolumeOption option) {
                        return option.getValue();
                    }
                };
        getTable().addColumn(optionValueColumn, "Option Value");
    }

}
