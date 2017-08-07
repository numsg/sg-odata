package com.sg.odata.service.datasource;

import com.sg.odata.service.datasource.query.JPAFilterBuilder;
import com.sg.odata.service.datasource.query.JPAKeyBuilder;
import com.sg.odata.service.datasource.query.JPAOrederByBuilder;
import com.sg.odata.service.datasource.query.JPAQuery;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.core.uri.queryoption.FilterOptionImpl;
import org.apache.olingo.server.core.uri.queryoption.OrderByOptionImpl;

import java.util.List;
import java.util.Locale;

/**
 * Created by numsg on 2017/3/15.
 */
public class JPAQueryBuilder {

    public static JPAQuery Build(final UriInfo uriInfo) throws ODataException {

        JPAQuery jpaQuery =new JPAQuery("", null);
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        UriResource uriResource = resourceParts.get(0); // the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        if (!(uriResource instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Only EntitySet is supported",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }
        StringBuilder queryStringBuilder = new StringBuilder();

        // SELECT [DISTINCT]
        queryStringBuilder.append("SELECT ");
        queryStringBuilder.append("DISTINCT e1 ");
        queryStringBuilder.append(" FROM ").append(edmEntitySet.getEntityType().getName().toUpperCase()).append(" e1 where 1=1  ");

        //1.主键查询
        JPAKeyBuilder.buildFromKey(queryStringBuilder,uriResourceEntitySet);

        //2.SystemQueryOption
        for(SystemQueryOption systemQueryOption : uriInfo.getSystemQueryOptions()){
            if(systemQueryOption instanceof FilterOptionImpl){
                JPAFilterBuilder.buildFromFilter(queryStringBuilder, (FilterOptionImpl)systemQueryOption);
            }
        }
        //3. OrderByOption
        OrderByOptionImpl orderByOptionImpl = (OrderByOptionImpl)uriInfo.getOrderByOption();
        JPAOrederByBuilder.buildFromOrderBy(queryStringBuilder ,orderByOptionImpl);

        jpaQuery.setQueryString(queryStringBuilder.toString());
        if(uriInfo.getTopOption()!=null)
            jpaQuery.setLimitCount(uriInfo.getTopOption().getValue());
        if(uriInfo.getSkipOption()!=null)
            jpaQuery.setSkipCount(uriInfo.getSkipOption().getValue());
        return jpaQuery;
    }

}
