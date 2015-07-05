package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class PluralMessages {
    public EngineMessage getNetworkInUse(int numberOfEntities) {
        return singularOrPlural(numberOfEntities, EngineMessage.ACTION_TYPE_FAILED_NETWORK_IN_ONE_USE,
            EngineMessage.ACTION_TYPE_FAILED_NETWORK_IN_MANY_USES);
    }

    public ValidationResult getNetworkInUse(Collection<String> vmNames) {
        return getNetworkInUse(vmNames, EngineMessage.VAR__ENTITIES__VM, EngineMessage.VAR__ENTITIES__VMS);
    }

    /**
     * @param names names of entities using the network
     */
    public ValidationResult getNetworkInUse(Collection<String> names,
        EngineMessage singularEntitiesReplacement,
        EngineMessage pluralEntitiesReplacement) {
        int numberOfEntities = names.size();

        if (names.isEmpty()) {
            return ValidationResult.VALID;
        }

        boolean useSingular = userSingular(numberOfEntities);

        ArrayList<String> replacements = new ArrayList<>();

        final String ENTITIES_USING_NETWORK = "ENTITIES_USING_NETWORK";
        if (useSingular) {
            String name = names.iterator().next();
            replacements.add(ReplacementUtils.createSetVariableString(ENTITIES_USING_NETWORK, name));
            replacements.add(singularEntitiesReplacement.name());
        } else {
            replacements.addAll(ReplacementUtils.replaceWith(ENTITIES_USING_NETWORK, new ArrayList<>(names)));
            replacements.add(pluralEntitiesReplacement.name());

        }

        return new ValidationResult(getNetworkInUse(numberOfEntities), replacements);
    }

    public EngineMessage singularOrPlural(int numberOfEntities, EngineMessage singular, EngineMessage plural) {
        return userSingular(numberOfEntities) ? singular : plural;
    }

    private boolean userSingular(int numberOfEntities) {
        return numberOfEntities == 1;
    }
}
