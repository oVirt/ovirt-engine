package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.Translator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiField;

public class SubTabVolumeGeneralView extends AbstractSubTabFormView<GlusterVolumeEntity, VolumeListModel, VolumeGeneralModel> implements SubTabVolumeGeneralPresenter.ViewDef, Editor<VolumeGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<VolumeGeneralModel, SubTabVolumeGeneralView> {
    }

    // We need this in order to find the icon for alert messages:
    private final ApplicationResources resources;

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel volumeType = new TextBoxLabel();
    TextBoxLabel replicaCount = new TextBoxLabel();
    TextBoxLabel stripeCount = new TextBoxLabel();
    TextBoxLabel numOfBricks = new TextBoxLabel();

    FormBuilder formBuilder;

    FormItem replicaFormItem;
    FormItem stripeFormItem;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public SubTabVolumeGeneralView(DetailModelProvider<VolumeListModel, VolumeGeneralModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);

        // Inject a reference to the resources:
        resources = ClientGinjectorProvider.instance().getApplicationResources();

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(formPanel);
        driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 4);
        formBuilder.addFormItem(new FormItem(constants.NameVolume(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.volumeTypeVolume(), volumeType, 1, 0));

        replicaFormItem = new FormItem(constants.replicaCountVolume(), replicaCount, 2, 0);
        formBuilder.addFormItem(replicaFormItem);

        stripeFormItem = new FormItem(constants.stripeCountVolume(), stripeCount, 2, 0);
        formBuilder.addFormItem(stripeFormItem);

        formBuilder.addFormItem(new FormItem(constants.numberOfBricksVolume(), numOfBricks, 3, 0));

        getDetailModel().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                VolumeGeneralModel model = (VolumeGeneralModel) sender;
                if ("VolumeType".equals(((PropertyChangedEventArgs) args).PropertyName)) { //$NON-NLS-1$
                    translateVolumeType((GlusterVolumeEntity) model.getEntity());
                }
            }
        });
    }

    @Override
    public void setMainTabSelectedItem(GlusterVolumeEntity selectedItem) {
        driver.edit(getDetailModel());

        if (selectedItem.getVolumeType() == GlusterVolumeType.REPLICATE
                || selectedItem.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE)
        {
            replicaFormItem.setIsAvailable(true);
            stripeFormItem.setIsAvailable(false);
        }
        else if (selectedItem.getVolumeType() == GlusterVolumeType.STRIPE
                || selectedItem.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE)
        {
            replicaFormItem.setIsAvailable(false);
            stripeFormItem.setIsAvailable(true);
        }
        else
        {
            replicaFormItem.setIsAvailable(false);
            stripeFormItem.setIsAvailable(false);
        }

        formBuilder.showForm(getDetailModel());
    }

    private void translateVolumeType(GlusterVolumeEntity volumeEntity) {
        Translator translator = EnumTranslator.Create(GlusterVolumeType.class);
        if (translator.containsKey(volumeEntity.getVolumeType()))
        {
            getDetailModel().setVolumeTypeSilently(translator.get(volumeEntity.getVolumeType()));
        }
    }

}
