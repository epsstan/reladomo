package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces;

import com.gs.fw.common.mithra.generator.metamodel.ObjectType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReladomoInterface
{
    // class name is inferred from the class to which this annotation is applied

    String packageName();

    ObjectType.Enums objectType() default ObjectType.Enums.READ_ONLY;

    Class[] superInterfaces() default {};

    String[] imports() default {};
}
