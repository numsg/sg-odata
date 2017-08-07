package com.sg.odata.service.datasource.query;

import org.apache.olingo.server.core.uri.queryoption.OrderByOptionImpl;

/**
 * Created by gaoqiang on 2017/4/26.
 */
public class JPAOrederByBuilder {

    /*
    * buildFromOrderBy
    * */
    public static  StringBuilder buildFromOrderBy(StringBuilder queryStringBuilder, OrderByOptionImpl orderByOptionImpl){
        if(orderByOptionImpl==null){
            return null;
        }
        String queryString = "  ORDER BY  " +  orderByOptionImpl.getText() ;
        queryStringBuilder.append(queryString);
        return queryStringBuilder;
    }

}
