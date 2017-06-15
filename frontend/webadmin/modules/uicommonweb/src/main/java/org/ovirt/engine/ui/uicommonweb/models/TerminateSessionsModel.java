package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TerminateSessionParameters;
import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class TerminateSessionsModel extends ConfirmationModel {

    private static final String CMD_TEMINATE = "OnTerminate"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final SearchableListModel sourceListModel;
    private final List<UserSession> sessions;

    public TerminateSessionsModel(SearchableListModel<UserSession, UserSession> sourceListModel) {
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
        for (UserSession session : sessions) {
            final long sessionId = session.getId();
            final String sessionUserName = session.getUserName();
            final String userSessionRow = ConstantsManager.getInstance().getMessages()
                    .userSessionRow(sessionId, sessionUserName);
            sessionStrings.add(userSessionRow);
        }
        setItems(sessionStrings);
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    private void onTerminate() {
        final List<ActionParametersBase> parameterList = new ArrayList<>(sessions.size());
        for (UserSession session : sessions) {
            final long sessionId = session.getId();
            final TerminateSessionParameters terminateSessionParameters = new TerminateSessionParameters(sessionId);
            parameterList.add(terminateSessionParameters);
        }

        Frontend.getInstance()
                .runMultipleActions(ActionType.TerminateSession, parameterList,
                        result -> sourceListModel.getSearchCommand().execute());
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
