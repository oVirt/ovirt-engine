package org.ovirt.engine.core.bll.migration;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ConvergenceConfigProvider {

    /**
     * Policy ID to policy
     */
    private Map<Guid, MigrationPolicy> migrationPolicies;

    protected Logger log = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void initialize() {
        initMigrationPolicies(Config.<String>getValue(ConfigValues.MigrationPolicies));
    }

    public Collection<MigrationPolicy> getAllMigrationPolicies() {
        return new ArrayList<>(migrationPolicies.values());
    }

    public MigrationPolicy getMigrationPolicy(Guid id) {
        if (migrationPolicies.containsKey(id)) {
            return migrationPolicies.get(id);
        } else {
            // this can happen since there is no guarantee on the cluster object that the policy is not going to be
            // deleted later on. In that case return the "no policy".
            return migrationPolicies.get(NoMigrationPolicy.ID);
        }
    }

    void initMigrationPolicies(String policiesStr) {
        List<MigrationPolicy> policies = Collections.EMPTY_LIST;

        try {
            policies = parse(policiesStr);
        } catch (IOException e) {
            log.error("The provided migration policies: '{}' are not valid, ignoring", policiesStr);
        }

        // init it to an empty map even the parsing failed
        migrationPolicies = policies.stream().collect(toMap(MigrationPolicy::getId, identity()));

        // the null object
        migrationPolicies.put(NoMigrationPolicy.ID, new NoMigrationPolicy());

    }

    List<MigrationPolicy> parse(String policy) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, MigrationPolicy.class);
        return mapper.readValue(policy, type);
    }

}
