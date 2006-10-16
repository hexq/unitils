package org.unitils.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Filip Neven
 */
public class AnnotationUtils {


    /**
     * Returns the given class's declared fields that are marked with the given annotation
     *
     * @param clazz
     * @param annotation
     * @return A List containing fields annotated with the given annotation
     */
    public static <T extends Annotation> List<Field> getFieldsAnnotatedWith(Class clazz, Class<T> annotation) {
        if (Object.class.equals(clazz)) {
            return Collections.emptyList();
        } else {
            List<Field> annotatedFields = new ArrayList<Field>();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(annotation) != null) {
                    annotatedFields.add(field);
                }
            }
            annotatedFields.addAll(getFieldsAnnotatedWith(clazz.getSuperclass(), annotation));
            return annotatedFields;
        }
    }

    /**
     * Returns the given class's declared methods that are marked with the given annotation
     *
     * @param clazz
     * @param annotation
     * @return A List containing methods annotated with the given annotation
     */
    public static <T extends Annotation> List<Method> getMethodsAnnotatedWith(Class clazz, Class<T> annotation) {

        if (Object.class.equals(clazz)) {
            return Collections.emptyList();
        } else {
            List<Method> annotatedMethods = new ArrayList<Method>();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getAnnotation(annotation) != null) {
                    annotatedMethods.add(method);
                }
            }
            annotatedMethods.addAll(getMethodsAnnotatedWith(clazz.getSuperclass(), annotation));
            return annotatedMethods;
        }
    }

    /**
     * todo javadoc
     *
     * @param clazz
     * @param annotation
     * @return the
     */
    public static <T extends Annotation> T getClassAnnotation(Class<? extends Object> clazz, Class<T> annotation) {

        return clazz.getAnnotation(annotation);
    }


}
