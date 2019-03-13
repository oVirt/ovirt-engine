package org.ovirt.engine.core.bll.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.migration.ConvergenceConfig;
import org.ovirt.engine.core.common.migration.ConvergenceItem;
import org.ovirt.engine.core.common.migration.ConvergenceItemWithStallingLimit;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.JsonHelper;

public class ConvergenceConfigProviderTest {

    private static final Version VERSION = Version.getLast();

    private ConvergenceConfigProvider provider = new ConvergenceConfigProvider();

    @Test
    public void jsonInvalidJson() {
        provider.initMigrationPolicies("this is not a valid json", VERSION);
        MigrationPolicy policy = provider.getMigrationPolicy(Guid.newGuid(), VERSION);
        assertTrue(policy instanceof NoMigrationPolicy);
    }

    @Test
    public void jsonInvalidJsonLegacyVersion() {
        provider.initMigrationPolicies("this is not a valid json", Version.v4_2);
        MigrationPolicy policy = provider.getMigrationPolicy(Guid.newGuid(), Version.v4_2);
        assertTrue(policy instanceof NoMigrationPolicy);
    }

    @Test
    public void jsonProperlyFilled() throws IOException {
        Guid id = Guid.newGuid();

        List<MigrationPolicy> policies = new ArrayList<>();

        ConvergenceConfig config = new ConvergenceConfig();
        config.setInitialItems(Collections.singletonList(new ConvergenceItem("setDowntime2", 1)));
        config.setLastItems(Collections.singletonList(new ConvergenceItem("setDowntime3", 15)));
        config.setConvergenceItems(Collections.singletonList(new ConvergenceItemWithStallingLimit(1, "someAction", 12)));

        MigrationPolicy policy = new MigrationPolicy(id, "some name", "some long description", config);
        policies.add(policy);

        String json = JsonHelper.objectToJson(policies, false);

        provider.initMigrationPolicies(json, VERSION);

        // just basic sanity check
        assertEquals(policy.getName(), provider.getMigrationPolicy(id, VERSION).getName());
        assertEquals(policy.getDescription(), provider.getMigrationPolicy(id, VERSION).getDescription());
        assertTrue(provider.getMigrationPolicy(id, new Version()) instanceof NoMigrationPolicy);
    }
}

