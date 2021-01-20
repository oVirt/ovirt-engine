package org.ovirt.engine.core.bll;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;

class GetUserProfilePropertyQueryTest
        extends AbstractUserQueryTest<UserProfilePropertyIdQueryParameters, GetUserProfilePropertyQuery<UserProfilePropertyIdQueryParameters>> {

    @Mock
    private UserProfileDao userProfileDaoMock;

    @Test
    public void executeQueryCommand() {
        when(getQueryParameters().getId()).thenReturn(Guid.newGuid());
        when(getQueryParameters().getType()).thenReturn(UserProfileProperty.PropertyType.SSH_PUBLIC_KEY);
        when(getUser().getId()).thenReturn(Guid.newGuid());

        UserProfileProperty existingProperty = createProperty(getUser().getId(), getQueryParameters().getId());
        when(userProfileDaoMock.get(getQueryParameters().getId())).thenReturn(existingProperty);

        getQuery().executeQueryCommand();

        assertThat((UserProfileProperty) getQuery().getQueryReturnValue().getReturnValue())
                .isEqualTo(existingProperty);
    }

    @Test
    public void propertyNotFound() {
        getQuery().executeQueryCommand();

        assertThat((UserProfileProperty) getQuery().getQueryReturnValue().getReturnValue())
                .isNull();
    }

    @Test
    public void propertyIncorrectType() {
        when(getQueryParameters().getId()).thenReturn(Guid.newGuid());
        when(getQueryParameters().getType()).thenReturn(UserProfileProperty.PropertyType.JSON);
        UserProfileProperty existingProperty = createProperty(Guid.Empty, getQueryParameters().getId());
        when(userProfileDaoMock.get(getQueryParameters().getId())).thenReturn(existingProperty);

        getQuery().executeQueryCommand();

        assertThat((UserProfileProperty) getQuery().getQueryReturnValue().getReturnValue())
                .isNull();
        assertThat(getQuery().getQueryReturnValue().getSucceeded()).isFalse();
    }

    @Test
    public void userNotAuthorized() {
        when(getQueryParameters().getId()).thenReturn(Guid.newGuid());
        when(getQueryParameters().getType()).thenReturn(UserProfileProperty.PropertyType.SSH_PUBLIC_KEY);
        when(getUser().getId()).thenReturn(Guid.newGuid());

        UserProfileProperty existingProperty = createProperty(Guid.Empty, getQueryParameters().getId());
        when(userProfileDaoMock.get(getQueryParameters().getId())).thenReturn(existingProperty);

        getQuery().executeQueryCommand();

        assertThat((UserProfileProperty) getQuery().getQueryReturnValue().getReturnValue())
                .isNull();
    }

    private UserProfileProperty createProperty(Guid userId, Guid propertyId) {
        return UserProfileProperty.builder()
                .withDefaultSshProp()
                .withPropertyId(propertyId)
                .withContent("some_content")
                .withUserId(userId)
                .build();
    }
}
