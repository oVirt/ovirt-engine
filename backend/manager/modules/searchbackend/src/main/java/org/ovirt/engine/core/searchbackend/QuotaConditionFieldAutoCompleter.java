package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;

public class QuotaConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    private static final String name = "NAME";
    private static final String storagePoolName = "STORAGEPOOLNAME";
    private static final String description = "DESCRIPTION";
    private static final String thresholdVdsGroupPercentage = "THRESHOLDVDSGROUPPERCENTAGE";
    private static final String thresholdStoragePercentage = "THRESHOLDSTORAGEPERCENTAGE";
    private static final String graceVdsGrouPercentage = "GRACEVDSGROUPPERCENTAGE";
    private static final String graceStoragePercentage = "GRACESTORAGEPERCENTAGE";

    private static final String enforcementType = "ENFORCEMENTTYPE";

    public QuotaConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        mVerbs.put(name, name);
        mVerbs.put(storagePoolName, storagePoolName);
        mVerbs.put(description, description);
        mVerbs.put(thresholdStoragePercentage, thresholdStoragePercentage);
        mVerbs.put(thresholdVdsGroupPercentage, thresholdVdsGroupPercentage);
        mVerbs.put(graceStoragePercentage, graceStoragePercentage);
        mVerbs.put(graceVdsGrouPercentage, graceVdsGrouPercentage);
        mVerbs.put(enforcementType, enforcementType);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(name, String.class);
        getTypeDictionary().put(storagePoolName, String.class);
        getTypeDictionary().put(description, String.class);
        getTypeDictionary().put(thresholdStoragePercentage, Integer.class);
        getTypeDictionary().put(thresholdVdsGroupPercentage, Integer.class);
        getTypeDictionary().put(graceStoragePercentage, Integer.class);
        getTypeDictionary().put(graceVdsGrouPercentage, Integer.class);
        getTypeDictionary().put(enforcementType, QuotaEnforcementTypeEnum.class);

        // building the ColumnName dict.
        columnNameDict.put(name, "quota_name");
        columnNameDict.put(storagePoolName, "storage_pool_name");
        columnNameDict.put(description, "description");
        columnNameDict.put(thresholdVdsGroupPercentage, "threshold_vds_group_percentage");
        columnNameDict.put(thresholdStoragePercentage, "threshold_storage_percentage");
        columnNameDict.put(graceStoragePercentage, "grace_storage_percentage");
        columnNameDict.put(graceVdsGrouPercentage, "grace_vds_group_percentage");
        columnNameDict.put(enforcementType, "quota_enforcement_type");

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        final Class<?> clazz = getTypeDictionary().get(fieldName);
        if (clazz == Integer.class) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        }
        return StringConditionRelationAutoCompleter.INSTANCE;
    }

    private final static EnumValueAutoCompleter enforcementTypeCompleter =
            new EnumValueAutoCompleter(QuotaEnforcementTypeEnum.class);

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if (enforcementType.equalsIgnoreCase(fieldName)) {
            return enforcementTypeCompleter;
        }
        return super.getFieldValueAutoCompleter(fieldName);
    }

}
