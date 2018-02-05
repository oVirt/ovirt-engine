package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

@SuppressWarnings("serial")
public class GetFilteredAndSortedParameters extends QueryParametersBase {

    public GetFilteredAndSortedParameters() {
        super();
    }

    public GetFilteredAndSortedParameters(int maxResults) {
        super();
        this.maxResults = maxResults;
    }

    public GetFilteredAndSortedParameters(int maxResults, int pageNum) {
        super();
        this.maxResults = maxResults;
        this.pageNum = pageNum;
    }

    /**
     * Max number of desired results
     */
    private int maxResults;

    /**
     * Page number
     */
    private int pageNum;

    public int getMaxResults() {
        return maxResults;
    }
    public void setMaxResults(int maxResults) {
        this.maxResults= maxResults;
    }
    public int getPageNum() {
        return pageNum;
    }
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("maxResults", maxResults)
                .append("pageNum", pageNum);
    }
}
