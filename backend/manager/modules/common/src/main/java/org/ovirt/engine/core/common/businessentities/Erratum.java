package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * An Erratum (also referred to as Katello Erratum) is the wrapper of a software update to an operating system.
 * <p>
 * "Errata are updates between major releases. An Erratum is metadata about a group of [software] packages
 * that explains the importance of the package updates. Errata may be released individually on an as-needed
 * basis or aggregated as a minor release. There are three main types of errata:
 * <br><br>
 * Enhancement: the new packages contain one or more added features
 * Bugfix: the new packages contain one or more bug fixes
 * Security: the new packages fix one or more security vulnerabilities"
 * <p>
 * @see http://www.katello.org/docs//user_guide/errata/index.html
 */
public class Erratum implements IVdcQueryable, BusinessEntity<String> {

    private static final long serialVersionUID = 1297381071010863377L;

    private String id;
    private String title;
    private ErrataType type;
    private ErrataSeverity severity;
    private String summary;
    private String description;
    private String solution;
    private Date issued;
    private List<String> packages;

    public Erratum() {
        // default to unknown severity, bug
        setSeverity(ErrataSeverity.UNKNOWN);
        setType(ErrataType.BUGFIX);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ErrataType getType() {
        return type;
    }

    public void setType(ErrataType type) {
        this.type = type;
    }

    public ErrataSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ErrataSeverity severity) {
        this.severity = severity;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((issued == null) ? 0 : issued.hashCode());
        result = prime * result + ((packages == null) ? 0 : packages.hashCode());
        result = prime * result + ((severity == null) ? 0 : severity.hashCode());
        result = prime * result + ((solution == null) ? 0 : solution.hashCode());
        result = prime * result + ((summary == null) ? 0 : summary.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Erratum other = (Erratum) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (issued == null) {
            if (other.issued != null)
                return false;
        } else if (!issued.equals(other.issued))
            return false;
        if (packages == null) {
            if (other.packages != null)
                return false;
        } else if (!packages.equals(other.packages))
            return false;
        if (severity != other.severity)
            return false;
        if (solution == null) {
            if (other.solution != null)
                return false;
        } else if (!solution.equals(other.solution))
            return false;
        if (summary == null) {
            if (other.summary != null)
                return false;
        } else if (!summary.equals(other.summary))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    public enum ErrataType {
        BUGFIX("bugfix"), //$NON-NLS-1$
        SECURITY("security"), //$NON-NLS-1$
        ENHANCEMENT("enhancement"); //$NON-NLS-1$

        private String description;

        private ErrataType(String description) {
            this.description = description;
        }

        public static ErrataType byDescription(String description) {
            for (ErrataType t : values()) {
                if (t.description.equals(description)) {
                    return t;
                }
            }

            return null;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ErrataSeverity {
        CRITICAL("Critical"), //$NON-NLS-1$
        IMPORTANT("Important"), //$NON-NLS-1$
        MODERATE("Moderate"), //$NON-NLS-1$
        UNKNOWN("Unknown"); //$NON-NLS-1$

        private String description;

        private ErrataSeverity(String severity) {
            this.description = severity;
        }

        public static ErrataSeverity byDescription(String description) {
            for (ErrataSeverity s : values()) {
                if (s.description.equals(description)) {
                    return s;
                }
            }

            return null;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        ToStringBuilder builder = ToStringBuilder.forInstance(this);
        builder.append("id", id); //$NON-NLS-1$
        builder.append("title", title); //$NON-NLS-1$
        builder.append("type", type); //$NON-NLS-1$
        builder.append("severity", severity); //$NON-NLS-1$

        return builder.toString();
    }

}
