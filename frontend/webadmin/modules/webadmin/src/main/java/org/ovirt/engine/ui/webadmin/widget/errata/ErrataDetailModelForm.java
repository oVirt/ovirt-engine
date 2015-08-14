package org.ovirt.engine.ui.webadmin.widget.errata;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.EnumTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;
import org.ovirt.engine.ui.webadmin.widget.label.TextBoxListLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class ErrataDetailModelForm extends AbstractModelBoundFormWidget<EntityModel<Erratum>> {
    private final static ApplicationConstants constants = AssetProvider.getConstants();

    interface Driver extends SimpleBeanEditorDriver<EntityModel<Erratum>, ErrataDetailModelForm> {
    }

    @Path("entity.id")
    TextBoxLabel id = new TextBoxLabel();
    @Path("entity.issued")
    FullDateTimeLabel issued = new FullDateTimeLabel(false);
    @Path("entity.type")
    EnumTextBoxLabel<ErrataType> type = new EnumTextBoxLabel<>();
    @Path("entity.severity")
    EnumTextBoxLabel<ErrataSeverity> severity = new EnumTextBoxLabel<>();
    @Path("entity.description")
    TextBoxLabel description = new TextBoxLabel();
    @Path("entity.solution")
    TextBoxLabel solution = new TextBoxLabel();
    @Path("entity.summary")
    TextBoxLabel summary = new TextBoxLabel();
    @Path("entity.packages")
    TextBoxListLabel<String> packages = new TextBoxListLabel<String>(", "); //$NON-NLS-1$
    private final Driver driver = GWT.create(Driver.class);

    private EntityModel<Erratum> currentModel;

    public ErrataDetailModelForm() {
        super(null, 1, 8); //1 column with 8 rows
    }

    public void initialize() {
        driver.initialize(this);
        formBuilder.addFormItem(new FormItem(constants.errataId(), id, 0, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.errataDateIssued(), issued, 1, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.errataType(), type, 2, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.errataSeverity(), severity, 3, 0) {
            @Override
            public boolean getIsAvailable() {
                EntityModel<Erratum> model = getModel();
                return model != null && model.getEntity() != null && model.getEntity().getSeverity() != null;
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.description(), description, 4, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.solution(), solution, 5, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.summary(), summary, 6, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.errataPackages(), packages, 7, 0), 2, 10);
    }

    @Override
    protected void doEdit(EntityModel<Erratum> model) {
        driver.edit(model);
    }

    @Override
    protected EntityModel<Erratum> getModel() {
        return currentModel;
    }

    public void setModel(EntityModel<Erratum> model) {
        currentModel = model;
    }
}
