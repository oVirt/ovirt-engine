package org.ovirt.engine.core.config.entity.helper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.migration.ConvergenceConfig;
import org.ovirt.engine.core.common.migration.ConvergenceItem;
import org.ovirt.engine.core.common.migration.ConvergenceItemWithStallingLimit;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.utils.JsonHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

public class MigrationPoliciesValueHelper extends StringValueHelper {

    @Override
    public ValidationResult validate(ConfigKey key, String value) {
        if (StringUtils.isEmpty(value)) {
            return new ValidationResult(false, "The provided value can not be empty");
        }

        ObjectMapper mapper = new ObjectMapper();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, MigrationPolicy.class);
        try {
            mapper.readValue(value, type);
        } catch (IOException e) {
            return new ValidationResult(false,
                    String.format("The provided migration policy can not be parsed: '%s'. A valid format is: '%s'",
                            e.getMessage(), getExamplePolicy()));
        }

        return new ValidationResult(true);
    }

    String getExamplePolicy() {
        ConvergenceConfig config = new ConvergenceConfig();
        config.setInitialItems(Arrays.asList(new ConvergenceItem("setDowntime", 100)));
        config.setConvergenceItems(Arrays.asList(new ConvergenceItemWithStallingLimit(1, "setDowntime", 200)));
        config.setLastItems(Arrays.asList(new ConvergenceItem("abort")));
        List<MigrationPolicy> policies = Arrays.asList(new MigrationPolicy(Guid.Empty, "name of the policy", "description", config));
        try {
            return JsonHelper.objectToJson(policies, false);
        } catch (IOException e) {
            // if this fails, than we really want to fail it with an exception
            throw new RuntimeException(e);
        }
    }
}
