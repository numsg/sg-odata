package com.sg.odata.service.datasource.query;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import java.util.List;

/**
 * Created by gaoqiang on 2017/4/26.
 */
public class JPAKeyBuilder {

    /*
    * buildFromKey
    * */
    public static  StringBuilder buildFromKey(StringBuilder queryStringBuilder, UriResourceEntitySet uriResourceEntitySet){
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType entityType = edmEntitySet.getEntityType();
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        for (final UriParameter key : keyPredicates) {
            queryStringBuilder.append(" AND " +  key.getName() + " = " + key.getText());
        }
        return queryStringBuilder;
    }

}
