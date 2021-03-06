package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.InterfaceResource;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ObjectResource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Grouping of multiple classes

//todo : add attributes for reladomo list
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReladomoListSpec
{
    ObjectResource[] resources();
    InterfaceResource[] interfaces() default {};

    boolean generateInterfaces() default false;
    boolean readOnlyInterfaces() default false;
    boolean enableOffHeap() default false;
}
