package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel.Type;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabEngineErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabTableView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.ErrataTableView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabVirtualMachineView.ViewIdHandler;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataFilterPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View for the main tab that contains errata (singular: Erratum) for the engine itself.
 * <p>
 * It is a little different from a typical main tab view -- it supports an AlertPanel for setting
 * a message upon failing to retrieve the errata. (Errata query results rely on integration with a
 * Katello provider, and if the provider has an issue, we want to notify the user right in the tab body.
 */
public class MainTabEngineErrataView extends AbstractMainTabTableView<Erratum,
    EngineErrataListModel> implements MainTabEngineErrataPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabEngineErrataView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface MainTabEngineErrataViewUiBinder extends UiBinder<Widget, MainTabEngineErrataView> {
    }

    interface Style extends CssResource {
        String filterPanel();
    }

    @UiField
    public FlowPanel tablePanel;

    @UiField
    AlertPanel errorMessagePanel;

    @UiField
    Style style;

    protected ErrataFilterPanel errataFilterPanel;

    private static MainTabEngineErrataViewUiBinder uiBinder = GWT.create(MainTabEngineErrataViewUiBinder.class);

    @Inject
    public MainTabEngineErrataView(MainModelProvider<Erratum, EngineErrataListModel> modelProvider) {
        super(modelProvider);

        ViewIdHandler.idHandler.generateAndSetIds(this);

        // configure the table columns -- share config with ErrataTableView
        ErrataTableView.initErrataGrid(getTable());

        initWidget(uiBinder.createAndBindUi(this));
        errorMessagePanel.setVisible(false);
        errorMessagePanel.setType(Type.WARNING);

        initFilterPanel();
        getTable().setTableOverhead(errataFilterPanel);
        getTable().enableColumnResizing();

        tablePanel.add(getTable());
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initFilterPanel() {
        errataFilterPanel = new ErrataFilterPanel();
        errataFilterPanel.init(true, true, true);
        errataFilterPanel.addStyleName(style.filterPanel());
    }

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

    public ErrataFilterPanel getErrataFilterPanel() {
        return errataFilterPanel;
    }
}
