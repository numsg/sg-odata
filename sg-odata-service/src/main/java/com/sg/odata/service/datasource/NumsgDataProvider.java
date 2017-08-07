package com.sg.odata.service.datasource;


import com.sg.odata.service.datasource.query.JPAQuery;
import com.sg.odata.service.util.NumsgEntityUtil;
import com.sg.odata.service.util.NumsgReflectionUtil;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

/**
 * Created by gaoqiang on 2017/4/26.
 */

@Service
public class NumsgDataProvider {

    // 实体包的名字空间
    @Value("${odata.namespace}")
    public String NAMESPACE ;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public EntityCollection read(final UriInfo uriInfo, EdmEntitySet edmEntitySet) throws ODataApplicationException {
        try {
            EntityManager em = entityManagerFactory.createEntityManager();
            JPAQuery jPAQuery = JPAQueryBuilder.Build(uriInfo);
            Query query = em.createQuery(jPAQuery.getQueryString());
            if (jPAQuery.getLimitCount() > 0) {
                query.setMaxResults(jPAQuery.getLimitCount());
            }

            if (jPAQuery.getSkipCount() > 0) {
                query.setFirstResult(jPAQuery.getSkipCount());
            }

//            for (Map.Entry<String, Object> entry : jPAQuery.getQueryParams().entrySet()) {
//                query.setParameter(entry.getKey(), entry.getValue());
//            }

            List<Object> listEntity  = query.getResultList();
            return ConvertEntityToOdataEntitySet(uriInfo,listEntity, edmEntitySet);
        }catch (ODataException ex){
            throw new ODataApplicationException("" ,500, Locale.ENGLISH, ex);
        }
    }

    public Object readCount(final UriInfo uriInfo, EdmEntitySet edmEntitySet) throws ODataApplicationException {
        try {
            EntityManager em = entityManagerFactory.createEntityManager();
            JPAQuery jPAQuery = JPAQueryCountBuilder.Build(uriInfo);
            Query query = em.createQuery(jPAQuery.getQueryString());

            List<Object> listEntity  = query.getResultList();
            return listEntity.get(0);
        }catch (ODataException ex){
            throw new ODataApplicationException("" ,500, Locale.ENGLISH, ex);
        }
    }


    /*
    * 将数据库查询的结果集转换成可以展示的EntityCollection
    * */
    private EntityCollection ConvertEntityToOdataEntitySet(final UriInfo uriInfo, List<?> listEntity, EdmEntitySet edmEntitySet) throws ODataApplicationException {
        EntityCollection entitySet = new EntityCollection();
        for (Object obEntity : listEntity) {
            Class reflectObj = obEntity.getClass();
            Field[] fields = reflectObj.getDeclaredFields();
            Entity entity = new Entity();
            Object objId = null;
            try
            {
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    if(NumsgReflectionUtil.isHibernateId(fields[i])){
                        objId = fields[i].get(obEntity);
                    }
                    if(fields[i].getType().isLocalClass()){ //类
                        entity.addProperty(createComplex(fields[i].getName(), fields[i], fields[i].get(obEntity)));
                    }else if(fields[i].getType().isEnum()){ //枚举
                        entity.addProperty(createPrimitive(fields[i].getName(),fields[i].get(obEntity)));
                    }else {
                        entity.addProperty(createPrimitive(fields[i].getName(),fields[i].get(obEntity)));
                    }
                }
            }catch (IllegalAccessException ex){
                throw new ODataRuntimeException("ConvertEntityToOdataEntitySet: " + ex.getMessage(), ex);
            }
            entity.setId(createId(edmEntitySet.getName(), objId));
            entity.setType(reflectObj.getName());
            entitySet.getEntities().add(entity);
        }
        if(uriInfo.getCountOption()!=null){
            if(uriInfo.getCountOption().getText().equals("true") ){
                entitySet.setCount(Integer.parseInt(readCount(uriInfo, edmEntitySet).toString()));
            }
        }
        return entitySet;
    }

    public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType) {
        EntityCollection collection = getRelatedEntityCollection(entity, relatedEntityType);
        if (collection.getEntities().isEmpty()) {
            return null;
        }
        return collection.getEntities().get(0);
    }

    public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType, List<UriParameter> keyPredicates) {
        EntityCollection relatedEntities = getRelatedEntityCollection(entity, relatedEntityType);
        return NumsgEntityUtil.findEntity(relatedEntityType, relatedEntities, keyPredicates);
    }

    public EntityCollection getRelatedEntityCollection(Entity sourceEntity, EdmEntityType targetEntityType){
        EntityCollection navigationTargetEntityCollection = new EntityCollection();

//        FullQualifiedName relatedEntityFqn = targetEntityType.getFullQualifiedName();
//        String sourceEntityFqn = sourceEntity.getType();
        try
        {
            Class clazz =  NumsgReflectionUtil.newClass(sourceEntity.getType());
            for (Field field :clazz.getFields()){
                field.setAccessible(true);
                if(NumsgReflectionUtil.isHibernateId(field)){
                    navigationTargetEntityCollection.setId(createId(sourceEntity, field.getName(), targetEntityType.getName())); //"id"
                    break;
                }
            }
        }catch (ODataException ex){
            throw new ODataRuntimeException("error: " + ex.getMessage(), ex);
        }

        Entity entity = new Entity();
        Object objId = null;
        for(Field field : sourceEntity.getProperty(targetEntityType.getName()).getValue().getClass().getDeclaredFields()){
            field.setAccessible(true);
            try {
                Object filedValue = field.get(sourceEntity.getProperty(targetEntityType.getName()).getValue());
                entity.addProperty(createPrimitive(field.getName(), filedValue));
                if(NumsgReflectionUtil.isHibernateId(field)){
                    objId = filedValue;
                }
            }
            catch (IllegalAccessException ex){
                throw new ODataRuntimeException("error: " + ex.getMessage(), ex);
            }
        }

        entity.setId(createId(targetEntityType.getName(), objId));
        entity.setType(new FullQualifiedName(NAMESPACE, targetEntityType.getName()).getFullQualifiedNameAsString());
        navigationTargetEntityCollection.getEntities().add(entity);

        if (navigationTargetEntityCollection.getEntities().isEmpty()) {
            return null;
        }

        return navigationTargetEntityCollection;
    }

    private Property createComplex(final String name, Field field, final Object value) {
        ComplexValue complexValue=new ComplexValue();
        List<Property> complexProperties = complexValue.getValue();
        complexProperties.add(createPrimitive(name, value));

        return new Property(null, field.getName(), ValueType.COMPLEX, complexValue);
    }

    private Property createPrimitive(final String name, final Object value) {
        return new Property(null, name, ValueType.PRIMITIVE, value);
    }

    private URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }

    private URI createId(Entity entity, String idPropertyName, String navigationName) {
        try {
            StringBuilder sb = new StringBuilder(getEntitySetName(entity)).append("(");
            final Property property = entity.getProperty(idPropertyName);
            sb.append(property.asPrimitive()).append(")");
            if(navigationName != null) {
                sb.append("/").append(navigationName);
            }
            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entity, e);
        }
    }

    private String getEntitySetName(Entity entity) {
        return entity.getType();
    }
}
