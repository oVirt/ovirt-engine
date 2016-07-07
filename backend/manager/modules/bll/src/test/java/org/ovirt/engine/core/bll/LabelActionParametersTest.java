package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;

public class LabelActionParametersTest {

    @Test
    public void validateLongNameFails() throws Exception {
        Label label = createLabel("veryveryveryveryverylongnamewhichiswaytoolongforalabel");

        validateLabelNameLength(label, false);
    }

    @Test
    public void validateEmptyNameFails() throws Exception {
        Label label = createLabel("");

        validateLabelNameLength(label, false);
    }

    @Test
    public void validateValidName() throws Exception {
        Label label = createLabel("validLabelName");

        validateLabelNameLength(label, true);
    }

    private void validateLabelNameLength(Label label, boolean isValidLabelName) {
        LabelActionParameters parameters = new LabelActionParameters(label);

        List<String> validationMessages = ValidationUtils.validateInputs(new ArrayList<>(), parameters);

        if(isValidLabelName){
            Assertions.assertThat(validationMessages).isEmpty();
        }
        else {
            Assertions.assertThat(validationMessages)
                    .contains(EngineMessage.AFFINITY_LABEL_NAME_SIZE_INVALID.name())
                    .contains("$min 1")
                    .contains("$max " + BusinessEntitiesDefinitions.TAG_NAME_SIZE);
        }
    }

    private Label createLabel(String labelName) {
        return new LabelBuilder()
                .name(labelName)
                .id(Guid.newGuid())
                .build();
    }
}
