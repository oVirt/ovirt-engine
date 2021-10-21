package org.ovirt.engine.core.bll;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;

class GetUserProfilePropertiesByUserIdQueryTest extends AbstractUserQueryTest<UserProfilePropertyIdQueryParameters, GetUserProfilePropertiesByUserIdQuery<UserProfilePropertyIdQueryParameters>> {

    @Mock
    private UserProfileDao userProfileDaoMock;

    @Test
    public void executeAsUser() {
        Guid userId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(userId);
        when(getQueryParameters().getType()).thenReturn(UserProfileProperty.PropertyType.SSH_PUBLIC_KEY);
        when(getUser().getId()).thenReturn(userId);

        UserProfileProperty sshProp = sshProp();
        UserProfileProperty jsonProp = jsonProp();
        executeQueryInternal(List.of(sshProp, jsonProp), List.of(sshProp));
    }

    private UserProfileProperty sshProp() {
        return UserProfileProperty.builder()
                .withDefaultSshProp()
                .withNewId()
                .withContent("some_content")
                .withUserId(getUser().getId())
                .build();
    }

    private UserProfileProperty jsonProp() {
        return UserProfileProperty.builder()
                .withTypeJson()
                .withName("OtherUnusedProp")
                .withNewId()
                .withContent("{}")
                .withUserId(getUser().getId())
                .build();
    }

    private void executeQueryInternal(List<UserProfileProperty> allAvailable,
            List<UserProfileProperty> filteredByType) {
        when(userProfileDaoMock.getAll(getQueryParameters().getId())).thenReturn(allAvailable);

        getQuery().executeQueryCommand();

        List<UserProfileProperty> properties = getQuery().getQueryReturnValue().getReturnValue();
        assertThat(properties).containsExactlyInAnyOrderElementsOf(filteredByType);
    }

    @Test
    public void notAuthorized() {
        when(getQueryParameters().getId()).thenReturn(Guid.newGuid());
        when(getUser().getId()).thenReturn(Guid.newGuid());

        getQuery().executeQueryCommand();

        verifyZeroInteractions(userProfileDaoMock);
        assertThat((Object) getQuery().getQueryReturnValue().getReturnValue()).isNull();
    }

    @Test
    public void executeAsAdminWithDifferentTargetUserId() {
        Guid userId = Guid.newGuid();
        Guid targetId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(targetId);
        when(getQueryParameters().getType()).thenReturn(UserProfileProperty.PropertyType.SSH_PUBLIC_KEY);
        when(getUser().getId()).thenReturn(userId);
        when(getUser().isAdmin()).thenReturn(true);

        UserProfileProperty sshProp = sshProp();
        UserProfileProperty jsonProp = jsonProp();
        executeQueryInternal(List.of(sshProp, jsonProp), List.of(sshProp));
    }

    @Test
    public void fetchAllProperties() {
        Guid userId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(userId);
        when(getQueryParameters().getType()).thenReturn(null);
        when(getUser().getId()).thenReturn(userId);

        List<UserProfileProperty> all = List.of(sshProp(), jsonProp());
        executeQueryInternal(all, all);
    }

}
