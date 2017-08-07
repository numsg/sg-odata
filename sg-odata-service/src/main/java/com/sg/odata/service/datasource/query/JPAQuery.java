package com.sg.odata.service.datasource.query;

import java.util.Map;

/**
 * Created by gaoqiang on 2017/4/26.
 */

/*
* JPAQuery对象
* */
public class JPAQuery {

    private String queryString;
    private Map<String, Object> queryParams;

    private int limitCount;
    private int skipCount;

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public int getLimitCount() {
        return limitCount;
    }

    public void setLimitCount(int limitCount) {
        this.limitCount = limitCount;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(int skipCount) {
        this.skipCount = skipCount;
    }

    public JPAQuery(String queryString, Map<String, Object> queryParams, int limitCount, int skipCount) {
        this.queryString = queryString;
        this.queryParams = queryParams;
        this.limitCount = limitCount;
        this.skipCount = skipCount;
    }

    public JPAQuery(String queryString, Map<String, Object> queryParams) {
        this(queryString, queryParams, -1, -1);
    }

    @Override
    public String toString() {
        return queryString + ", params=" + queryParams;
    }

}
