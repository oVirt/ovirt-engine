package org.ovirt.engine.ui.frontend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.ui.frontend.UserSettings.Builder.create;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

@ExtendWith(MockitoExtension.class)
public class UserSettingsTest {
    @Mock
    UserSettings.Parser parser;

    public static String MARKER = "{}"; //$NON-NLS-1$

    @Test
    public void defaultSettings() {
        UserSettings settings = UserSettings.defaultSettings();
        assertThat(settings).isNotNull();
        assertThat(settings.isLocalStoragePersistedOnServer()).isTrue();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getOriginalUserOptions()).isEmpty();
        assertThat(settings.getLocalStoragePersistenceVersion()).isNull();
    }

    @Test
    public void defaultSettingsForNullUser() {
        UserSettings settings = UserSettings.from(null);
        assertThat(settings).isNotNull();
        assertThat(settings.isLocalStoragePersistedOnServer()).isTrue();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getOriginalUserOptions()).isEmpty();
        assertThat(settings.getLocalStoragePersistenceVersion()).isNull();
    }

    @Test
    public void parseEmptyStorage() {
        UserSettings.Builder builder = new UserSettings.Builder(str -> parser);
        when(parser.parseStorage()).thenReturn(Collections.emptyMap());
        when(parser.getVersion()).thenReturn(null);
        when(parser.getLocalStoragePersistence()).thenReturn(null);

        DbUser user = createUser();
        UserSettings settings = builder.fromUser(user).build();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getLocalStoragePersistenceVersion()).isNull();
        assertThat(settings.isLocalStoragePersistedOnServer()).isTrue();
        assertThat(settings.getOriginalUserOptions()).isEqualTo(user.getUserOptions());
    }

    @Test
    public void parseVersion() {
        UserSettings.Builder builder = new UserSettings.Builder(str -> parser);
        when(parser.parseStorage()).thenReturn(Collections.emptyMap());
        when(parser.getVersion()).thenReturn("1");//$NON-NLS-1$
        when(parser.getLocalStoragePersistence()).thenReturn(null);

        DbUser user = createUser();
        UserSettings settings = builder.fromUser(user).build();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getLocalStoragePersistenceVersion()).isEqualTo(1);
        assertThat(settings.isLocalStoragePersistedOnServer()).isTrue();
        assertThat(settings.getOriginalUserOptions()).isEqualTo(user.getUserOptions());
    }

    @Test
    public void parsePersistenceFlag() {
        UserSettings.Builder builder = new UserSettings.Builder(str -> parser);
        when(parser.parseStorage()).thenReturn(Collections.emptyMap());
        when(parser.getVersion()).thenReturn(null);
        when(parser.getLocalStoragePersistence()).thenReturn("false");//$NON-NLS-1$

        DbUser user = createUser();
        UserSettings settings = builder.fromUser(user).build();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getLocalStoragePersistenceVersion()).isNull();
        assertThat(settings.isLocalStoragePersistedOnServer()).isFalse();
        assertThat(settings.getOriginalUserOptions()).isEqualTo(user.getUserOptions());
    }

    @Test
    public void createFromSettings() {
        Map<String, String> map = new HashMap<>();
        map.put(MARKER, MARKER);
        UserSettings original = create()
                .withStorage(map)
                .withLocalStoragePersistence(false)
                .withLocalStoragePersistenceVersion(2)
                .build();
        UserSettings copy = create().fromSettings(original).build();

        assertThat(copy.isLocalStoragePersistedOnServer()).isEqualTo(original.isLocalStoragePersistedOnServer());
        assertThat(copy.getLocalStoragePersistedOnServer()).isEqualTo(original.getLocalStoragePersistedOnServer());
        assertThat(copy.getLocalStoragePersistenceVersion()).isEqualTo(original.getLocalStoragePersistenceVersion());
        assertThat(copy.getOriginalUserOptions()).isEqualTo(original.getOriginalUserOptions());
    }

    private DbUser createUser() {
        Map<String, String> options = new HashMap<>();
        options.put(MARKER, MARKER);
        DbUser user = new DbUser();
        user.setUserOptions(options);
        return user;
    }
}
