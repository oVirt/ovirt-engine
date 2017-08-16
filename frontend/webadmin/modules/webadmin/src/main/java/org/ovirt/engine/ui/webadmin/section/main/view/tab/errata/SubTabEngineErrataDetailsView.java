package org.ovirt.engine.ui.webadmin.section.main.view.tab.errata;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.errata.SubTabEngineErrataDetailsPresenter;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataDetailModelForm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * View for the sub tab that shows details for an engine Erratum selected in the main tab.
 */
public class SubTabEngineErrataDetailsView extends AbstractSubTabFormView<Erratum, EngineErrataListModel,
        EntityModel<Erratum>> implements SubTabEngineErrataDetailsPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabEngineErrataDetailsView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, SubTabEngineErrataDetailsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField (provided = true)
    ErrataDetailModelForm errataDetailModelForm;

    @UiField
    HTMLPanel errataTitle;

    @UiField
    FlowPanel errataDetailPanel;

    public SubTabEngineErrataDetailsView() {
        //Don't care about the provider, as its not used.
        super(null);
        errataDetailModelForm = new ErrataDetailModelForm();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        generateIds();
        errataDetailModelForm.initialize();
    }

    public void updateErrataDetailFormPanel(Erratum erratum) {
        errataTitle.clear();
        errataTitle.add(new HTML(erratum.getTitle()));
        errataDetailModelForm.setModel(new EntityModel<>(erratum));
        errataDetailModelForm.update();
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(Erratum selectedItem) {
        updateErrataDetailFormPanel(selectedItem);
    }

}
