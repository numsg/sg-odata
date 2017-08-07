package com.sg.odata.service.util;

import com.sg.odata.service.enumx.EntityRelationType;
import org.apache.olingo.commons.api.ex.ODataException;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Created by gaoqiang on 2017/4/26.
 */
public class NumsgReflectionUtil {

    /*
    * 创建一个实例
    * */
    public static <T> Class<T> newClass(String className) throws ODataException {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ODataException("Cannot create class of: " + className, e);
        }
    }

    /*
    * 判断是否是数据库表列
    * */
    public static boolean isHibernateCloumn(Field field){
        Annotation[] annotations = field.getDeclaredAnnotations();
        if(annotations==null){
            return false;
        }
        for(Annotation annotation : annotations){
            if(annotation instanceof Column) {
                return true;
            }
        }
        return  false;
    }

    /*
    * 判断是否是数据库表列
    * */
    public static boolean isHibernateId(Field field){
        Annotation[] annotations = field.getDeclaredAnnotations();
        if(annotations==null){
            return false;
        }
        for(Annotation annotation : annotations){
            if(annotation instanceof Id) {
                return true;
            }
        }
        return  false;
    }
    /*
    * 判断是否是数据库表列
    * */
    public static EntityRelationType getHibernateRelationType(Field field){
        Annotation[] annotations = field.getDeclaredAnnotations();
        if(annotations==null){
            return EntityRelationType.None;
        }
        for(Annotation annotation : annotations){
            if(annotation instanceof OneToMany) {
                return EntityRelationType.OneToMany;
            }else  if(annotation instanceof ManyToMany){
                return EntityRelationType.ManyToMany;
            }
            else  if(annotation instanceof OneToOne){
                return EntityRelationType.OneToOne;
            }
            else  if(annotation instanceof ManyToOne){
                return EntityRelationType.ManyToOne;
            }
        }
        return  EntityRelationType.None;
    }

}
