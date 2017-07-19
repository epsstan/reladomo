package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ObjectResource;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ReladomoObject;
import com.sun.source.util.Trees;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

public class ObjectResourceWrapper
{
    private List<String> interfaceClassNames;
    private ReladomoObject reladomoObject;
    private List<? extends Element> enclosedElements;
    private String name;
    private String specName;
    private ObjectResource spec;
    private Set<String> annotationArguments = new HashSet<String>();

    public ObjectResourceWrapper(ReladomoListSpecWrapper reladomoListSpec, ObjectResource spec, Elements elementUtils, Types typeUtils, Trees trees)
    {
        this.spec = spec;
        try
        {
            spec.name();
        }
        catch (MirroredTypeException e)
        {
            Element element = typeUtils.asElement(e.getTypeMirror());
            this.specName = element.getSimpleName().toString();
            annotationArguments = reladomoListSpec.getAllObjectResourceAnnotationArguments().get(specName);

            if (!specName.endsWith("Spec"))
            {
                throw new IllegalArgumentException("ReladmoObjectSpec class name as to end with 'Spec'. Found name " + specName);
            }
            this.name = specName.replaceAll("Spec", "");
            this.reladomoObject = element.getAnnotation(ReladomoObject.class);
            this.enclosedElements = element.getEnclosedElements();
            this.interfaceClassNames = gatherInterfaceClassNames(element);
        }
    }

    private List<String> gatherInterfaceClassNames(Element element)
    {
        List<String> interfaceClassNames = new ArrayList<String>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = element.getAnnotationMirrors().get(0).getElementValues();
        for (ExecutableElement key : elementValues.keySet())
        {
            if (key.getSimpleName().toString().equals("interfaces"))
            {
                com.sun.tools.javac.util.List interfaces = (com.sun.tools.javac.util.List)elementValues.get(key).getValue();
                for (int i = 0 ; i < interfaces.size() ; i++)
                {
                    String name = reduceToSimpleNam(interfaces, i);
                    interfaceClassNames.add(name);
                }
            }
        }
        return interfaceClassNames;
    }

    private String reduceToSimpleNam(com.sun.tools.javac.util.List interfaces, int i)
    {
        String name = interfaces.get(i).toString().replaceAll(".class", "");
        if (name.endsWith("Spec"))
        {
            name = name.substring(0, name.lastIndexOf("Spec"));
        }
        return name.substring(name.lastIndexOf(".") + 1);
    }

    public ReladomoObject getReladomoObject()
    {
        return reladomoObject;
    }

    public String getName()
    {
        return name;
    }

    public String getSpecName()
    {
        return specName;
    }

    public List<? extends Element> getEnclosedElements()
    {
        return enclosedElements;
    }

    boolean generateInterfaces()
    {
        return spec.generateInterfaces();
    }
    boolean readOnlyInterfaces()
    {
        return spec.readOnlyInterfaces();
    }
    boolean enableOffHeap() { return spec.enableOffHeap(); }
    boolean replicated() { return spec.replicated(); }

    public boolean isGenerateInterfacesSet()
    {
        return annotationArguments.contains("generateInterfaces");
    }
    public boolean isEnableOffHeapSet()
    {
        return annotationArguments.contains("enableOffHeap");
    }
    public boolean isReadOnlyInterfacesSet()
    {
        return annotationArguments.contains("readOnlyInterfaces");
    }

    public List<String> getInterfaceClassNames()
    {
        return interfaceClassNames;
    }
}
