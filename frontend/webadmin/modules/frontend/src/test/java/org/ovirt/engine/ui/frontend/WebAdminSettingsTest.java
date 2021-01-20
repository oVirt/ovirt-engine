package org.ovirt.engine.ui.frontend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.ui.frontend.WebAdminSettings.Builder.create;
import static org.ovirt.engine.ui.frontend.WebAdminSettings.WEB_ADMIN;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class WebAdminSettingsTest {
    @Mock
    WebAdminSettings.Parser parser;

    public static String EMPTY_MAP = "{}"; //$NON-NLS-1$

    @Test
    public void defaultSettings() {
        checkDefaultOptions(WebAdminSettings.defaultSettings());
    }

    private void checkDefaultOptions(WebAdminSettings settings) {
        assertThat(settings).isNotNull();
        assertThat(settings.isLocalStoragePersistedOnServer()).isTrue();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getOriginalUserOptions()).isEqualTo(UserProfileProperty.builder()
                .withTypeJson()
                .withName(WEB_ADMIN)
                .withContent(EMPTY_MAP)
                .build());
        assertThat(settings.getLocalStoragePersistenceVersion()).isEqualTo(WebAdminSettings.CURRENT_VERSION);
    }

    @Test
    public void defaultSettingsForNullProperty() {
        checkDefaultOptions(WebAdminSettings.from(null));
    }

    @Test
    public void defaultSettingsForNonWebAdminProperty() {
        UserProfileProperty nonWebAdminProperty =
                UserProfileProperty.builder().from(createWebAdminProperty()).withName(EMPTY_MAP).build();
        WebAdminSettings.Builder builder = new WebAdminSettings.Builder(str -> parser);
        checkDefaultOptions(builder.fromProperty(nonWebAdminProperty).build());
    }

    @Test
    public void defaultSettingsForNonJsonProperty() {
        UserProfileProperty nonWebAdminProperty =
                UserProfileProperty.builder().from(createWebAdminProperty()).withTypeSsh().build();
        WebAdminSettings.Builder builder = new WebAdminSettings.Builder(str -> parser);
        checkDefaultOptions(builder.fromProperty(nonWebAdminProperty).build());
    }

    @Test
    public void parseEmptyStorage() {
        WebAdminSettings.Builder builder = new WebAdminSettings.Builder(str -> parser);
        when(parser.parseStorage()).thenReturn(Collections.emptyMap());
        when(parser.getVersion()).thenReturn(null);
        when(parser.getLocalStoragePersistence()).thenReturn(null);

        UserProfileProperty property = createWebAdminProperty();
        WebAdminSettings settings = builder.fromProperty(property).build();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getLocalStoragePersistenceVersion()).isNull();
        assertThat(settings.isLocalStoragePersistedOnServer()).isTrue();
        assertThat(settings.getOriginalUserOptions()).isEqualTo(property);
    }

    @Test
    public void parseVersion() {
        WebAdminSettings.Builder builder = new WebAdminSettings.Builder(str -> parser);
        when(parser.parseStorage()).thenReturn(Collections.emptyMap());
        when(parser.getVersion()).thenReturn("1");//$NON-NLS-1$
        when(parser.getLocalStoragePersistence()).thenReturn(null);

        UserProfileProperty property = createWebAdminProperty();
        WebAdminSettings settings = builder.fromProperty(property).build();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getLocalStoragePersistenceVersion()).isEqualTo(1);
        assertThat(settings.isLocalStoragePersistedOnServer()).isTrue();
        assertThat(settings.getOriginalUserOptions()).isEqualTo(property);
    }

    @Test
    public void parsePersistenceFlag() {
        WebAdminSettings.Builder builder = new WebAdminSettings.Builder(str -> parser);
        when(parser.parseStorage()).thenReturn(Collections.emptyMap());
        when(parser.getVersion()).thenReturn(null);
        when(parser.getLocalStoragePersistence()).thenReturn("false");//$NON-NLS-1$

        UserProfileProperty property = createWebAdminProperty();
        WebAdminSettings settings = builder.fromProperty(property).build();
        assertThat(settings.getLocalStoragePersistedOnServer()).isEmpty();
        assertThat(settings.getLocalStoragePersistenceVersion()).isNull();
        assertThat(settings.isLocalStoragePersistedOnServer()).isFalse();
        assertThat(settings.getOriginalUserOptions()).isEqualTo(property);
    }

    @Test
    public void createFromSettings() {
        Map<String, String> map = new HashMap<>();
        map.put(EMPTY_MAP, EMPTY_MAP);
        WebAdminSettings original = create()
                .withStorage(map)
                .withLocalStoragePersistence(false)
                .withLocalStoragePersistenceVersion(2)
                .build();
        WebAdminSettings copy = create().fromSettings(original).build();

        assertThat(copy.isLocalStoragePersistedOnServer()).isEqualTo(original.isLocalStoragePersistedOnServer());
        assertThat(copy.getLocalStoragePersistedOnServer()).isEqualTo(original.getLocalStoragePersistedOnServer());
        assertThat(copy.getLocalStoragePersistenceVersion()).isEqualTo(original.getLocalStoragePersistenceVersion());
        assertThat(copy.getOriginalUserOptions()).isEqualTo(original.getOriginalUserOptions());
    }

    private UserProfileProperty createWebAdminProperty() {
        return UserProfileProperty.builder()
                .withNewId()
                .withUserId(Guid.newGuid())
                .withName(WEB_ADMIN)
                .withContent(EMPTY_MAP)
                .withTypeJson()
                .build();
    }
}
