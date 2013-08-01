package org.ovirt.engine.ui.uicommonweb.models.datacenters;


import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public abstract class NetworkQoSModel extends Model {


    private final ListModel sourceListModel;
    private ListModel dataCenters;

    private EntityModel name;
    private EntityModel inboundAverage;
    private EntityModel inboundPeak;
    private EntityModel inboundBurst;
    private EntityModel outboundAverage;
    private EntityModel outboundPeak;
    private EntityModel outboundBurst;
    private EntityModel inboundEnabled;
    private EntityModel outboundEnabled;

    protected NetworkQoS networkQoS = new NetworkQoS();

    public NetworkQoSModel(DataCenterNetworkQoSListModel sourceListModel) {
        this.sourceListModel = sourceListModel;
        setName(new EntityModel());
        setDataCenters(new ListModel());
        getDataCenters().setSelectedItem(sourceListModel.getDataCenter());
        getDataCenters().setIsChangable(false);
        setInboundAverage(new EntityModel(AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSInboundAverageDefaultValue)));
        setInboundPeak(new EntityModel(AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSInboundPeakDefaultValue)));
        setInboundBurst(new EntityModel(AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSInboundBurstDefaultValue)));
        setOutboundAverage(new EntityModel(AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSOutboundAverageDefaultValue)));
        setOutboundPeak(new EntityModel(AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSOutboundPeakDefaultValue)));
        setOutboundBurst(new EntityModel(AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSOutboundBurstDefaultValue)));
        addCommands();
        setInboundEnabled(new EntityModel(Boolean.TRUE));
        setOutboundEnabled(new EntityModel(Boolean.TRUE));
        getInboundEnabled().getEntityChangedEvent().addListener(this);
        getOutboundEnabled().getEntityChangedEvent().addListener(this);
    }

    public ListModel getSourceListModel() {
        return sourceListModel;
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        getInboundAverage().validateEntity(new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0,
                (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MaxAverageNetworkQoSValue))});
        getInboundPeak().validateEntity(new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0,
                (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MaxPeakNetworkQoSValue))});
        getInboundBurst().validateEntity(new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0,
                (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MaxBurstNetworkQoSValue))});
        getOutboundAverage().validateEntity(new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0,
                (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MaxAverageNetworkQoSValue))});
        getOutboundPeak().validateEntity(new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0,
                (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MaxPeakNetworkQoSValue))});
        getOutboundBurst().validateEntity(new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0,
                (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MaxBurstNetworkQoSValue))});

        boolean inboundDisabled = Boolean.FALSE.equals(getInboundEnabled().getEntity());
        boolean outboundDisabled = Boolean.FALSE.equals(getOutboundEnabled().getEntity());

        return getName().getIsValid()
                && (inboundDisabled
                || (getInboundAverage().getIsValid()
                && getInboundPeak().getIsValid()
                && getInboundBurst().getIsValid()))
                && (outboundDisabled ||
                (getOutboundAverage().getIsValid()
                && getOutboundPeak().getIsValid()
                && getOutboundBurst().getIsValid()));
    }


    protected void addCommands() {
        UICommand tempVar2 = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar2.setIsDefault(true);
        getCommands().add(tempVar2);
        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar3.setIsCancel(true);
        getCommands().add(tempVar3);
    }

    public StoragePool getSelectedDc() {
        return (StoragePool) getDataCenters().getSelectedItem();
    }

    public void flush() {
        networkQoS.setName((String) getName().getEntity());
        networkQoS.setStoragePoolId(((StoragePool)getDataCenters().getSelectedItem()).getId());

        boolean inboundEnabled = Boolean.TRUE.equals(getInboundEnabled().getEntity());
        boolean outboundEnabled = Boolean.TRUE.equals(getOutboundEnabled().getEntity());

        if (inboundEnabled) {
            networkQoS.setInboundAverage(Integer.parseInt(getInboundAverage().getEntity().toString()));
            networkQoS.setInboundPeak(Integer.parseInt(getInboundPeak().getEntity().toString()));
            networkQoS.setInboundBurst(Integer.parseInt( getInboundBurst().getEntity().toString()));
        } else {
            networkQoS.setInboundAverage(null);
            networkQoS.setInboundPeak(null);
            networkQoS.setInboundBurst(null);
        }

        if (outboundEnabled) {
            networkQoS.setOutboundAverage(Integer.parseInt(getOutboundAverage().getEntity().toString()));
            networkQoS.setOutboundPeak(Integer.parseInt(getOutboundPeak().getEntity().toString()));
            networkQoS.setOutboundBurst(Integer.parseInt(getOutboundBurst().getEntity().toString()));
        } else {
            networkQoS.setOutboundAverage(null);
            networkQoS.setOutboundPeak(null);
            networkQoS.setOutboundBurst(null);
        }
    }

    protected abstract void executeSave();

    private void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    public void onSave() {
        if (!validate()) {
            return;
        }

        // Save changes.
        flush();

        // Execute all the required commands (detach, attach, update) to save the updates
        executeSave();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        } else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (getInboundEnabled().equals(sender)) {
            updateInboundAvailability();
        } else if (getOutboundEnabled().equals(sender)) {
            updateOutboundAvailability();
        }
    }

    private void updateOutboundAvailability() {
        boolean enabled = Boolean.TRUE.equals(getOutboundEnabled().getEntity());
        getOutboundAverage().setIsChangable(enabled);
        getOutboundPeak().setIsChangable(enabled);
        getOutboundBurst().setIsChangable(enabled);
    }

    private void updateInboundAvailability() {
        boolean enabled = Boolean.TRUE.equals(getInboundEnabled().getEntity());
        getInboundAverage().setIsChangable(enabled);
        getInboundPeak().setIsChangable(enabled);
        getInboundBurst().setIsChangable(enabled);
    }

    protected void postSaveAction(boolean succeeded) {
        if (succeeded) {
            cancel();
        }
        stopProgress();
    }

    public ListModel getDataCenters() {
        return dataCenters;
    }

    public void setDataCenters(ListModel dataCenters) {
        this.dataCenters = dataCenters;
    }

    public EntityModel getName() {
        return name;
    }

    public void setName(EntityModel name) {
        this.name = name;
    }

    public EntityModel getInboundAverage() {
        return inboundAverage;
    }

    public void setInboundAverage(EntityModel inboundAverage) {
        this.inboundAverage = inboundAverage;
    }

    public EntityModel getInboundPeak() {
        return inboundPeak;
    }

    public void setInboundPeak(EntityModel inboundPeak) {
        this.inboundPeak = inboundPeak;
    }

    public EntityModel getInboundBurst() {
        return inboundBurst;
    }

    public void setInboundBurst(EntityModel inboundBurst) {
        this.inboundBurst = inboundBurst;
    }

    public EntityModel getOutboundAverage() {
        return outboundAverage;
    }

    public void setOutboundAverage(EntityModel outboundAverage) {
        this.outboundAverage = outboundAverage;
    }

    public EntityModel getOutboundPeak() {
        return outboundPeak;
    }

    public void setOutboundPeak(EntityModel outboundPeak) {
        this.outboundPeak = outboundPeak;
    }

    public EntityModel getOutboundBurst() {
        return outboundBurst;
    }

    public void setOutboundBurst(EntityModel outboundBurst) {
        this.outboundBurst = outboundBurst;
    }

    public EntityModel getInboundEnabled() {
        return inboundEnabled;
    }

    public void setInboundEnabled(EntityModel inboundEnabled) {
        this.inboundEnabled = inboundEnabled;
    }

    public EntityModel getOutboundEnabled() {
        return outboundEnabled;
    }

    public void setOutboundEnabled(EntityModel outboundEnabled) {
        this.outboundEnabled = outboundEnabled;
    }
}
