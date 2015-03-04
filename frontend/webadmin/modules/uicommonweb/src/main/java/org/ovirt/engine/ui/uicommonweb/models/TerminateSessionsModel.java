package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.TerminateSessionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class TerminateSessionsModel extends ConfirmationModel {

    private static final String CMD_TEMINATE = "OnTerminate"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final SearchableListModel sourceListModel;
    private final List<EngineSession> sessions;

    public TerminateSessionsModel(SearchableListModel<EngineSession, EngineSession> sourceListModel) {
        this.sourceListModel = sourceListModel;
        this.sessions = sourceListModel.getSelectedItems();

        setTitle(ConstantsManager.getInstance().getConstants().terminateSessionTitle());
        setMessage(ConstantsManager.getInstance().getConstants().terminateSessionConfirmation());
        setHelpTag(HelpTag.terminate_session);
        setHashName("terminate_sessions"); //$NON-NLS-1$

        UICommand okCommand = UICommand.createOkUiCommand(CMD_TEMINATE, this);
        getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createDefaultCancelUiCommand(CMD_CANCEL, this);
        getCommands().add(cancelCommand);
    }

    @Override
    public void initialize() {
        super.initialize();
        setSessionsDetails();
    }

    private void setSessionsDetails() {
        final List<String> sessionStrings = new ArrayList<>();
        for (EngineSession session : sessions) {
            sessionStrings.add(StringFormat.format("%s:%d, %s:%s", //$NON-NLS-1$
                    ConstantsManager.getInstance().getConstants().sessionDbId(),
                    session.getId(),
                    ConstantsManager.getInstance().getConstants().userName(),
                    session.getUserName()));
        }
        setItems(sessionStrings);
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    private void onTerminate() {
        List<VdcActionParametersBase> parameterList = new ArrayList<>(sessions.size());
        for (EngineSession session : sessions) {
            final TerminateSessionParameters terminateSessionParameters =
                    new TerminateSessionParameters(session.getId());
            parameterList.add(terminateSessionParameters);
        }

        Frontend.getInstance()
                .runMultipleActions(VdcActionType.TerminateSession, parameterList, new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        sourceListModel.getSearchCommand().execute();
                    }
                });
        cancel();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (CMD_TEMINATE.equals(command.getName())) {
            onTerminate();
        } else if (CMD_CANCEL.equals(command.getName())) {
            cancel();
        }
    }

}
