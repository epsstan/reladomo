package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ObjectResource;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ReladomoObject;
import com.sun.source.util.Trees;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectResourceWrapper
{
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
        }
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
}
