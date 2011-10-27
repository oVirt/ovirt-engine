package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.ActionGroup;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetEntitiesWithPermittedActionParameters")
public class GetEntitiesWithPermittedActionParameters extends VdcQueryParametersBase {

    @XmlElement(name = "ActionGroup")
    private ActionGroup actionGroup;

    public GetEntitiesWithPermittedActionParameters() {
    }

    public void setActionGroup(ActionGroup actionGroup) {
        this.actionGroup = actionGroup;
    }

    public ActionGroup getActionGroup() {
        return actionGroup;
    }

}
