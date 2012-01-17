package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MultilevelAdministrationsQueriesParameters")
public class MultilevelAdministrationsQueriesParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 7435982218559374552L;

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.LIST_IQUERYABLE;
    }

    public MultilevelAdministrationsQueriesParameters() {
    }
}
