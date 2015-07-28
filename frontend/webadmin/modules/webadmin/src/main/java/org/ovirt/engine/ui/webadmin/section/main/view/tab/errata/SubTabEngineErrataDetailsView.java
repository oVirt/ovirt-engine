package org.ovirt.engine.ui.webadmin.section.main.view.tab.errata;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.ErratumModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.errata.SubTabEngineErrataDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.ErrataListWithDetailsPopupView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * View for the sub tab that shows details for an engine Erratum selected in the main tab.
 */
public class SubTabEngineErrataDetailsView extends AbstractSubTabFormView<Erratum, EngineErrataListModel, ErratumModel>
        implements SubTabEngineErrataDetailsPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabEngineErrataDetailsView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, SubTabEngineErrataDetailsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public interface Style extends CssResource {
        String errataTitleLabel();
        String errataTitlePanel();
    }

    private GeneralFormPanel errataDetailFormPanel;
    private Span errataTitle;

    @UiField
    FlowPanel errataDetailPanel;

    @UiField
    public Style style;

    @Inject
    public SubTabEngineErrataDetailsView(DetailTabModelProvider<EngineErrataListModel, ErratumModel> modelProvider) {
        super(modelProvider);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        generateIds();
    }

    private void buildErrataDetailPanel() {
        errataDetailPanel.clear();
        errataDetailFormPanel = new GeneralFormPanel();
        errataTitle = new Span();
        errataTitle.setStyleName(style.errataTitleLabel());
        FlowPanel errataTitlePanel = new FlowPanel();
        errataTitlePanel.setStyleName(style.errataTitlePanel());
        errataTitlePanel.add(errataTitle);
        errataDetailPanel.add(errataTitlePanel);
        errataDetailPanel.add(errataDetailFormPanel);
    }

    public void updateErrataDetailFormPanel(Erratum erratum) {
        buildErrataDetailPanel();
        errataTitle.setText(erratum.getTitle());
        // share the panel configuration with ErrataListWithDetailsPopupView
        ErrataListWithDetailsPopupView.buildErrataDetailForm(errataDetailFormPanel, erratum);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(Erratum selectedItem) {
        updateErrataDetailFormPanel(selectedItem);
    }

}
