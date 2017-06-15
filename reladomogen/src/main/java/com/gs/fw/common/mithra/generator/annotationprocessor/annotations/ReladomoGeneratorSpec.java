package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Used to add generation properties to a domain (list)
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReladomoGeneratorSpec
{
    Class domain();

    String generatedDir();

    boolean generateGscListMethod() default false;

    boolean generateConcreteClasses() default true;
}

/*
    <reladomo-gen xml="${root}/reladomo/src/test/reladomo-xml/MithraClassListToImport.xml"
            generatedDir="${root}/reladomo/target/test-generated-onheap-src"
            nonGeneratedDir="${root}/reladomo/src/test/java"
            generateGscListMethod="${mithra.generateGscListMethod}"
            generateConcreteClasses="${reladomo.generateConcreteClasses}">
        </reladomo-gen>
*/

