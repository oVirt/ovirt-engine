package org.ovirt.engine.core.bll.migration;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

@Singleton
public class ConvergenceConfigProvider {

    /** Compatibility version to policy ID to policy */
    private Map<Version, Map<Guid, MigrationPolicy>> migrationPoliciesByVersion = new HashMap<>();

    protected Logger log = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void initialize() {
        for (Version version : Version.ALL) {
            initMigrationPolicies(Config.getValue(ConfigValues.MigrationPolicies, version.toString()), version);
        }
    }

    private Map<Guid, MigrationPolicy> getMigrationPolicies(Version version) {
        Map<Guid, MigrationPolicy> migrationPolicies = migrationPoliciesByVersion.get(version);
        if (migrationPolicies == null) {
            migrationPolicies = Collections.singletonMap(NoMigrationPolicy.ID, new NoMigrationPolicy());
        }
        return migrationPolicies;
    }

    public Map<Version, List<MigrationPolicy>> getAllMigrationPolicies() {
        Map<Version, List<MigrationPolicy>> result = new HashMap<>();
        for (Version version : Version.ALL) {
            result.put(version, new ArrayList<>(getMigrationPolicies(version).values()));
        }
        return result;
    }

    public MigrationPolicy getMigrationPolicy(Guid id, Version compatibilityVersion) {
        Map<Guid, MigrationPolicy> migrationPolicies = getMigrationPolicies(compatibilityVersion);
        if (migrationPolicies.containsKey(id)) {
            return migrationPolicies.get(id);
        } else {
            // this can happen since there is no guarantee on the cluster object that the policy is not going to be
            // deleted later on. In that case return the "no policy".
            return migrationPolicies.get(NoMigrationPolicy.ID) != null ?
                    migrationPolicies.get(NoMigrationPolicy.ID) :
                    new NoMigrationPolicy();
        }
    }

    void initMigrationPolicies(String policiesStr, Version version) {
        List<MigrationPolicy> policies = Collections.emptyList();

        try {
            policies = parse(policiesStr);
        } catch (IOException e) {
            log.error("The provided migration policies: '{}' are not valid, ignoring", policiesStr);
        }

        // init it to an empty map even the parsing failed
        Map<Guid, MigrationPolicy> migrationPolicies =
                policies.stream().collect(toMap(MigrationPolicy::getId, identity()));

        // the null object
        if (version.less(Version.v4_3)) {
            migrationPolicies.put(NoMigrationPolicy.ID, new NoMigrationPolicy());
        }

        migrationPoliciesByVersion.put(version, migrationPolicies);
    }

    List<MigrationPolicy> parse(String policy) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, MigrationPolicy.class);
        return mapper.readValue(policy, type);
    }

}
