package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelationshipSpec
{
    enum Cardinality
    {
        OneToMany, ManyToMany, OneToOne, ManyToOne
    }

    // this is the relationship expression
    String contract();

    Cardinality cardinality();

    boolean relatedIsDependent() default false;

    String reverseRelationshipName();

}
