package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoListSpec;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import java.util.HashSet;
import java.util.Set;

public class ElementTreeTranslator extends TreeTranslator
{
    private Set<String> identifiers = new HashSet<String>();

    @Override
    public void visitAnnotation(JCTree.JCAnnotation jcAnnotation)
    {
        JCTree annotationType = jcAnnotation.getAnnotationType();
        if (!annotationType.toString().equals(ReladomoListSpec.class.getSimpleName()))
        {
            return;
        }
        IdentifierVisitor identifierVisitor = new IdentifierVisitor();
        for (JCTree.JCExpression expression : jcAnnotation.getArguments())
        {
            expression.accept(identifierVisitor);
        }
        identifiers.addAll(identifierVisitor.getIdentifiers());
    }

    public Set<String> getIdentifiers()
    {
        return identifiers;
    }

    static class IdentifierVisitor extends TreeTranslator
    {
        private Set<String> identifiers = new HashSet<String>();

        @Override
        public void visitAssign(JCTree.JCAssign jcAssign)
        {
            String var = jcAssign.getVariable().toString();
            identifiers.add(var);
        }

        public Set<String> getIdentifiers()
        {
            return identifiers;
        }
    }
}
