package org.ovirt.engine.ui.uicommonweb.builders;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((property1 == null) ? 0 : property1.hashCode());
        result = prime * result + ((property2 == null) ? 0 : property2.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestingBackendModel other = (TestingBackendModel) obj;
        if (property1 == null) {
            if (other.property1 != null)
                return false;
        } else if (!property1.equals(other.property1))
            return false;
        if (property2 == null) {
            if (other.property2 != null)
                return false;
        } else if (!property2.equals(other.property2))
            return false;
        return true;
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
