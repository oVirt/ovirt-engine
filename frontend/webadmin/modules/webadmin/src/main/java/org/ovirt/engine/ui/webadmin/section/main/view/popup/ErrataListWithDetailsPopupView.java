package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.HasErrata;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataListModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ErrataListWithDetailsPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;

/**
 * View for the popup widget that renders errata (singular: Erratum). The popup is basically a split panel,
 * with a grid of Erratum in the top pane, and the selected Erratum's detail in the bottom pane.
 */
public class ErrataListWithDetailsPopupView extends AbstractModelBoundPopupView<AbstractErrataCountModel> implements ErrataListWithDetailsPopupPresenterWidget.ViewDef {

    private final static int SOUTH_SIZE = 300;

    interface Driver extends SimpleBeanEditorDriver<AbstractErrataCountModel, ErrataListWithDetailsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ErrataListWithDetailsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public interface Style extends CssResource {
        String errataTitleLabel();
        String errataTitlePanel();
        String progressDotsImage();
        String errataDetailFormPanel();
    }

    @UiField
    public Style style;

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    private static FullDateTimeRenderer renderer = new FullDateTimeRenderer(false, false);

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    private FlowPanel errataDetailPanel;

    private Span errataTitle;

    private GeneralFormPanel errataDetailFormPanel;

    private ErrataTableView errataTableView;

    protected final AbstractErrataListModel model;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public ErrataListWithDetailsPopupView(EventBus eventBus, AbstractErrataListModel model) {

        super(eventBus);

        this.model = model;

        errataTableView = new ErrataTableView();
        driver.initialize(this);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void edit(AbstractErrataCountModel transferObj) {
        errataTableView.getErrataTable().setLoadingState(LoadingState.LOADING);

        model.setGuid(transferObj.getGuid());
        errataTableView.init(model);
        HasErrata entity = transferObj.getEntity();
        model.setEntity(entity);
        model.search();

        buildErrataDetailPanel();
        setErrataDetailPanelVisibilty(false);

        errataTableView.edit();
    }

    public void showErrataList() {
        errataTableView.edit();
    }

    public ErrataTableView getErrataTable() {
        return errataTableView;
    }

    private void buildErrataDetailPanel() {
        // TODO may be able to share this config with SubTabEngineErrataDetailsView
        errataDetailPanel = new FlowPanel();
        errataDetailFormPanel = new GeneralFormPanel();
        errataDetailFormPanel.addStyleName(style.errataDetailFormPanel());
        errataTitle = new Span();
        errataTitle.setStyleName(style.errataTitleLabel());
        FlowPanel errataTitlePanel = new FlowPanel();
        errataTitlePanel.setStyleName(style.errataTitlePanel());
        errataTitlePanel.add(errataTitle);
        errataDetailPanel.add(errataTitlePanel);
        errataDetailPanel.add(errataDetailFormPanel);
    }

    public void setErrataDetailPanelVisibilty(boolean visible) {
        splitLayoutPanel.clear();

        if (visible) {
            splitLayoutPanel.addSouth(errataDetailPanel, SOUTH_SIZE);
        }

        splitLayoutPanel.add(errataTableView);
    }

    public void updateErrataDetailFormPanel(Erratum erratum) {
        errataTitle.setText(erratum.getTitle());
        buildErrataDetailForm(errataDetailFormPanel, erratum);
    }

    /**
     * Build the errata details form. public static so the configuration can be shared with ErrataListWithDetailsPopupView.
     */
    public static void buildErrataDetailForm(GeneralFormPanel panel, Erratum erratum) {
        FormBuilder formBuilder = new FormBuilder(panel, 1, 10);
        formBuilder.setRelativeColumnWidth(0, 3);

        formBuilder.addFormItem(new FormItem(constants.errataId(), new Span(erratum.getId()), 0, 0));
        formBuilder.addFormItem(new FormItem(constants.errataDateIssued(), new Span(renderer.render(erratum.getIssued())), 1, 0));
        formBuilder.addFormItem(new FormItem(constants.errataType(), new Span(erratum.getType().getDescription()), 2, 0));
        if (erratum.getSeverity() != null) {
            formBuilder.addFormItem(new FormItem(constants.errataSeverity(), new Span(erratum.getSeverity().getDescription()), 3, 0));
        }
        formBuilder.addFormItem(new FormItem(constants.description(), new Span(erratum.getDescription().toString()), 4, 0));
        formBuilder.addFormItem(new FormItem(constants.solution(), new Span(erratum.getSolution()), 5, 0));
        formBuilder.addFormItem(new FormItem(constants.summary(), new Span(erratum.getSummary()), 6, 0));
        formBuilder.addFormItem(new FormItem(constants.errataPackages(), new Span(StringUtils.join(erratum.getPackages(), ", ")), 7, 0)); //$NON-NLS-1$
    }

    public Erratum getSelectedErratum() {
        return errataTableView.getSelectedErratum();
    }

    @Override
    public AbstractErrataCountModel flush() {
        return null;
    }

}
