package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;

class AddUserProfilePropertyCommandTest extends BaseCommandTest {
    @Mock
    private UserProfileDao userProfileDaoMock;

    private UserProfilePropertyParameters parameters = mock(UserProfilePropertyParameters.class);

    @InjectMocks
    private final AddUserProfilePropertyCommand<UserProfilePropertyParameters> addCommand =
            new AddUserProfilePropertyCommand<>(parameters, CommandContext.createContext(""));

    @Test
    void addSinglSshPropToEmptyProfile() {
        Guid userId = Guid.newGuid();
        UserProfileProperty inputProp = UserProfileProperty.builder()
                .withDefaultSshProp()
                .withUserId(userId)
                .withContent(
                        "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCyc9W33gGJWNFWA+lHOiFh/4H25KUaBoVrXRh1K2Jkxn3fclTjdUVw536Vx1vOfUNekmQIUm6OsuMUKDd64qdXDOMRrFdmZqmgBCsAYJpCeCg5ybyrPAIDSQM/2B57gipokJJS9/4sJf3L0OhiQrMr07p4xMaHCrNlSgBXTZWFcBoEKh5y5dxzOrZduBJE5Q17yclA/omb/fkSrXyw1jxkPG+m5x2YR/LSoloBGHHmYE4bXQ1n9wg8dCjYh2pC9tgL7g4q1USLrW5kMSk7HiaO7Da1dOMBlNFq5YAWEkZ9mgMW7prSnYelQ2o9RE0SddWiAsl75Vla5uprtLVWcfA784kVJJixwgjv5jk1lMaM7KmwH+Onbibdk74p8kdxo4CcYEUZhH0B4JhYP0P20t+xfRmqDSTji5zt6ioPevH2nWpC0uUL2PQssERZcHX9Y/UVyPPAQWYftVRaDzK/jwUSrARz5uQ9z80Cm7+tCV8gkBPCz9YDpIDy7rr6dBRb6I8= user@domain")
                .build();
        UserProfile existingProfile = UserProfile.builder()
                .withUserId(userId)
                .build();

        when(userProfileDaoMock.getProfile(any())).thenReturn(existingProfile);

        when(parameters.getUserProfileProperty()).thenReturn(inputProp);
        addCommand.setCurrentUser(createWithId(userId));

        assertTrue(addCommand.validate(), buildValidationMessage(addCommand.getReturnValue()));

        addCommand.executeCommand();

        verify(userProfileDaoMock).save(
                argThat((UserProfileProperty outputProp) -> {
                            checkAssertsForSshProp(inputProp, outputProp);
                            checkAssertsForGenericProp(inputProp, outputProp);
                            return true;
                        }
                ));
        verify(userProfileDaoMock).getProfile(any());
        verifyNoMoreInteractions(userProfileDaoMock);
        assertTrue(addCommand.getReturnValue().getSucceeded());

    }
}
