package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.UserProfileTestHelper.buildValidationMessage;
import static org.ovirt.engine.core.bll.UserProfileTestHelper.checkAssertsForGenericProp;
import static org.ovirt.engine.core.bll.UserProfileTestHelper.checkAssertsForSshProp;
import static org.ovirt.engine.core.bll.UserProfileTestHelper.createWithId;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;

class UpdateUserProfilePropertyCommandTest extends BaseCommandTest {
    @Mock
    private UserProfileDao userProfileDaoMock;

    private UserProfilePropertyParameters parameters = mock(UserProfilePropertyParameters.class);

    @InjectMocks
    private final UpdateUserProfilePropertyCommand<UserProfilePropertyParameters> updateCommand =
            new UpdateUserProfilePropertyCommand<>(parameters, CommandContext.createContext(""));

    @Test
    void updatePublicSshKeyContent() {
        Guid userId = Guid.newGuid();
        Guid propertyId = Guid.newGuid();
        UserProfileProperty inputProp = UserProfileProperty.builder()
                .withDefaultSshProp()
                .withUserId(userId)
                .withPropertyId(propertyId)
                .withContent(
                        "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCyc9W33gGJWNFWA+lHOiFh/4H25KUaBoVrXRh1K2Jkxn3fclTjdUVw536Vx1vOfUNekmQIUm6OsuMUKDd64qdXDOMRrFdmZqmgBCsAYJpCeCg5ybyrPAIDSQM/2B57gipokJJS9/4sJf3L0OhiQrMr07p4xMaHCrNlSgBXTZWFcBoEKh5y5dxzOrZduBJE5Q17yclA/omb/fkSrXyw1jxkPG+m5x2YR/LSoloBGHHmYE4bXQ1n9wg8dCjYh2pC9tgL7g4q1USLrW5kMSk7HiaO7Da1dOMBlNFq5YAWEkZ9mgMW7prSnYelQ2o9RE0SddWiAsl75Vla5uprtLVWcfA784kVJJixwgjv5jk1lMaM7KmwH+Onbibdk74p8kdxo4CcYEUZhH0B4JhYP0P20t+xfRmqDSTji5zt6ioPevH2nWpC0uUL2PQssERZcHX9Y/UVyPPAQWYftVRaDzK/jwUSrARz5uQ9z80Cm7+tCV8gkBPCz9YDpIDy7rr6dBRb6I8= user@domain")
                .build();
        UserProfileProperty existingProp = UserProfileProperty.builder()
                .withDefaultSshProp()
                .withUserId(userId)
                .withPropertyId(propertyId)
                .withContent(
                        "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDBj3lf14pTF0jBKDa+d83CIuppk8NX+3yJIhwzgFGEFzn4z9M+h4Ph7kYj135LLw1xztJG46oi9PkdnlurfUeHyBOAlat05ZREpadwXKYjOBlIq4dD5h8/TF7dNU0lBx6C+0TnHb6Dc573tyaGVdS7Wa+fPuM2cEQKnCAEI2zucpODX2eFWL2eriK+/iYfW0CQB+O2+ZWMYFTPyBvTEF6GMRfy9+ZleqXbS69hzwlyQHMNKeAVDy9ijdfOZZTx2+ltSV/yFDtqi4Hg8JPYfjt9DYV8QwSWYXxzrjmdPBVz3ZmE+6KKM+ul1alYUvwSGWU5ACcJko9KowSUeYx169k02hX8CZCOpaUge78xvJD+h9cRR/cKyeOHK6HyRvtVlgSPWyH3EGz1WVR2DktTFKuX3ZHqvJ8u8FLjte5u1fs9Cvqr14TVvHDU/Nyqjcac2elkiHgaHtgDMbMf1AtQJs1WDKzFuIOCS68VLqRBt7TS2pWyajwEvnSw1NSVDNzPpUk= other@other")
                .build();

        when(userProfileDaoMock.get(propertyId)).thenReturn(existingProp);
        when(userProfileDaoMock.update(any())).thenReturn(mock(UserProfileProperty.class));

        when(parameters.getUserProfileProperty()).thenReturn(inputProp);
        updateCommand.setCurrentUser(createWithId(userId));

        assertTrue(updateCommand.validate(), buildValidationMessage(updateCommand.getReturnValue()));

        updateCommand.executeCommand();

        verify(userProfileDaoMock).update(
                argThat((UserProfileProperty outputProp) -> {
                            checkAssertsForSshProp(inputProp, outputProp);
                            checkAssertsForGenericProp(inputProp, outputProp);
                            return true;
                        }
                ));
        verify(userProfileDaoMock).get(propertyId);
        verifyNoMoreInteractions(userProfileDaoMock);
        assertTrue(updateCommand.getReturnValue().getSucceeded());
        assertNotNull(updateCommand.getReturnValue().getActionReturnValue());
    }

    @Test
    void noProperty() {
        when(parameters.getUserProfileProperty()).thenReturn(mock(UserProfileProperty.class));
        assertFalse(updateCommand.validate(), buildValidationMessage(updateCommand.getReturnValue()));
    }

    @Test
    void notAuthorized() {
        when(parameters.getUserProfileProperty()).thenReturn(mock(UserProfileProperty.class));
        when(userProfileDaoMock.get(any())).thenReturn(mock(UserProfileProperty.class));
        assertFalse(updateCommand.validate(), buildValidationMessage(updateCommand.getReturnValue()));
    }
}
