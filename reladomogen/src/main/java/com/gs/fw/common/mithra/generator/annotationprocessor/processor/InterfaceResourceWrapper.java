package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.InterfaceResource;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.ReladomoInterface;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Set;

public class InterfaceResourceWrapper
{
    private InterfaceResource spec;
    private ReladomoInterfaceSpecDetails reladomoInterfaceSpecDetails;
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
            String specName = element.getSimpleName().toString();
            annotationArguments = reladomoListSpec.getAllObjectResourceAnnotationArguments().get(specName);

            if (!specName.endsWith("Spec"))
            {
                throw new IllegalArgumentException("ReladmoObjectSpec class name as to end with 'Spec'. Found name " + specName);
            }
            this.reladomoInterfaceSpecDetails = new ReladomoInterfaceSpecDetails(
                    specName.replaceAll("Spec", ""),
                    specName,
                    element.getAnnotation(ReladomoInterface.class),
                    element.getEnclosedElements());
        }
    }

    public ReladomoInterfaceSpecDetails getReladomoInterfaceSpecDetails()
    {
        return reladomoInterfaceSpecDetails;
    }

    public boolean readOnlyInterfaces()
    {
        return spec.readOnlyInterfaces();
    }
}
