package org.ovirt.engine.ui.uicommonweb.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.ui.uicommonweb.action.UiAction.ActionFlowState;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.common.ProgressModel;

@ExtendWith(MockitoExtension.class)
public abstract class UiActionBaseTest {

    @Mock
    Model model;

    @Mock
    ProgressModel progressModel;

    @BeforeEach
    public void setUp() {
        when(model.getProgress()).thenReturn(null, progressModel);
    }

    protected void assertNoErrors(ActionFlowState flowState) {
        assertTrue(flowState.getFailedActionsMap().isEmpty());
    }

    protected void assertErrors(ActionFlowState flowState, int expectedNumOfErrors) {
        int numOfErrors = 0;
        for (List<ActionReturnValue> values : flowState.getFailedActionsMap().values()) {
            numOfErrors += values.size();
        }
        assertEquals(expectedNumOfErrors, numOfErrors);
    }

    private ActionFlowState getFlowState(List<UiAction> actions) {
        return actions.get(0).getActionFlowState();
    }

    protected void assertAllDone(List<UiAction> actions, boolean shouldCallProgressActions) {
        assertTrue(getFlowState(actions).isAllDone());
        int numOfProgressInteractions = shouldCallProgressActions ? 1 : 0;
        verify(model, times(numOfProgressInteractions)).startProgress();
        verify(model, times(numOfProgressInteractions)).stopProgress();

        UiAction previousAction = null;
        for (UiAction action : actions) {
            if (previousAction != null) {
                assertSame(previousAction.getActionFlowState(), action.getActionFlowState());
            }
            previousAction = action;
        }
    }

    protected void assertAllDone(List<UiAction> actions) {
        assertAllDone(actions, true);
    }

    protected void assertNotAllDone(ActionFlowState flowState) {
        assertFalse(flowState.isAllDone());
        verify(model).startProgress();
        verify(model, never()).stopProgress();
    }

    protected void assertFinishedWithNoErrors(List<UiAction> actions, boolean shouldCallProgressActions) {
        assertNoErrors(getFlowState(actions));
        assertAllDone(actions, shouldCallProgressActions);
    }

    protected void assertFinishedWithNoErrors(List<UiAction> actions) {
        assertFinishedWithNoErrors(actions, true);
    }

    protected void assertFinishedWithErrors(List<UiAction> actions, int numOfErrors) {
        assertErrors(getFlowState(actions), numOfErrors);
        assertAllDone(actions);
    }

    protected List<UiAction> runActionFlow(ActionType... actionTypes) {
        List<UiAction> actions = new ArrayList<>();

        for (int i = 0; i <= actionTypes.length; ++i) {
            actions.add(createAction());
        }

        for (int i = 0; i < actionTypes.length; ++i) {
            ActionType actionType = actionTypes[i];
            if (ActionType.next == actionType) {
                actions.get(i).then(actions.get(i + 1));
            } else if (ActionType.parallel == actionType) {
                actions.get(i).and(actions.get(i + 1));
            }
        }

        UiAction firstAction = actions.get(0);

        firstAction.runAction();

        return actions;
    }

    protected List<UiAction> runActionFlow(List<ActionType> actionTypes) {
        return runActionFlow(actionTypes.toArray(new ActionType[actionTypes.size()]));
    }

    protected abstract UiAction createAction();

    protected static enum ActionType {
        next,
        parallel
    }
}
