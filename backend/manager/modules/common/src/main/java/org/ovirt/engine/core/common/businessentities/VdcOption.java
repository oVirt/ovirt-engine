package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "VdcOption")
@Entity
@Table(name = "vdc_options")
public class VdcOption implements Serializable {
    private static final long serialVersionUID = 5489148306184781421L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private int id;

    @Column(name = "option_name", length = 100, nullable = false)
    private String name;

    @Column(name = "option_value", length = 4000, nullable = false)
    private String value;

    @Column(name = "version", length = 40, nullable = false)
    private String version = "general";

    public VdcOption() {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        VdcOption other = ( VdcOption) obj;
        String otherName = other.getoption_name();
        String otherValue = other.getoption_value();
        if (name == null) {
            if (otherName != null)
                return false;
        } else if (!name.equals(otherName))
            return false;
        if (value == null) {
            if (otherValue != null)
                return false;
        } else if (!value.equals(otherValue))
            return false;
        return true;
    }

    public VdcOption(String option_name, String option_value, int option_id) {
        this.setoption_name(option_name);
        this.setoption_value(option_value);
        this.setoption_id(option_id);
    }

    public String getoption_name() {
        return this.name;
    }

    public void setoption_name(String value) {
        this.name = value;
    }

    public String getoption_value() {
        return this.value;
    }

    public void setoption_value(String value) {
        this.value = value;
    }

    public int getoption_id() {
        return this.id;
    }

    public void setoption_id(int value) {
        this.id = value;
    }

    public String getversion() {
        return version;
    }

    public void setversion(String value) {
        version = value;
    }
}
