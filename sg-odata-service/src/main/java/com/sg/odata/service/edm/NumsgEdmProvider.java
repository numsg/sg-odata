package com.sg.odata.service.edm;

import com.sg.odata.service.enumx.EntityRelationType;
import com.sg.odata.service.util.NumsgEntityTypeUtil;
import com.sg.odata.service.util.NumsgReflectionUtil;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;
import org.hibernate.jpa.internal.metamodel.AbstractType;
import org.hibernate.jpa.internal.metamodel.EntityTypeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gaoqiang on 2017/4/26.
 */

@Service
public class NumsgEdmProvider extends CsdlAbstractEdmProvider {

    // 实体包的名字空间
    @Value("${odata.namespace}")
    public String NAMESPACE ;

    // EDM Container
    public final String CONTAINER_NAME = "Container";

    public FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) throws ODataException {

        EntityType entityType = NumsgEntityTypeUtil.getEntityTypeByEntityFullTypeName(entityManagerFactory.getMetamodel()
                ,entityTypeName.getFullQualifiedNameAsString());

        return new CsdlEntityType()
                .setName(entityType.getName())
                .setKey(Arrays.asList(
                        new CsdlPropertyRef().setName(NumsgEntityTypeUtil.getId(entityType))))
                .setProperties(
                        createCsdlPropertyList(entityType)
                ).setNavigationProperties(
                        createNavigationProperties(entityType)
                );

    }

    /*
    * 创建CsdlEntity的普通属性
    * @eg new CsdlProperty().setName("ModelYear").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
    *   .setMaxLength(4),
    * */
    private List<CsdlProperty> createCsdlPropertyList(EntityType entityType) throws ODataException {

        List<CsdlProperty> csdlPropertyList = new ArrayList<>();
        Class clazz = NumsgReflectionUtil.newClass(((AbstractType)entityType).getTypeName());

        for(Field field: clazz.getDeclaredFields() ) {
            if(NumsgReflectionUtil.isHibernateCloumn(field)){

                CsdlProperty csdlProperty = new CsdlProperty();
                if(NumsgEntityTypeUtil.getEdmType(field.getType())!=null){
                    csdlProperty.setName(field.getName()).setType(NumsgEntityTypeUtil.getEdmType(field.getType()).getFullQualifiedName());
                }else {
                    if(field.getType().isEnum()) {
                        csdlProperty.setName(field.getName()).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
                    }
                    else {
                        csdlProperty.setName(field.getName()).setType(NumsgEntityTypeUtil.getEdmType(field.getType()).getFullQualifiedName());
                    }
                }
                csdlPropertyList.add(csdlProperty);
            }
        }

        return  csdlPropertyList;
    }

    /*
    * 创建CsdlEntity的导航属性
    * @eg   new CsdlNavigationProperty().setName("Manufacturer").setType(ET_MANUFACTURER)
    * */
    private List<CsdlNavigationProperty> createNavigationProperties(EntityType entityType) throws ODataException {
        List<CsdlNavigationProperty> csdlNavigationPropertyList = new ArrayList<>();
        Class clazz = NumsgReflectionUtil.newClass(((AbstractType)entityType).getTypeName());

        for(Field field: clazz.getDeclaredFields() ) {
            if(NumsgReflectionUtil.getHibernateRelationType(field).equals(EntityRelationType.ManyToMany)
                    || NumsgReflectionUtil.getHibernateRelationType(field).equals(EntityRelationType.OneToMany)){
                CsdlNavigationProperty csdlNavigationProperty = new CsdlNavigationProperty();
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                csdlNavigationProperty.setName(field.getName()).setType(new FullQualifiedName(pt.getActualTypeArguments()[0].getTypeName())).setCollection(true);
                csdlNavigationPropertyList.add(csdlNavigationProperty);
                continue;
            }else if(NumsgReflectionUtil.getHibernateRelationType(field).equals(EntityRelationType.ManyToOne)
                    ||NumsgReflectionUtil.getHibernateRelationType(field).equals(EntityRelationType.OneToOne)){
                CsdlNavigationProperty csdlNavigationProperty = new CsdlNavigationProperty();
                csdlNavigationProperty.setName(field.getName()).setType(new FullQualifiedName(NAMESPACE, field.getName()));
                csdlNavigationPropertyList.add(csdlNavigationProperty);
                continue;
            }
        }
        return  csdlNavigationPropertyList;
    }

    /*
    * CsdlComplexType
    * */
    @Override
    public CsdlComplexType getComplexType(final FullQualifiedName complexTypeName) throws ODataException {
        Class clazz = NumsgReflectionUtil.newClass(complexTypeName.getFullQualifiedNameAsString());
        CsdlComplexType csdlComplexType = new CsdlComplexType();
        List<CsdlProperty> csdlPropertyList = new ArrayList<>();
        for(Field field: clazz.getDeclaredFields() ) {
            csdlPropertyList.add(new CsdlProperty()
                    .setName(field.getName())
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()));

        }
        csdlComplexType.setName(complexTypeName.getName()).setProperties(csdlPropertyList);
        return csdlComplexType;
    }

    /*
    * 获取CsdlEntitySet
    * */
    @Override
    public CsdlEntitySet getEntitySet(final FullQualifiedName entityContainer, final String entitySetName)
            throws ODataException {
        CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
        if (!CONTAINER_FQN.equals(entityContainer)) {
            throw new ODataException("Container not equal");
        }
        Metamodel metamodel = entityManagerFactory.getMetamodel();
        for (EntityType entityType : metamodel.getEntities()){
            if(entitySetName.toLowerCase().contains(((EntityTypeImpl)entityType).getName().toLowerCase())) {
                return CreateCsdlEntitySet(entityType,entitySetName);
            }
        }
        throw new ODataException("Not find entitySet");
    }

    /*
    * 创建CsdlEntitySet
    * */
    private CsdlEntitySet CreateCsdlEntitySet(EntityType entityType,String entitySetName) throws ODataException {
        List<CsdlNavigationPropertyBinding> csdlNavigationPropertyBindingList = new ArrayList<>();
        Class clazz = NumsgReflectionUtil.newClass(((AbstractType)entityType).getTypeName());
        CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
        for(Field field: clazz.getDeclaredFields() ) {
            if(NumsgReflectionUtil.getHibernateRelationType(field).equals(EntityRelationType.ManyToMany)
                    || NumsgReflectionUtil.getHibernateRelationType(field).equals(EntityRelationType.OneToMany)){
                CsdlNavigationPropertyBinding csdlNavigationPropertyBinding =new CsdlNavigationPropertyBinding();
                csdlNavigationPropertyBinding.setPath(field.getName()).setTarget(CONTAINER_FQN.getFullQualifiedNameAsString() + "/" + field.getName());
                csdlNavigationPropertyBindingList.add(csdlNavigationPropertyBinding);
                continue;
            }else if(NumsgReflectionUtil.getHibernateRelationType(field).equals(EntityRelationType.ManyToOne)
                    ||NumsgReflectionUtil.getHibernateRelationType(field).equals(EntityRelationType.OneToOne)){
                CsdlNavigationPropertyBinding csdlNavigationPropertyBinding =new CsdlNavigationPropertyBinding();
                csdlNavigationPropertyBinding.setPath(field.getName()).setTarget(CONTAINER_FQN.getFullQualifiedNameAsString() + "/" + field.getName()+"s");
                csdlNavigationPropertyBindingList.add(csdlNavigationPropertyBinding);
                continue;
            }
        }
        CsdlEntitySet csdlEntitySet = new CsdlEntitySet();
        csdlEntitySet
                .setName(entitySetName)
                .setType( new FullQualifiedName(NAMESPACE, entitySetName.substring(0,entitySetName.length()-1)) )
                .setNavigationPropertyBindings(csdlNavigationPropertyBindingList);
        return  csdlEntitySet;

    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);
        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
        Metamodel metamodel = entityManagerFactory.getMetamodel();
        for (EntityType entityType : metamodel.getEntities()) {
            String entityName = ((EntityTypeImpl) entityType).getTypeName().substring(((EntityTypeImpl) entityType).getTypeName().lastIndexOf('.')+1);
            entityTypes.add(getEntityType( new FullQualifiedName(NAMESPACE, entityName)));
        }
        schema.setEntityTypes(entityTypes);
        // ComplexTypes
//        List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
//        complexTypes.add(getComplexType(new FullQualifiedName("com.numsg.odata.entity.metadata.Gender")));
//        schema.setComplexTypes(complexTypes);
        // EntityContainer
        schema.setEntityContainer(getEntityContainer());
        schemas.add(schema);

        return schemas;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        CsdlEntityContainer container = new CsdlEntityContainer();
        CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
        container.setName(CONTAINER_FQN.getName());

        // EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        container.setEntitySets(entitySets);
        Metamodel metamodel = entityManagerFactory.getMetamodel();
        for (EntityType entityType : metamodel.getEntities()) {
            String entityName = ((EntityTypeImpl) entityType).getTypeName().substring(((EntityTypeImpl) entityType).getTypeName().lastIndexOf('.')+1);
            entitySets.add(getEntitySet(CONTAINER_FQN, entityName+"s"));
        }
        return container;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName)
            throws ODataException {
        CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
        if (entityContainerName == null || CONTAINER_FQN.equals(entityContainerName)) {
            return new CsdlEntityContainerInfo().setContainerName(CONTAINER_FQN);
        }
        throw new ODataException("Get EntityContainerInfo null");
    }

}
