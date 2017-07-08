package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object;

import com.gs.fw.common.mithra.generator.metamodel.CardinalityType;
import com.gs.fw.common.mithra.generator.metamodel.ForeignKeyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Relationship
{
    // this is the relationship expression
    String contract();
    CardinalityType.Enums cardinality();
    boolean relatedIsDependent() default false;
    String reverseRelationshipName() default "";

    ForeignKeyType.Enums foreignKeyType() default ForeignKeyType.Enums.Auto;
    boolean directReference() default false;
    String orderBy() default "";
    String parameters() default "";
    String returnType() default "";
    boolean finalGetter() default false;
}
