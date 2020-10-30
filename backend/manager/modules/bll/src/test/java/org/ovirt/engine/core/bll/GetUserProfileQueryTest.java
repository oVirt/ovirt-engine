package org.ovirt.engine.core.bll;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;

class GetUserProfileQueryTest
        extends AbstractUserQueryTest<IdQueryParameters, GetUserProfileQuery<IdQueryParameters>> {

    @Mock
    private UserProfileDao userProfileDaoMock;

    @Test
    void executeQueryCommand() {
        Guid userId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(userId);
        when(getUser().getId()).thenReturn(userId);
        when(userProfileDaoMock.getProfile(getQueryParameters().getId()))
                .thenReturn(mock(UserProfile.class));

        getQuery().executeQueryCommand();

        assertNotNull(getQuery().getQueryReturnValue().getReturnValue());
        verify(userProfileDaoMock).getProfile(any());
        verifyNoMoreInteractions(userProfileDaoMock);
    }

    @Test
    public void notAuthorized() {
        when(getQueryParameters().getId()).thenReturn(Guid.newGuid());
        when(getUser().getId()).thenReturn(Guid.newGuid());

        getQuery().executeQueryCommand();

        verifyZeroInteractions(userProfileDaoMock);
        assertThat((Object) getQuery().getQueryReturnValue().getReturnValue()).isNull();
    }

}
