package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.UserProfileTestHelper.buildValidationMessage;
import static org.ovirt.engine.core.bll.UserProfileTestHelper.createWithId;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;

class RemoveUserProfilePropertyCommandTest extends BaseCommandTest {
    @Mock
    private UserProfileDao userProfileDaoMock;

    private IdParameters parameters = mock(IdParameters.class);

    @InjectMocks
    private final RemoveUserProfilePropertyCommand<IdParameters> removeCommand =
            new RemoveUserProfilePropertyCommand<>(parameters, CommandContext.createContext(""));

    @Test
    void removeProp() {
        Guid userId = Guid.newGuid();
        Guid propertyId = Guid.newGuid();
        UserProfileProperty existingProp = UserProfileProperty.builder()
                .withDefaultSshProp()
                .withUserId(userId)
                .withPropertyId(propertyId)
                .build();

        when(userProfileDaoMock.get(propertyId)).thenReturn(existingProp);
        when(parameters.getId()).thenReturn(propertyId);
        removeCommand.setCurrentUser(createWithId(userId));

        assertTrue(removeCommand.validate(), buildValidationMessage(removeCommand.getReturnValue()));

        removeCommand.executeCommand();

        verify(userProfileDaoMock).remove(eq(propertyId));
        verify(userProfileDaoMock).get(propertyId);
        verifyNoMoreInteractions(userProfileDaoMock);
        assertTrue(removeCommand.getReturnValue().getSucceeded());

    }

    @Test
    void noProperty() {
        when(parameters.getId()).thenReturn(Guid.newGuid());
        assertFalse(removeCommand.validate(), buildValidationMessage(removeCommand.getReturnValue()));
    }

    @Test
    void notAuthorized() {
        when(parameters.getId()).thenReturn(Guid.newGuid());
        when(userProfileDaoMock.get(any())).thenReturn(mock(UserProfileProperty.class));
        assertFalse(removeCommand.validate(), buildValidationMessage(removeCommand.getReturnValue()));
    }
}
