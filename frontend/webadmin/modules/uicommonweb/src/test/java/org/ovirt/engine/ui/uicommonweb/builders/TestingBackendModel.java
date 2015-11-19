package org.ovirt.engine.ui.uicommonweb.builders;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

class TestingBackendModel implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 4381691759208069800L;

    private String property1;

    private String property2;

    public TestingBackendModel(String property1, String property2) {
        super();
        this.property1 = property1;
        this.property2 = property2;
    }

    public String getProperty1() {
        return property1;
    }

    public void setProperty1(String property1) {
        this.property1 = property1;
    }

    public String getProperty2() {
        return property2;
    }

    public void setProperty2(String property2) {
        this.property2 = property2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                property1,
                property2
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TestingBackendModel)) {
            return false;
        }
        TestingBackendModel other = (TestingBackendModel) obj;
        return Objects.equals(property1, other.property1)
                && Objects.equals(property2, other.property2);
    }

    @Override
    public Guid getId() {
        return null;
    }

    @Override
    public void setId(Guid id) {

    }

    @Override
    public String toString() {
        return "BackendModel [property1=" + property1 + ", property2=" + property2 + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
