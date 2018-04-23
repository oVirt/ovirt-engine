package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VolumeParameterListModel extends SearchableListModel<GlusterVolumeEntity, GlusterVolumeOptionEntity> {

    private UICommand addParameterCommand;
    private UICommand editParameterCommand;
    private UICommand resetParameterCommand;
    private UICommand resetAllParameterCommand;

    public UICommand getAddParameterCommand() {
        return addParameterCommand;
    }

    public void setAddParameterCommand(UICommand command) {
        this.addParameterCommand = command;
    }

    public UICommand getEditParameterCommand() {
        return editParameterCommand;
    }

    public void setEditParameterCommand(UICommand command) {
        this.editParameterCommand = command;
    }

    public void setResetParameterCommand(UICommand command) {
        this.resetParameterCommand = command;
    }

    public UICommand getResetParameterCommand() {
        return resetParameterCommand;
    }

    public void setResetAllParameterCommand(UICommand command) {
        this.resetAllParameterCommand = command;
    }

    public UICommand getResetAllParameterCommand() {
        return resetAllParameterCommand;
    }

    @Override
    protected String getListName() {
        // TODO Auto-generated method stub
        return "VolumeParameterListModel"; //$NON-NLS-1$
    }

    public VolumeParameterListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().parameterTitle());
        setHelpTag(HelpTag.parameters);
        setHashName("parameters"); //$NON-NLS-1$
        setAddParameterCommand(new UICommand(ConstantsManager.getInstance().getConstants().addVolume(), this));
        setEditParameterCommand(new UICommand(ConstantsManager.getInstance().getConstants().editVolume(), this));
        setResetParameterCommand(new UICommand(ConstantsManager.getInstance().getConstants().resetVolume(), this));
        setResetAllParameterCommand(new UICommand(ConstantsManager.getInstance().getConstants().resetAllVolume(), this));
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getEditParameterCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
        getResetParameterCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
        getResetAllParameterCommand().setIsExecutionAllowed(getItems() != null && getItems().size() > 0);
    }

    private void addParameter() {
        if (getWindow() != null) {
            return;
        }

        GlusterVolumeEntity volume = getEntity();
        if (volume == null) {
            return;
        }

        VolumeParameterModel volumeParameterModel = new VolumeParameterModel();
        volumeParameterModel.setTitle(ConstantsManager.getInstance().getConstants().addOptionVolume());
        volumeParameterModel.setHelpTag(HelpTag.add_option);
        volumeParameterModel.setHashName("add_option"); //$NON-NLS-1$
        setWindow(volumeParameterModel);
        volumeParameterModel.startProgress();

        AsyncQuery<QueryReturnValue> asyncQuery = new AsyncQuery<>(returnValue -> {
            VolumeParameterModel innerParameterModel = (VolumeParameterModel) getWindow();
            if (!returnValue.getSucceeded()) {
                innerParameterModel.setOptionsMap(new HashMap<String, GlusterVolumeOptionInfo>());
                innerParameterModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .errorInFetchingVolumeOptionList());
            } else {
                innerParameterModel.setOptionsMap(getOptionsMap(returnValue.getReturnValue()));
            }

            innerParameterModel.stopProgress();

            UICommand command = UICommand.createDefaultOkUiCommand("OnSetParameter", VolumeParameterListModel.this); //$NON-NLS-1$
            innerParameterModel.getCommands().add(command);
            innerParameterModel.getCommands().add(UICommand.createCancelUiCommand("OnCancel", VolumeParameterListModel.this)); //$NON-NLS-1$
        });
        asyncQuery.setHandleFailure(true);
        AsyncDataProvider.getInstance().getGlusterVolumeOptionInfoList(asyncQuery, volume.getClusterId());
    }

    private Map<String, GlusterVolumeOptionInfo> getOptionsMap(Set<GlusterVolumeOptionInfo> optionList) {
        Map<String, GlusterVolumeOptionInfo> optionsMap = new HashMap<>();
        for (GlusterVolumeOptionInfo volumeOption : optionList) {
            optionsMap.put(volumeOption.getKey(), volumeOption);
        }
        GlusterVolumeOptionInfo cifsVolumeOption = getCifsVolumeOption();
        optionsMap.put(cifsVolumeOption.getKey(), cifsVolumeOption);
        return optionsMap;
    }

    private void onSetParameter() {
        if (getEntity() == null) {
            return;
        }

        GlusterVolumeEntity volume = getEntity();

        VolumeParameterModel model = (VolumeParameterModel) getWindow();

        if (!model.validate()) {
            return;
        }

        GlusterVolumeOptionEntity option = new GlusterVolumeOptionEntity();
        option.setVolumeId(volume.getId());
        option.setKey(model.getKeyList().getSelectedItem());
        option.setValue(model.getValue().getEntity());

        model.startProgress();

        Frontend.getInstance().runAction(ActionType.SetGlusterVolumeOption,
                new GlusterVolumeOptionParameters(option),
                result -> {
                    VolumeParameterListModel localModel = (VolumeParameterListModel) result.getState();
                    localModel.postOnSetParameter(result.getReturnValue());
                }, this);
    }

    public void postOnSetParameter(ActionReturnValue returnValue) {
        VolumeParameterModel model = (VolumeParameterModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
        }
    }

    private void cancel() {
        setWindow(null);
    }

    private void editParameter() {
        if (getWindow() != null) {
            return;
        }

        GlusterVolumeEntity volume = getEntity();
        if (volume == null) {
            return;
        }

        VolumeParameterModel volumeParameterModel = new VolumeParameterModel();
        volumeParameterModel.setTitle(ConstantsManager.getInstance().getConstants().editOptionVolume());
        volumeParameterModel.setHelpTag(HelpTag.edit_option);
        volumeParameterModel.setHashName("edit_option"); //$NON-NLS-1$
        volumeParameterModel.setIsNew(false);
        setWindow(volumeParameterModel);

        volumeParameterModel.getKeyList().setIsChangeable(false);
        volumeParameterModel.startProgress();

        AsyncQuery<QueryReturnValue> asyncQuery = new AsyncQuery<>(returnValue -> {
            VolumeParameterModel innerParameterModel = (VolumeParameterModel) getWindow();

            if (!returnValue.getSucceeded()) {
                innerParameterModel.setOptionsMap(new HashMap<String, GlusterVolumeOptionInfo>());
                innerParameterModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .errorInFetchingVolumeOptionList());
            } else {
                innerParameterModel.setOptionsMap(getOptionsMap(returnValue.getReturnValue()));
            }

            GlusterVolumeOptionEntity selectedOption = getSelectedItem();
            innerParameterModel.getDescription().setEntity(""); //$NON-NLS-1$
            innerParameterModel.getKeyList().setSelectedItem(selectedOption.getKey());
            innerParameterModel.getValue().setEntity(selectedOption.getValue());

            innerParameterModel.stopProgress();

            UICommand command = UICommand.createDefaultOkUiCommand("OnSetParameter", VolumeParameterListModel.this); //$NON-NLS-1$
            innerParameterModel.getCommands().add(command);
            command = UICommand.createCancelUiCommand("OnCancel", VolumeParameterListModel.this); //$NON-NLS-1$
            innerParameterModel.getCommands().add(command);
        });
        asyncQuery.setHandleFailure(true);
        AsyncDataProvider.getInstance().getGlusterVolumeOptionInfoList(asyncQuery, volume.getClusterId());
    }

    private GlusterVolumeOptionInfo getCifsVolumeOption() {
        GlusterVolumeOptionInfo cifsOption = new GlusterVolumeOptionInfo();
        cifsOption.setKey("user.cifs"); //$NON-NLS-1$
        return cifsOption;
    }

    private void resetParameter() {
        if (getWindow() != null) {
            return;
        }

        if (getSelectedItem() == null) {
            return;
        }
        GlusterVolumeOptionEntity selectedOption = getSelectedItem();

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().resetOptionVolumeTitle());
        model.setHelpTag(HelpTag.reset_option);
        model.setHashName("reset_option"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().resetOptionVolumeMsg());

        ArrayList<String> list = new ArrayList<>();
        list.add(selectedOption.getKey());
        model.setItems(list);

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnResetParameter", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("OnCancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onResetParameter() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (getSelectedItem() == null) {
            return;
        }
        GlusterVolumeOptionEntity selectedOption = getSelectedItem();

        ResetGlusterVolumeOptionsParameters parameters =
                new ResetGlusterVolumeOptionsParameters(selectedOption.getVolumeId(), selectedOption, false);

        model.startProgress();

        Frontend.getInstance().runAction(ActionType.ResetGlusterVolumeOptions,
                parameters,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    private void resetAllParameters() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().resetAllOptionsTitle());
        model.setHelpTag(HelpTag.reset_all_options);
        model.setHashName("reset_all_options"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().resetAllOptionsMsg());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnResetAllParameters", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("OnCancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onResetAllParameters() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (getEntity() == null) {
            return;
        }
        GlusterVolumeEntity volume = getEntity();

        ResetGlusterVolumeOptionsParameters parameters =
                new ResetGlusterVolumeOptionsParameters(volume.getId(), null, false);

        model.startProgress();

        Frontend.getInstance().runAction(ActionType.ResetGlusterVolumeOptions,
                parameters,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }
        GlusterVolumeEntity glusterVolumeEntity = getEntity();
        ArrayList<GlusterVolumeOptionEntity> list = new ArrayList<>();
        for (GlusterVolumeOptionEntity glusterVolumeOption : glusterVolumeEntity.getOptions()) {
            list.add(glusterVolumeOption);
        }
        setItems(list);

    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getAddParameterCommand())) {
            addParameter();
        } else if (command.getName().equals("OnSetParameter")) { //$NON-NLS-1$
            onSetParameter();
        } else if (command.getName().equals("OnCancel")) { //$NON-NLS-1$
            cancel();
        } else if (command.equals(getEditParameterCommand())) {
            editParameter();
        } else if (command.equals(getResetParameterCommand())) {
            resetParameter();
        } else if (command.getName().equals("OnResetParameter")) { //$NON-NLS-1$
            onResetParameter();
        } else if (command.equals(getResetAllParameterCommand())) {
            resetAllParameters();
        } else if (command.getName().equals("OnResetAllParameters")) { //$NON-NLS-1$
            onResetAllParameters();
        }
    }
}
