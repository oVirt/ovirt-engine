package org.ovirt.engine.core.bll;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;

class GetUserProfilePropertyQueryTest
        extends AbstractUserQueryTest<IdQueryParameters, GetUserProfilePropertyQuery<IdQueryParameters>> {

    @Mock
    private UserProfileDao userProfileDaoMock;

    @Test
    public void executeQueryCommand() {
        when(getQueryParameters().getId()).thenReturn(Guid.newGuid());
        when(getUser().getId()).thenReturn(Guid.newGuid());

        UserProfileProperty existingProperty = createProperty(getUser().getId());
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
    public void userNotAuthorized() {
        when(getQueryParameters().getId()).thenReturn(Guid.newGuid());
        when(getUser().getId()).thenReturn(Guid.newGuid());

        UserProfileProperty existingProperty = createProperty(Guid.Empty);
        when(userProfileDaoMock.get(getQueryParameters().getId())).thenReturn(existingProperty);

        getQuery().executeQueryCommand();

        assertThat((UserProfileProperty) getQuery().getQueryReturnValue().getReturnValue())
                .isNull();
    }

    private UserProfileProperty createProperty(Guid userId) {
        return UserProfileProperty.builder()
                .withDefaultSshProp()
                .withPropertyId(getQueryParameters().getId())
                .withContent("some_content")
                .withUserId(userId)
                .build();
    }
}
