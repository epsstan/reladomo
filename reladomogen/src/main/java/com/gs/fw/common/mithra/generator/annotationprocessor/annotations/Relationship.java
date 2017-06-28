package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import com.gs.fw.common.mithra.generator.metamodel.CardinalityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Relationship
{
    enum Cardinality
    {
        OneToMany("one-to-many"), ManyToMany("many-to-many"), OneToOne("one-to-one"), ManyToOne("many-to-one");

        private String value;

        Cardinality(String value)
        {
            this.value = value;
        }

        public CardinalityType toType()
        {
            return new CardinalityType().with(value, null);
        }
    }

    enum ForeignKeyType
    {
        auto("auto"), False("false");

        private String value;

        ForeignKeyType(String value)
        {
            this.value = value;
        }

        public com.gs.fw.common.mithra.generator.metamodel.ForeignKeyType toType()
        {
            return new com.gs.fw.common.mithra.generator.metamodel.ForeignKeyType().with(value, null);
        }
    }

    // this is the relationship expression
    String contract();
    Cardinality cardinality();
    boolean relatedIsDependent() default false;
    String reverseRelationshipName() default "";
    ForeignKeyType foreignKeyType() default ForeignKeyType.auto;
    boolean directReference() default false;
    String orderBy() default "";
    String parameters() default "";
    String returnType() default "";
    boolean finalGetter() default false;
}
