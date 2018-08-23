package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.label.VolumeTransportTypeLabel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.label.DetailsTextBoxLabel;
import org.ovirt.engine.ui.webadmin.widget.label.VolumeCapacityLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueLabel;

public class SubTabVolumeGeneralView extends AbstractSubTabFormView<GlusterVolumeEntity, VolumeListModel, VolumeGeneralModel> implements SubTabVolumeGeneralPresenter.ViewDef, Editor<VolumeGeneralModel> {

    interface Driver extends UiCommonEditorDriver<VolumeGeneralModel, SubTabVolumeGeneralView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabVolumeGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    StringValueLabel name = new StringValueLabel();
    StringValueLabel volumeId = new StringValueLabel();
    StringValueLabel volumeType = new StringValueLabel();
    StringValueLabel replicaCount = new StringValueLabel();
    StringValueLabel stripeCount = new StringValueLabel();
    StringValueLabel disperseCount = new StringValueLabel();
    StringValueLabel redundancyCount = new StringValueLabel();
    StringValueLabel numOfBricks = new StringValueLabel();
    VolumeTransportTypeLabel transportTypes = new VolumeTransportTypeLabel();
    StringValueLabel snapMaxLimit = new StringValueLabel();

    VolumeCapacityLabel<Long> volumeTotalCapacity;
    VolumeCapacityLabel<Long> volumeUsedCapacity;
    VolumeCapacityLabel<Long> volumeFreeCapacity;
    VolumeCapacityLabel<Long> volumeConfirmedFreeCapacity;
    ValueLabel<Long> volumeVdoSavings;

    FormBuilder formBuilder;

    FormItem replicaFormItem;
    FormItem stripeFormItem;
    FormItem disperseCountFormItem;
    FormItem redundancyCountFormItem;

    @Ignore
    DetailsTextBoxLabel<ArrayList<ValueLabel<Long>>, Long> volumeCapacityDetailsLabel = new DetailsTextBoxLabel<>(constants.total(), constants.used(), constants.free(), constants.confirmedFree(), constants.vdoSavingsBrickAdvancedLabel());

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabVolumeGeneralView(DetailModelProvider<VolumeListModel, VolumeGeneralModel> modelProvider) {
        super(modelProvider);

        initCapacityLabel();

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(formPanel);
        driver.initialize(this);

        generateIds();

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 11);

        formBuilder.addFormItem(new FormItem(constants.nameVolume(), name, 0, 0));

        formBuilder.addFormItem(new FormItem(constants.volumeIdVolume(), volumeId, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.volumeTypeVolume(), volumeType, 2, 0));

        replicaFormItem = new FormItem(constants.replicaCountVolume(), replicaCount, 3, 0);
        formBuilder.addFormItem(replicaFormItem);

        stripeFormItem = new FormItem(constants.stripeCountVolume(), stripeCount, 4, 0);
        formBuilder.addFormItem(stripeFormItem);

        formBuilder.addFormItem(new FormItem(constants.numberOfBricksVolume(), numOfBricks, 5, 0));
        formBuilder.addFormItem(new FormItem(constants.transportTypesVolume(), transportTypes, 6, 0));

        formBuilder.addFormItem(new FormItem(constants.maxNumberOfSnapshotsVolume(), snapMaxLimit, 7, 0));

        disperseCountFormItem = new FormItem(constants.disperseCount(), disperseCount, 8, 0);
        formBuilder.addFormItem(disperseCountFormItem);

        redundancyCountFormItem = new FormItem(constants.redundancyCount(), redundancyCount, 9, 0);
        formBuilder.addFormItem(redundancyCountFormItem);

        volumeCapacityDetailsLabel.setWidth("275px");//$NON-NLS-1$
        formBuilder.addFormItem(new FormItem(constants.volumeCapacityStatistics(), volumeCapacityDetailsLabel, 10, 0));

        getDetailModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            VolumeGeneralModel model = (VolumeGeneralModel) sender;
            if ("VolumeType".equals(args.propertyName)) { //$NON-NLS-1$
                translateVolumeType(model.getEntity());
            }
        });
        formBuilder.setRelativeColumnWidth(0, 4);
    }

    private void initCapacityLabel() {
        this.volumeTotalCapacity = new VolumeCapacityLabel<>(constants);
        this.volumeFreeCapacity = new VolumeCapacityLabel<>(constants);
        this.volumeUsedCapacity = new VolumeCapacityLabel<>(constants);
        this.volumeConfirmedFreeCapacity = new VolumeCapacityLabel<>(constants);
        this.volumeVdoSavings = new ValueLabel<>();
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(GlusterVolumeEntity selectedItem) {
        driver.edit(getDetailModel());

        replicaFormItem.setIsAvailable(selectedItem.getVolumeType().isReplicatedType());
        stripeFormItem.setIsAvailable(selectedItem.getVolumeType().isStripedType());
        disperseCountFormItem.setIsAvailable(selectedItem.getVolumeType().isDispersedType());
        redundancyCountFormItem.setIsAvailable(selectedItem.getVolumeType().isDispersedType());

        ArrayList<ValueLabel<Long>> volumeCapacityDetails =
                new ArrayList<>(Arrays.<ValueLabel<Long>>asList(volumeTotalCapacity, volumeUsedCapacity, volumeFreeCapacity));
        if (selectedItem.getAdvancedDetails().getCapacityInfo().getConfirmedFreeSize() != null) {
            volumeCapacityDetails.add(volumeConfirmedFreeCapacity);
        }
        if (selectedItem.getAdvancedDetails().getCapacityInfo().getVdoSavings() != null) {
            volumeCapacityDetails.add(volumeVdoSavings);
        }
        volumeCapacityDetailsLabel.setValue(volumeCapacityDetails);

        formBuilder.update(getDetailModel());
    }

    private void translateVolumeType(GlusterVolumeEntity volumeEntity) {
        EnumTranslator translator = EnumTranslator.getInstance();
        if (translator.containsKey(volumeEntity.getVolumeType())) {
            String volumeType = translator.translate(volumeEntity.getVolumeType());
            if (volumeEntity.getIsArbiter()) {
                volumeType += " (" + ConstantsManager.getInstance().getConstants().arbiter() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            getDetailModel().setVolumeTypeSilently(volumeType);
        }
    }

}
