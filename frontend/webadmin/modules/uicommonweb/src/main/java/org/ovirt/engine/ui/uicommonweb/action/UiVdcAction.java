package org.ovirt.engine.ui.uicommonweb.action;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

/**
* This class represents an action that runs a single vdcAction <code>Frontend.getInstance().runAction(..)</code>.
*
* An example for using the class-
* <code>actionA.then(actionB).and(actionC).and(actionD).then(actionE).and(actionF).onAllExecutionsFinish(actionG)</code>
* <code>actionA.runAction()</code>
*
* Since the vdc actions are asynchronous the flow will be the following-
* 1. actionA will execute a vdcAction.
* 2. actionB, actionC and actionD will be executed in a parallel manner (will be sent to the server without waiting for each other's results)
* just after the result of actionA is returned.
* 3. actionE/F will be executed parallely after actionD's result is returned.
* 4. actionG is the <code>final</code>, it will be executed just after the results of all the actions in the flow was returned.
*
* actionA->actionB
*        ->actionC
*        ->actionD->actionE
*                 ->actionF
*                          ->actionG
*
* In case of failures-
* 1. If actionA fails- all the other action won't be executed (since they are dependent on its result).
* 2. If actionB/C fails- actionG won't be executed, since it is the final action and is executed just if all the previous actions in the flow were executed.
*                        All the other actions won't be affected since they are not dependent on actionB/C.
* 3. If actionD fails actionE/F/G won't be executed.
* 4. If actionE/F fails- actionG won't be executed.
*/
public class UiVdcAction extends UiAction {

    private ActionType actionType;
    private ActionParametersBase parameters;
    private boolean showErrorDialogOnFirstFailure;

    /**
     * @param actionType
     *            the <code>VdsActionType</code>
     * @param parameters
     *            the parameters of the action
     * @param model
     *            model the model on which to start and stop the progress.
     * @param showErrorDialog
     *            if true, in case of a error, the error dialog will be displayed immediately. Otherwise, the error will
     *            be collected and displayed with all the other errors at the end of the flow.
     */
    public UiVdcAction(ActionType actionType,
            ActionParametersBase parameters,
            Model model,
            boolean showErrorDialogOnFirstFailure) {
        super(model);
        this.actionType = actionType;
        this.parameters = parameters;
        this.showErrorDialogOnFirstFailure = showErrorDialogOnFirstFailure;
    }

    public UiVdcAction(ActionType actionType, ActionParametersBase parameters, Model model) {
        this(actionType, parameters, model, false);
    }

    @Override
    void internalRunAction() {
        IFrontendActionAsyncCallback callback = createCallback();
        Frontend.getInstance().runAction(actionType, parameters, callback, showErrorDialogOnFirstFailure);
    }

    private IFrontendActionAsyncCallback createCallback() {
        return result -> {
            ActionReturnValue returnValue = result.getReturnValue();
            if (returnValue == null || !returnValue.getSucceeded()) {
                if (!showErrorDialogOnFirstFailure && returnValue != null) {
                    getActionFlowState().addFailure(actionType, returnValue);
                }

                // Reset the next action
                then(null);
            }

            runNextAction();
        };
    }
}
