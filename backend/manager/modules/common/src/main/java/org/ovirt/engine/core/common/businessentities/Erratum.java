package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.List;
import java.util.Objects;

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
 * @link http://www.katello.org/docs//user_guide/errata/index.html
 */
public class Erratum implements Queryable, BusinessEntity<String>, Nameable {

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

    public ErrataSeverity getSeverityOrDefault() {
        return severity == null ? ErrataSeverity.UNKNOWN : severity;
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
    public String getName() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                description,
                id,
                issued,
                packages,
                severity,
                solution,
                summary,
                title,
                type
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Erratum)) {
            return false;
        }
        Erratum other = (Erratum) obj;
        return Objects.equals(description, other.description)
                && Objects.equals(id, other.id)
                && Objects.equals(issued, other.issued)
                && Objects.equals(packages, other.packages)
                && severity == other.severity
                && Objects.equals(solution, other.solution)
                && Objects.equals(summary, other.summary)
                && Objects.equals(title, other.title)
                && type == other.type;
    }

    public enum ErrataType {
        BUGFIX("bugfix", "ORANGERED"), // $NON-NLS-1$ $NON-NLS-2$
        SECURITY("security", "DARKORANGE"), //$NON-NLS-1$ $NON-NLS-2$
        ENHANCEMENT("enhancement", "BLUE"); //$NON-NLS-1$ $NON-NLS-2$

        private String description;
        private String color;

        private ErrataType(String description, String color) {
            this.description = description;
            this.color = color;
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

        public String getColor() {
            return color;
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
