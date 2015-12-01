package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Set;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

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
        setAddParameterCommand(new UICommand(ConstantsManager.getInstance().getConstants().AddVolume(), this));
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

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                VolumeParameterListModel volumeParameterListModel = (VolumeParameterListModel) model;
                VolumeParameterModel innerParameterModel = (VolumeParameterModel) getWindow();

                ArrayList<GlusterVolumeOptionInfo> optionInfoList = new ArrayList<>();

                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) result;

                if (!returnValue.getSucceeded()) {
                    innerParameterModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .errorInFetchingVolumeOptionList());
                } else {
                    optionInfoList =
                            new ArrayList<>((Set<GlusterVolumeOptionInfo>) returnValue.getReturnValue());
                    optionInfoList.add(getCifsVolumeOption());
                }

                innerParameterModel.getKeyList().setItems(optionInfoList);
                innerParameterModel.stopProgress();

                UICommand command = UICommand.createDefaultOkUiCommand("OnSetParameter", volumeParameterListModel); //$NON-NLS-1$
                innerParameterModel.getCommands().add(command);
                innerParameterModel.getCommands().add(UICommand.createCancelUiCommand("OnCancel", volumeParameterListModel)); //$NON-NLS-1$
            }
        };
        AsyncDataProvider.getInstance().getGlusterVolumeOptionInfoList(_asyncQuery, volume.getClusterId());
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
        option.setKey(model.getSelectedKey().getEntity());
        option.setValue(model.getValue().getEntity());

        model.startProgress();

        Frontend.getInstance().runAction(VdcActionType.SetGlusterVolumeOption,
                new GlusterVolumeOptionParameters(option),
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        VolumeParameterListModel localModel = (VolumeParameterListModel) result.getState();
                        localModel.postOnSetParameter(result.getReturnValue());
                    }
                }, this);
    }

    public void postOnSetParameter(VdcReturnValueBase returnValue) {
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
        volumeParameterModel.getSelectedKey().setIsChangeable(false);
        volumeParameterModel.startProgress();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                VolumeParameterListModel volumeParameterListModel = (VolumeParameterListModel) model;
                VolumeParameterModel innerParameterModel = (VolumeParameterModel) getWindow();

                ArrayList<GlusterVolumeOptionInfo> optionInfoList = new ArrayList<>();

                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) result;

                if (!returnValue.getSucceeded()) {
                    innerParameterModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .errorInFetchingVolumeOptionList());
                } else {
                    optionInfoList =
                            new ArrayList<>((Set<GlusterVolumeOptionInfo>) returnValue.getReturnValue());
                    optionInfoList.add(getCifsVolumeOption());
                }

                innerParameterModel.getKeyList().setItems(optionInfoList);

                GlusterVolumeOptionEntity selectedOption = getSelectedItem();
                innerParameterModel.getDescription().setEntity(""); //$NON-NLS-1$
                innerParameterModel.getSelectedKey().setEntity(selectedOption.getKey());
                innerParameterModel.getValue().setEntity(selectedOption.getValue());

                innerParameterModel.stopProgress();

                UICommand command = UICommand.createDefaultOkUiCommand("OnSetParameter", volumeParameterListModel); //$NON-NLS-1$
                innerParameterModel.getCommands().add(command);
                command = UICommand.createCancelUiCommand("OnCancel", volumeParameterListModel); //$NON-NLS-1$
                innerParameterModel.getCommands().add(command);
            }
        };
        AsyncDataProvider.getInstance().getGlusterVolumeOptionInfoList(_asyncQuery, volume.getClusterId());
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

        Frontend.getInstance().runAction(VdcActionType.ResetGlusterVolumeOptions,
                parameters,
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
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

        Frontend.getInstance().runAction(VdcActionType.ResetGlusterVolumeOptions,
                parameters,
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
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
        }
        else if (command.getName().equals("OnSetParameter")) { //$NON-NLS-1$
            onSetParameter();
        }
        else if (command.getName().equals("OnCancel")) { //$NON-NLS-1$
            cancel();
        }
        else if (command.equals(getEditParameterCommand())) {
            editParameter();
        }
        else if (command.equals(getResetParameterCommand())) {
            resetParameter();
        }
        else if (command.getName().equals("OnResetParameter")) { //$NON-NLS-1$
            onResetParameter();
        }
        else if (command.equals(getResetAllParameterCommand())) {
            resetAllParameters();
        }
        else if (command.getName().equals("OnResetAllParameters")) { //$NON-NLS-1$
            onResetAllParameters();
        }
    }
}
