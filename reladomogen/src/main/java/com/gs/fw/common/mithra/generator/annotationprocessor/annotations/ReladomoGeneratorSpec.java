package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Used to add generation properties to a domain (list).
    This is very similar to the reladom-gen xml tag.

    <reladomo-gen xml="${root}/reladomo/src/test/reladomo-xml/MithraClassListToImport.xml"
            nonGeneratedDir="${root}/reladomo/target/test-generated-onheap-src"
            nonGeneratedDir="${root}/reladomo/src/test/java"
            generateGscListMethod="${mithra.generateGscListMethod}"
            generateConcreteClasses="${reladomo.generateConcreteClasses}">
    </reladomo-gen>
*/

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReladomoGeneratorSpec
{
    Class domain();

    String nonGeneratedDir();

    boolean generateGscListMethod() default false;

    boolean generateConcreteClasses() default true;
}

