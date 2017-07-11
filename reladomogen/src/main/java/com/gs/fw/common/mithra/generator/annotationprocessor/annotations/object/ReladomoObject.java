package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object;

import com.gs.fw.common.mithra.generator.metamodel.ObjectType;
import com.gs.fw.common.mithra.generator.metamodel.SuperClassType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReladomoObject
{
    // class name is inferred from the class to which this annotation is applied

    String packageName();

    String defaultTableName();

    ObjectType.Enums objectType() default ObjectType.Enums.READ_ONLY;

    SuperClassType.Enums[] superClassType() default {};

    SuperClass[] superClass() default {};

    Class[] updateListenerClass() default {};

    Class[] datedTransactionalTemporalDirector() default {};

    Class[] interfaces() default {};

}
