package org.ovirt.engine.core.bll.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.common.config.ConfigValues.StorageQosSupported;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class DiskProfileHelperTest {

    private static final Guid STORAGE_DOMAIN_1 = Guid.newGuid();
    private static final Guid STORAGE_DOMAIN_2 = Guid.newGuid();
    private static final Guid USER_ENTITY_ID = Guid.newGuid();

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(StorageQosSupported, Version.getLast().getValue(),
                    true));
    @Mock
    private DiskProfileDao diskProfileDao;

    @Mock
    private PermissionDao permissionDao;

    @Spy
    @InjectMocks
    private DiskProfileHelper diskProfileHelper = new DiskProfileHelper();

    private DiskProfile diskProfile_a;
    private DiskProfile diskProfile_b;
    private DiskImage diskImage;
    private DbUser dbUser;
    private Map<DiskImage, Guid> map = new HashMap<>();

    @Before
    public void setUp() {
        dbUser = new DbUser();
        dbUser.setId(USER_ENTITY_ID);
        diskProfile_a = diskProfileHelper.createDiskProfile(STORAGE_DOMAIN_1, "disk_profile_A");
        diskProfile_b = diskProfileHelper.createDiskProfile(STORAGE_DOMAIN_2, "disk_profile_B");
        diskImage = createDisk();
        map.clear();

        when(diskProfileHelper.isDiskProfileParentEntityValid(any(DiskProfile.class), any(Guid.class)))
                .thenReturn(ValidationResult.VALID);
        doReturn(Guid.newGuid()).when(permissionDao)
                .getEntityPermissions(any(Guid.class),
                        any(ActionGroup.class),
                        any(Guid.class),
                        any(VdcObjectType.class));
        doReturn(Arrays.asList(diskProfile_a)).when(diskProfileDao).getAllForStorageDomain
                (STORAGE_DOMAIN_1);
        doReturn(Arrays.asList(diskProfile_b)).when(diskProfileDao).getAllForStorageDomain
                (STORAGE_DOMAIN_2);
    }

    private DiskImage createDisk() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setDiskAlias(RandomUtils.instance().nextString(10));
        disk.setActive(true);
        disk.setImageStatus(ImageStatus.OK);
        ArrayList<Guid> storageDomainIds = new ArrayList<>();
        storageDomainIds.add(STORAGE_DOMAIN_1);
        storageDomainIds.add(STORAGE_DOMAIN_2);
        disk.setStorageIds(storageDomainIds);
        return disk;
    }

    @Test
    public void setAndValidateWithoutDiskProfilesTest() {
        map.put(diskImage, STORAGE_DOMAIN_1);
        assertEquals(diskProfileHelper.setAndValidateDiskProfiles(map, Version.getLast(), dbUser),
                ValidationResult.VALID);
    }

    @Test
    public void setAndValidateSingleProfileTest() {
        diskImage.setDiskProfileId(diskProfile_a.getId());
        map.put(diskImage, STORAGE_DOMAIN_1);
        assertEquals(diskProfileHelper.setAndValidateDiskProfiles(map, Version.getLast(), dbUser),
                ValidationResult.VALID);
    }

    @Test
    public void setAndValidateMultipleStorageDomainsAndDiskProfilesTest() {
        diskImage.setDiskProfileIds(new ArrayList<Guid>(Arrays.asList(diskProfile_a.getId(), diskProfile_b.getId())));
        map.put(diskImage, STORAGE_DOMAIN_1);
        assertEquals(diskProfileHelper.setAndValidateDiskProfiles(map, Version.getLast(), dbUser),
                ValidationResult.VALID);
        diskImage.setDiskProfileIds(new ArrayList<Guid>(Arrays.asList(diskProfile_a.getId(), diskProfile_b.getId())));
        map.clear();
        map.put(diskImage, STORAGE_DOMAIN_2);
        assertEquals(diskProfileHelper.setAndValidateDiskProfiles(map, Version.getLast(), dbUser),
                ValidationResult.VALID);
    }

    @Test
    public void noDiskProfileAssociatedToStorageDomainFailureTest() {
        map.put(diskImage, STORAGE_DOMAIN_2);
        diskProfileHelper.setAndValidateDiskProfiles(map, Version.getLast(), dbUser);
        map.clear();
        map.put(diskImage, STORAGE_DOMAIN_1);
        assertThat(diskProfileHelper.setAndValidateDiskProfiles(map, Version.getLast(), dbUser), failsWith(EngineMessage
                .ACTION_TYPE_DISK_PROFILE_NOT_FOUND_FOR_STORAGE_DOMAIN));
    }

}
