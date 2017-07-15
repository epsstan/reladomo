package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.InterfaceResource;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.ReladomoInterface;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterfaceResourceWrapper
{
    private List<? extends Element> enclosedElements;
    private String specName;
    private String name;
    private InterfaceResource spec;
    private ReladomoInterface reladomoInterface;
    private Set<String> annotationArguments = new HashSet<String>();

    public InterfaceResourceWrapper(ReladomoListSpecWrapper reladomoListSpec, InterfaceResource spec, Types typeUtils)
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
            this.reladomoInterface = element.getAnnotation(ReladomoInterface.class);
            this.enclosedElements = element.getEnclosedElements();
        }
    }

    public List<? extends Element> getEnclosedElements()
    {
        return enclosedElements;
    }

    public String getSpecName()
    {
        return specName;
    }

    public String getName()
    {
        return name;
    }

    public ReladomoInterface getReladomoInterface()
    {
        return reladomoInterface;
    }

    public boolean readOnlyInterfaces()
    {
        return spec.readOnlyInterfaces();
    }

    public String[] imports()
    {
        return reladomoInterface.imports();
    }

    public Class[] superInterfaces()
    {
        return reladomoInterface.superInterfaces();
    }
}
