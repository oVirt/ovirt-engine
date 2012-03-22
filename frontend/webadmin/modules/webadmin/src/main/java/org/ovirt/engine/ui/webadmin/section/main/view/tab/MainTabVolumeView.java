package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVolumePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabVolumeView extends AbstractMainTabWithDetailsTableView<GlusterVolumeEntity, VolumeListModel> implements MainTabVolumePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabVolumeView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabVolumeView(MainModelProvider<GlusterVolumeEntity, VolumeListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new VolumeStatusColumn(), "", "30px");
        TextColumnWithTooltip<GlusterVolumeEntity> nameColumn = new TextColumnWithTooltip<GlusterVolumeEntity>() {
            @Override
            public String getValue(GlusterVolumeEntity object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<GlusterVolumeEntity> volumeTypeColumn =
                new EnumColumn<GlusterVolumeEntity, GlusterVolumeType>() {

                    @Override
                    protected GlusterVolumeType getRawValue(GlusterVolumeEntity object) {
                        return object.getVolumeType();
                    }
                };
        getTable().addColumn(volumeTypeColumn, "Volume Type");

        TextColumnWithTooltip<GlusterVolumeEntity> numOfBricksColumn =
                new TextColumnWithTooltip<GlusterVolumeEntity>() {
                    @Override
                    public String getValue(GlusterVolumeEntity object) {
                        return Integer.toString(object.getBricks().size());
                    }
                };
        getTable().addColumn(numOfBricksColumn, "Number of Bricks");

        TextColumnWithTooltip<GlusterVolumeEntity> transportColumn =
                new EnumColumn<GlusterVolumeEntity, TransportType>() {

                    @Override
                    protected TransportType getRawValue(GlusterVolumeEntity object) {
                        return object.getTransportType();
                    }

                };
        getTable().addColumn(transportColumn, "Transport Type");

        TextColumnWithTooltip<GlusterVolumeEntity> statusColumn =
                new EnumColumn<GlusterVolumeEntity, GlusterVolumeStatus>() {
                    @Override
                    protected GlusterVolumeStatus getRawValue(GlusterVolumeEntity object) {
                        return object.getStatus();
                    }
                };
        getTable().addColumn(statusColumn, "Status");

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>("Create Volume") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCreateVolumeCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveVolumeCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>("Start") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStartCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>("Stop") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStopCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>("Rebalance") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRebalanceCommand();
            }
        });
    }
}
