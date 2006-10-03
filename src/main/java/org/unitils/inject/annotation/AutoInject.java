package org.unitils.inject.annotation;

import org.unitils.inject.PropertyAccessType;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Filip Neven
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoInject {

    String target() default "";

    PropertyAccessType propertyAccessType() default PropertyAccessType.DEFAULT;

}
