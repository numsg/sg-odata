package com.sg.odata.service.processor;


import com.sg.odata.service.datasource.NumsgDataProvider;
import com.sg.odata.service.datasource.option.ExpandOptionBuilder;
import com.sg.odata.service.util.NumsgProcessorUtil;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by gaoqiang on 2017/4/26.
 */
public class NumsgEntityCollectionProcessor implements EntityCollectionProcessor,CountEntityCollectionProcessor {
    private OData odata;
    private ServiceMetadata serviceMetadata;
    private final NumsgDataProvider dataProvider;

    public NumsgEntityCollectionProcessor(final NumsgDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }


    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void countEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        // 1. 首先确认是进入哪个EntitySet
        final EdmEntitySet edmFirstEntitySet = NumsgProcessorUtil.getFirstEdmEntitySet(uriInfo.asUriInfoResource());

        //2.使用spring jpa-data 获取数据
        Object count = dataProvider.readCount(uriInfo, edmFirstEntitySet);

        InputStream serializedContent = new ByteArrayInputStream(count.toString().getBytes());

        //设置 response data, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, "application/json;charset:utf-8");
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        // 1. 首先确认是进入哪个EntitySet
        final EdmEntitySet edmFirstEntitySet = NumsgProcessorUtil.getFirstEdmEntitySet(uriInfo.asUriInfoResource());

        //2.使用spring jpa-data 获取数据
        EntityCollection entityCollection = dataProvider.read(uriInfo, edmFirstEntitySet);

        // 3.使用客户端请求的requestedContentType来创建序列化格式
        ODataSerializer serializer = odata.createSerializer(responseFormat);

        // 4. 使用system query options
        // 处理 $expand
        final ExpandOption expandOption = uriInfo.getExpandOption();
        for(Entity entity :entityCollection.getEntities()){
            ExpandOptionBuilder.handlerExpandOption(expandOption, entity ,edmFirstEntitySet, dataProvider);
        }

       // select 默认处理
        final SelectOption selectOption = uriInfo.getSelectOption();
        String selectList = odata.createUriHelper().buildContextURLSelectList(edmFirstEntitySet.getEntityType(), expandOption, selectOption);

        ContextURL contextUrl = ContextURL.with().entitySet(edmFirstEntitySet)
                .selectList(selectList)
                .suffix(ContextURL.Suffix.ENTITY).build();

        final String id = request.getRawBaseUri() + "/" + edmFirstEntitySet.getName();
        // 5.开始执行序列化
        InputStream serializedContent = serializer.entityCollection(serviceMetadata, edmFirstEntitySet.getEntityType(), entityCollection,
                EntityCollectionSerializerOptions.with()
                        .id(id)
                        .contextURL(contextUrl)
                        .count(uriInfo.getCountOption())
                        .expand(expandOption).select(selectOption)
                        .build()).getContent();

        // 5.设置response data，headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

//    private ContextURL getContextUrl(final EdmEntitySet entitySet, final boolean isSingleEntity,
//                                     final ExpandOption expand, final SelectOption select, final String navOrPropertyPath)
//            throws SerializerException {
//
//        return ContextURL.with().entitySet(entitySet)
//                .selectList(odata.createUriHelper().buildContextURLSelectList(entitySet.getEntityType(), expand, select))
//                .suffix(isSingleEntity ? ContextURL.Suffix.ENTITY : null)
//                .navOrPropertyPath(navOrPropertyPath)
//                .build();
//    }
//
//    public static boolean isODataMetadataNone(final ContentType contentType) {
//        return contentType.isCompatible(ContentType.APPLICATION_JSON)
//                && ContentType.VALUE_ODATA_METADATA_NONE.equalsIgnoreCase(
//                contentType.getParameter(ContentType.PARAMETER_ODATA_METADATA));
//    }
}
