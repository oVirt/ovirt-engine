package org.ovirt.engine.core.common.businessentities.gluster;

public enum GeoRepCrawlStatus {

    CHANGELOG_CRAWL("Changelog Crawl"),
    UNKNOWN,
    NOT_APPLICABLE("N/A"),
    HYBRID_CRAWL("Hybrid Crawl"),
    HISTORY_CRAWL("History Crawl");

    private String statusMsg;

    private GeoRepCrawlStatus(String status) {
        statusMsg = status;
    }

    private GeoRepCrawlStatus() {
        statusMsg = this.name();
    }

    public String value() {
        return statusMsg;
    }

    public static GeoRepCrawlStatus from(String status) {
        for (GeoRepCrawlStatus crawlStatus : values()) {
            if (crawlStatus.value().equalsIgnoreCase(status)) {
                return crawlStatus;
            }
        }
        return GeoRepCrawlStatus.UNKNOWN;
    }
}
