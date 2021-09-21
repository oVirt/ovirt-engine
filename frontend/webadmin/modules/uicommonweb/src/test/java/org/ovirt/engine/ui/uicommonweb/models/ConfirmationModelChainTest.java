package org.ovirt.engine.ui.uicommonweb.models;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelChain.ConfirmationModelChainItem;

class ConfirmationModelChainTest {

    private ConfirmationModelChain chain;

    private Runnable successCallback;

    private Runnable confirmationConfirmedCallback;

    private Model parentModel;

    private ConfirmationModel confirmation1;

    private ConfirmationModel confirmation2;

    private ConfirmationModel confirmation3;

    private ConfirmationModel confirmation4;

    @BeforeEach
    public void before() {
        chain = new ConfirmationModelChain();

        chain.setOkCommand(mock(UICommand.class));
        chain.setCancelCommand(mock(UICommand.class));

        successCallback = mock(Runnable.class);
        confirmationConfirmedCallback = mock(Runnable.class);
        parentModel = mock(Model.class);

        confirmation1 = mock(ConfirmationModel.class);
        confirmation2 = mock(ConfirmationModel.class);
        confirmation3 = mock(ConfirmationModel.class);
        confirmation4 = mock(ConfirmationModel.class);
    }

    @Test
    void emptyList() {
        chain.execute(parentModel, successCallback);

        verify(parentModel, never()).setConfirmWindow(any());
        verify(successCallback, times(1)).run();
    }

    @Test
    void noApplicableConfirmation() {
        chain.addConfirmation(createChainItem(confirmation1, false, confirmationConfirmedCallback));
        chain.addConfirmation(createChainItem(confirmation2, false));
        chain.addConfirmation(createChainItem(confirmation3, false));
        chain.addConfirmation(createChainItem(confirmation4, false));

        chain.execute(parentModel, successCallback);

        verify(confirmationConfirmedCallback, never()).run();
        verify(parentModel, never()).setConfirmWindow(any());
        verify(successCallback, times(1)).run();
    }

    @Test
    void allOkConfirmations() {
        chain.addConfirmation(createChainItem(confirmation1, true));
        chain.addConfirmation(createChainItem(confirmation2, true, confirmationConfirmedCallback));
        chain.addConfirmation(createChainItem(confirmation3, true, confirmationConfirmedCallback));
        chain.addConfirmation(createChainItem(confirmation4, false, confirmationConfirmedCallback));

        doAnswer(invocation -> {
            chain.executeCommand(chain.getOkCommand());
            return null;
        }).when(parentModel).setConfirmWindow(notNull());

        chain.execute(parentModel, successCallback);

        InOrder inOrder = Mockito.inOrder(parentModel);
        inOrder.verify(parentModel).setConfirmWindow(confirmation1);
        inOrder.verify(parentModel).setConfirmWindow(confirmation2);
        inOrder.verify(parentModel).setConfirmWindow(confirmation3);
        inOrder.verify(parentModel).setConfirmWindow(null);

        verify(confirmationConfirmedCallback, times(2)).run();
        verify(successCallback, times(1)).run();
    }

    @Test
    void cancelledConfirmation() {
        chain.addConfirmation(createChainItem(confirmation1, true));
        chain.addConfirmation(createChainItem(confirmation2, false, confirmationConfirmedCallback));
        chain.addConfirmation(createChainItem(confirmation3, true, confirmationConfirmedCallback));
        chain.addConfirmation(createChainItem(confirmation4, true));

        doAnswer(invocation -> {
            chain.executeCommand(chain.getOkCommand());
            return null;
        }).when(parentModel).setConfirmWindow(confirmation1);

        doAnswer(invocation -> {
            chain.executeCommand(chain.getCancelCommand());
            return null;
        }).when(parentModel).setConfirmWindow(confirmation3);

        chain.execute(parentModel, successCallback);

        InOrder inOrder = Mockito.inOrder(parentModel);
        inOrder.verify(parentModel).setConfirmWindow(confirmation1);
        inOrder.verify(parentModel).setConfirmWindow(confirmation3);
        inOrder.verify(parentModel).setConfirmWindow(null);

        verify(confirmationConfirmedCallback, never()).run();
        verify(successCallback, never()).run();
    }

    private ConfirmationModelChainItem createChainItem(ConfirmationModel confirmation, boolean isRequired) {
        return createChainItem(confirmation, isRequired, null);
    }

    private ConfirmationModelChainItem createChainItem(ConfirmationModel confirmation, boolean isRequired, Runnable callback) {
        return new ConfirmationModelChainItem() {

            @Override
            public boolean isRequired() {
                return isRequired;
            }

            @Override
            public ConfirmationModel getConfirmation() {
                return confirmation;
            }

            @Override
            public void onConfirm() {
                if (callback != null) {
                    callback.run();
                }
            }
        };
    }
}
