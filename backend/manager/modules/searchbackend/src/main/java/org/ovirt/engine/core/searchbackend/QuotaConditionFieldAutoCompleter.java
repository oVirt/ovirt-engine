package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;

public class QuotaConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String STORAGEPOOLNAME = "STORAGEPOOLNAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String thresholdVdsGroupPercentage = "THRESHOLDVDSGROUPPERCENTAGE";
    public static final String thresholdStoragePercentage = "THRESHOLDSTORAGEPERCENTAGE";
    public static final String graceVdsGrouPercentage = "GRACEVDSGROUPPERCENTAGE";
    public static final String graceStoragePercentage = "GRACESTORAGEPERCENTAGE";

    private static final String enforcementType = "ENFORCEMENTTYPE";

    public QuotaConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        mVerbs.add(NAME);
        mVerbs.add(STORAGEPOOLNAME);
        mVerbs.add(DESCRIPTION);
        mVerbs.add(thresholdStoragePercentage);
        mVerbs.add(thresholdVdsGroupPercentage);
        mVerbs.add(graceStoragePercentage);
        mVerbs.add(graceVdsGrouPercentage);
        mVerbs.add(enforcementType);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(STORAGEPOOLNAME, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(thresholdStoragePercentage, Integer.class);
        getTypeDictionary().put(thresholdVdsGroupPercentage, Integer.class);
        getTypeDictionary().put(graceStoragePercentage, Integer.class);
        getTypeDictionary().put(graceVdsGrouPercentage, Integer.class);
        getTypeDictionary().put(enforcementType, QuotaEnforcementTypeEnum.class);

        // building the ColumnName dict.
        columnNameDict.put(NAME, "quota_name");
        columnNameDict.put(STORAGEPOOLNAME, "storage_pool_name");
        columnNameDict.put(DESCRIPTION, "description");
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
