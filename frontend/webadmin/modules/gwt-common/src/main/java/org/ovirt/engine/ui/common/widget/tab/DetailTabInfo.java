package org.ovirt.engine.ui.common.widget.tab;

import java.util.Objects;

import com.google.gwt.dom.client.Style.HasCssName;

public class DetailTabInfo {
    private String detailTitle;
    private int detailPriority;

    private HasCssName icon;

    public String getDetailTitle() {
        return detailTitle;
    }

    public void setDetailTitle(String detailTitle) {
        this.detailTitle = detailTitle;
    }

    public int getDetailPriority() {
        return detailPriority;
    }

    public void setDetailPriority(int detailPriority) {
        this.detailPriority = detailPriority;
    }

    public HasCssName getIcon() {
        return icon;
    }

    public void setIcon(HasCssName icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DetailTabInfo)) {
            return false;
        }
        DetailTabInfo other = (DetailTabInfo) obj;
        return Objects.equals(this.detailTitle, other.detailTitle)
                && Objects.equals(this.detailPriority, other.detailPriority)
                && Objects.equals(this.icon, other.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(detailTitle, detailPriority, icon);
    }
}
