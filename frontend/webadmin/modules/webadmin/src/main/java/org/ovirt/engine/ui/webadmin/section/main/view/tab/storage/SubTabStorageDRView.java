package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel.Type;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDRListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDRPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabStorageDRView extends AbstractSubTabTableView<StorageDomain, StorageDomainDR, StorageListModel, StorageDRListModel> implements SubTabStorageDRPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageDRView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface SubTabStorageDRViewUiBinder extends UiBinder<Widget, SubTabStorageDRView> {
    }

    interface Style extends CssResource {
        String filterPanel();
    }

    private static SubTabStorageDRViewUiBinder uiBinder = GWT.create(SubTabStorageDRViewUiBinder.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @UiField
    AlertPanel errorMessagePanel;

    @UiField
    public FlowPanel tablePanel;

    @UiField
    Style style;

    @Inject
    public SubTabStorageDRView(SearchableDetailModelProvider<StorageDomainDR, StorageListModel, StorageDRListModel> modelProvider) {
        super(modelProvider);
        initWidget(uiBinder.createAndBindUi(this));
        errorMessagePanel.setVisible(false);
        errorMessagePanel.setType(Type.WARNING);

        initTable();
        tablePanel.add(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<StorageDomainDR> sessionColumn =
                new AbstractTextColumn<StorageDomainDR>() {
                    @Override
                    public String getValue(StorageDomainDR storageDomainDR) {
                        GlusterGeoRepSession session = getDetailModel().getGeoRepSessionsMap().get(storageDomainDR.getGeoRepSessionId());
                        if (session == null) {
                            return storageDomainDR.getGeoRepSessionId().toString();
                        } else {
                            return messages.geoRepRemoteSessionName(session.getSlaveHostName(),
                                    session.getSlaveVolumeName());
                        }
                    }
                };
        getTable().addColumn(sessionColumn, constants.geoRepSlaveVolume(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomainDR> scheduleColumn =
                new AbstractTextColumn<StorageDomainDR>() {
                    @Override
                    public String getValue(StorageDomainDR storageDomainDR) {
                        StorageSyncSchedule schedule = new StorageSyncSchedule(storageDomainDR.getScheduleCronExpression());
                        return schedule.toString();
                    }
                };
        getTable().addColumn(scheduleColumn, constants.scheduleLabel(), "300px"); //$NON-NLS-1$
    }

    @Override
    public void clearErrorMessage() {
        errorMessagePanel.clearMessages();
        errorMessagePanel.setVisible(false);
        tablePanel.setVisible(true);
    }

    @Override
    public void showErrorMessage(SafeHtml message) {
        tablePanel.setVisible(false);

        errorMessagePanel.clearMessages();
        errorMessagePanel.setVisible(true);
        errorMessagePanel.addMessage(message);
    }

}
