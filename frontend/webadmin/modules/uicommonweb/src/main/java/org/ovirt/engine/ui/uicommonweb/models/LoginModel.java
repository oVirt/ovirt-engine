package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class LoginModel extends Model {

    public static final EventDefinition loggedInEventDefinition;
    private Event<EventArgs> privateLoggedInEvent;

    public Event<EventArgs> getLoggedInEvent() {
        return privateLoggedInEvent;
    }

    private void setLoggedInEvent(Event<EventArgs> value) {
        privateLoggedInEvent = value;
    }

    private ListModel<String> privateProfile;

    public ListModel<String> getProfile() {
        return privateProfile;
    }

    private void setProfile(ListModel<String> value) {
        privateProfile = value;
    }

    private EntityModel<String> privateUserName;

    public EntityModel<String> getUserName() {
        return privateUserName;
    }

    private void setUserName(EntityModel<String> value) {
        privateUserName = value;
    }

    private EntityModel<Boolean> createInstanceOnly;

    public EntityModel<Boolean> getCreateInstanceOnly() {
        return createInstanceOnly;
    }

    public void setCreateInstanceOnly(EntityModel<Boolean> createInstanceOnly) {
        this.createInstanceOnly = createInstanceOnly;
    }

    private DbUser privateLoggedUser;

    // If true, indicates that the model is in the process of logging in automatically
    private boolean loggingInAutomatically = false;

    public DbUser getLoggedUser() {
        return privateLoggedUser;
    }

    protected void setLoggedUser(DbUser value) {
        privateLoggedUser = value;
    }

    static {
        loggedInEventDefinition = new EventDefinition("LoggedIn", LoginModel.class); //$NON-NLS-1$
    }

    private List<String> messages;

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> value) {
        if (!Objects.equals(messages, value)) {
            messages = value;
            onPropertyChanged(new PropertyChangedEventArgs("Message")); //$NON-NLS-1$
        }
    }

    public LoginModel() {
        setLoggedInEvent(new Event<>(loggedInEventDefinition));

        setProfile(new ListModel<String>());
        getProfile().setIsChangeable(false);
        setUserName(new EntityModel<String>());
        getUserName().setIsChangeable(false);
        getUserName().getEntityChangedEvent().addListener(this);
        setCreateInstanceOnly(new EntityModel<>(false));

        AsyncQuery<List<String>> asyncQuery = new AsyncQuery<>(domains -> {

            if (domains == null) {
                setMessages(Arrays.asList(ConstantsManager.getInstance()
                        .getConstants()
                        .couldNotConnectToOvirtEngineServiceMsg()));
                return;
            }

            if (!loggingInAutomatically) {
                // Don't enable the screen when we are in the process of logging in automatically.
                // If this happens to be executed before the AutoLogin() is executed,
                // it is not a problem, as the AutoLogin() will disable the screen by itself.
                getUserName().setIsChangeable(true);
                getProfile().setIsChangeable(true);
            }

            Collections.sort(domains);
            getProfile().setItems(domains);

        });
        asyncQuery.setHandleFailure(true);
        AsyncDataProvider.getInstance().getAAAProfilesListViaPublic(asyncQuery, true);
    }

    protected void raiseLoggedInEvent() {
        // Cache all configurations values before logging-in
        AsyncDataProvider.getInstance().initCache(this);
    }

    public void autoLogin(DbUser user, UserProfileProperty webAdminOption) {
        loggingInAutomatically = true;
        getUserName().setEntity(user.getLoginName());
        getProfile().setSelectedItem(user.getDomain());
        disableLoginScreen();
        setLoggedUser(user);
        Frontend.getInstance().setLoggedInUser(user);
        Frontend.getInstance().getUserProfileManager().setInjectedWebAdminUserOption(webAdminOption);
        raiseLoggedInEvent();
    }

    protected void disableLoginScreen() {
        getUserName().setIsChangeable(false);
        getProfile().setIsChangeable(false);
    }

    protected boolean validate() {
        getUserName().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getProfile().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getUserName().getIsValid() && getProfile().getIsValid();
    }

    @Override
    public void cleanup() {
        cleanupEvents(getLoggedInEvent());
        super.cleanup();
    }
}
