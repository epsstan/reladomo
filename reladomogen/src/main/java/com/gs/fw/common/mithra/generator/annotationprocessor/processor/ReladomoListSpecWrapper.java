package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ObjectResource;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoListSpec;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

public class ReladomoListSpecWrapper
{
    private ReladomoListSpec spec;
    private String specName;
    private final Set<String> reladomoListAnnotationArguments;
    private final Map<String, Set<String>> allObjectResourceAnnotationArguments;

    public ReladomoListSpecWrapper(ReladomoListSpec spec, Element element, String specName,
                                   Elements elements, Types typeUtils, Trees trees)
    {
        this.spec = spec;
        this.specName = specName;

        TreePath path = trees.getPath(element);
        Scanner scanner = new Scanner();
        scanner.scan(path, trees);

        this.reladomoListAnnotationArguments = scanner.reladomoListAnnotationArguments;
        this.allObjectResourceAnnotationArguments = scanner.objectResourceAnnotationArguments;
    }

    public String getSpecName()
    {
        return specName;
    }
    public ObjectResource[] resources()
    {
        return spec.resources();
    }
    public Map<String, Set<String>> getAllObjectResourceAnnotationArguments()
    {
        return allObjectResourceAnnotationArguments;
    }

    boolean generateInterfaces()
    {
        return spec.generateInterfaces();
    }
    boolean readOnlyInterfaces()
    {
        return spec.readOnlyInterfaces();
    }
    boolean enableOffHeap()
    {
        return spec.enableOffHeap();
    }

    public boolean isGenerateInterfacesSet()
    {
        return reladomoListAnnotationArguments.contains("generateInterfaces");
    }
    public boolean isEnableOffHeapSet()
    {
        return reladomoListAnnotationArguments.contains("enableOffHeap");
    }
    public boolean isReadOnlyInterfacesSet()
    {
        return reladomoListAnnotationArguments.contains("readOnlyInterfaces");
    }

    static class Scanner extends TreePathScanner
    {
        private Set<String> reladomoListAnnotationArguments = new HashSet<String>();
        private Map<String, Set<String>> objectResourceAnnotationArguments = new HashMap<String, Set<String>>();

        @Override
        public Object visitAnnotation(AnnotationTree annotationTree, Object o)
        {
            Name name = ((JCTree.JCIdent) ((JCTree.JCAnnotation) annotationTree).annotationType).getName();
            if (name.toString().equals(ReladomoListSpec.class.getSimpleName()))
            {
                List<? extends ExpressionTree> arguments = annotationTree.getArguments();
                for (ExpressionTree arg : arguments)
                {
                    reladomoListAnnotationArguments.add(((JCTree.JCAssign) arg).getVariable().toString());
                }
            }
            else if (name.toString().equals(ObjectResource.class.getSimpleName()))
            {
                List<? extends ExpressionTree> arguments = annotationTree.getArguments();
                Set<String> resourceArguments = new HashSet<String>();
                for (ExpressionTree arg : arguments)
                {
                    String argName = ((JCTree.JCAssign) arg).getVariable().toString();
                    resourceArguments.add(argName);
                    if (argName.equals("name"))
                    {
                        JCTree.JCExpression argValue = ((JCTree.JCAssign) arg).getExpression();
                        objectResourceAnnotationArguments.put(argValue.toString().replace(".class", ""), resourceArguments);
                    }
                }
            }
            return super.visitAnnotation(annotationTree, o);
        }
    }
}
