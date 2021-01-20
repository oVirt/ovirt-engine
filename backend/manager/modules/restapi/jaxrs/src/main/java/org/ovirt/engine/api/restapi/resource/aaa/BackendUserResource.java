package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.api.restapi.resource.aaa.BackendUserOptionResource.addUserProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.UserOption;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.EventSubscriptionsResource;
import org.ovirt.engine.api.resource.aaa.DomainUserGroupsResource;
import org.ovirt.engine.api.resource.aaa.SshPublicKeysResource;
import org.ovirt.engine.api.resource.aaa.UserOptionsResource;
import org.ovirt.engine.api.resource.aaa.UserResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.api.restapi.resource.BackendAssignedPermissionsResource;
import org.ovirt.engine.api.restapi.resource.BackendAssignedRolesResource;
import org.ovirt.engine.api.restapi.resource.BackendEventSubscriptionsResource;
import org.ovirt.engine.api.restapi.resource.BackendUserTagsResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This resource corresponds to an user that has been added to the engine and
 * stored in the database.
 */
public class BackendUserResource
        extends AbstractBackendSubResource<User, DbUser>
        implements UserResource {

    private static final String MERGE = "merge";

    private static final String[] IMMUTABLE_FIELDS = {
            "department",
            "domainEntryId",
            "email",
            "lastName",
            "loggedIn",
            "namespace",
            "password",
            "principal",
            "userName",
            "domain",
            "groups",
            "permissions",
            "roles",
            "sshPublicKeys",
            "tags" };
    private BackendUsersResource parent;

    public BackendUserResource(String id, BackendUsersResource parent) {
        super(id, User.class, DbUser.class);
        this.parent = parent;
    }

    public void setParent(BackendUsersResource parent) {
        this.parent = parent;
    }

    public BackendUsersResource getParent() {
        return parent;
    }

    @Override
    public User get() {
        return addUserProperties(
                performGet(
                        QueryType.GetDbUserByUserId,
                        new IdQueryParameters(guid),
                        BaseResource.class),
                getOptionsResource().list());
    }

    @Override
    public User update(User user) {
        boolean mergeOptions = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, MERGE, false, false);
        validateUpdate(user, get());

        Map<String, String> incomingPropertyValues = filterOutDuplicatedProperties(user.getUserOptions());
        Map<String, UserOption> existingProperties = retrieveExistingProperties();
        List<String> impactedProperties = calculateImpactedProperties(incomingPropertyValues, existingProperties);

        // one-by-one decide which operation applies to given property
        for (String name : impactedProperties) {
            String incomingContent = incomingPropertyValues.get(name);
            UserOption existingOption = existingProperties.get(name);

            if (incomingContent != null) {
                if (existingOption == null) {
                    addNewUserProfileProperty(name, incomingContent);
                } else {
                    updateExistingUserProfileProperty(name, existingOption, incomingContent);
                }
            } else if (existingOption != null && !mergeOptions) {
                removeUserProfileProperty(existingOption);
            }
        }

        return get();
    }

    private void removeUserProfileProperty(UserOption existingOption) {
        // in the default case (mergeOptions == false) all existing properties
        // that are not included in the update are removed
        performAction(ActionType.RemoveUserProfileProperty,
                new IdParameters(Guid.createGuidFromStringDefaultEmpty(existingOption.getId())));
    }

    /**
     * Calculate all properties impacted by this operation.
     * Each property on the list can be either updated, added or deleted.
     * @param incomingPropertyValues properties sent in the request
     * @param existingProperties properties currently stored in the DB
     * @return list of unique property names
     */
    private List<String> calculateImpactedProperties(Map<String, String> incomingPropertyValues, Map<String, UserOption> existingProperties) {
        return Stream.concat(
                incomingPropertyValues.keySet().stream(),
                existingProperties.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     *
     * @return map of (property name, property)
     */
    private Map<String, UserOption> retrieveExistingProperties() {
        return getOptionsResource().list().getUserOptions().stream()
                // properties from the DB are guaranteed to have unique names
                .collect(Collectors.toMap(UserOption::getName, prop -> prop));
    }

    /**
     * @return map of (property name, property value)
     */
    private Map<String, String> filterOutDuplicatedProperties(Properties properties) {
        return Optional.ofNullable(properties)
                .orElse(new Properties())
                .getProperties()
                .stream()
                .collect(Collectors.toMap(Property::getName,
                        Property::getValue,
                        // each property name should be unique
                        // filter out duplicates  using a merge function (last property wins)
                        (firstValue, secondValue) -> secondValue));
    }

    private void updateExistingUserProfileProperty(String name, UserOption existingOption, String incomingContent) {
        performAction(
                ActionType.UpdateUserProfileProperty,
                new UserProfilePropertyParameters(UserProfileProperty.builder()
                        .withName(name)
                        .withPropertyId(Guid.createGuidFromStringDefaultEmpty(existingOption.getId()))
                        .withTypeJson()
                        .withContent(incomingContent)
                        .withUserId(guid)
                        .build())
        );
    }

    private void addNewUserProfileProperty(String name, String incomingContent) {
        performAction(ActionType.AddUserProfileProperty,
                new UserProfilePropertyParameters(UserProfileProperty.builder()
                        .withName(name)
                        .withTypeJson()
                        .withContent(incomingContent)
                        .withUserId(guid)
                        .build()));
    }

    @Override
    protected String[] getStrictlyImmutable() {
        List<String> all = new ArrayList<>();
        all.addAll(Arrays.asList(super.getStrictlyImmutable()));
        all.addAll(Arrays.asList(IMMUTABLE_FIELDS));
        return all.toArray(new String[] {});
    }

    @Override
    public AssignedRolesResource getRolesResource() {
        return inject(new BackendAssignedRolesResource(guid));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendUserTagsResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                QueryType.GetPermissionsOnBehalfByAdElementId,
                new IdQueryParameters(guid),
                User.class));
    }

    @Override
    public SshPublicKeysResource getSshPublicKeysResource() {
        return inject(new BackendSSHPublicKeysResource(guid));
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveUser, new IdParameters(guid));
    }

    @Override
    public DomainUserGroupsResource getGroupsResource() {
        return inject(new BackendDomainUserGroupsResource(guid));
    }

    @Override
    public EventSubscriptionsResource getEventSubscriptionsResource() {
        return inject(new BackendEventSubscriptionsResource(id));
    }

    @Override public UserOptionsResource getOptionsResource() {
        return inject(new BackendUserOptionsResource(guid));
    }
}
