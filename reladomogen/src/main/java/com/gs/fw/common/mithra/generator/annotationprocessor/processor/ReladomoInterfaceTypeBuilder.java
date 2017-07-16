package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.*;
import com.gs.fw.common.mithra.generator.metamodel.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReladomoInterfaceTypeBuilder
{
    public MithraInterfaceType buildInterfaceType(InterfaceResourceWrapper interfaceResource)
    {
        ReladomoInterface spec = interfaceResource.getReladomoInterface();
        MithraInterfaceType mithraInterfaceType = new MithraInterfaceType();
        mithraInterfaceType.setPackageName(spec.packageName());
        mithraInterfaceType.setReadOnlyInterfaces(interfaceResource.readOnlyInterfaces());
        mithraInterfaceType.setSourceFileName(interfaceResource.getSpecName());
        mithraInterfaceType.setImportedSource(interfaceResource.getSpecName());

        setImports(interfaceResource, mithraInterfaceType);
        setSuperInterfaces(interfaceResource, mithraInterfaceType);

        List<AsOfAttributeInterfaceType> asOfAttributeTypes = new ArrayList<AsOfAttributeInterfaceType>();
        List<AttributeInterfaceType> attributeTypes = new ArrayList<AttributeInterfaceType>();
        List<RelationshipInterfaceType> relationshipTypes = new ArrayList<RelationshipInterfaceType>();

        // todo : maybe convert this to use the visitor pattern ?
        AnnotationParser.ElementsByType elementsByType = partitionInterfaceElements(interfaceResource.getEnclosedElements());
        gatherInterfaceAttributes(asOfAttributeTypes, attributeTypes, elementsByType.attributes);
        gatherInterfaceRelationships(relationshipTypes, elementsByType.relationships);

        setInterfaceSourceAttribute(mithraInterfaceType, elementsByType);

        return mithraInterfaceType;
    }

    private void setInterfaceSourceAttribute(MithraInterfaceType mithraInterfaceType, AnnotationParser.ElementsByType elementsByType)
    {
        if (!elementsByType.sourceAttributes.isEmpty())
        {
            Element element = elementsByType.sourceAttributes.get(0);
            SourceAttributeInterfaceType sourceAttributeInterfaceType = makeInterfaceSourceAttribute(element, element.getSimpleName().toString());
            mithraInterfaceType.setSourceAttribute(sourceAttributeInterfaceType);
        }
    }

    private void setSuperInterfaces(InterfaceResourceWrapper interfaceResource, MithraInterfaceType mithraInterfaceType)
    {
        Class[] superInterfaces = interfaceResource.superInterfaces();
        if (superInterfaces != null)
        {
            List<String> superInterfaceNames = new ArrayList<String>();
            for (Class clazz : superInterfaces)
            {
                superInterfaceNames.add(clazz.getSimpleName());
            }
            mithraInterfaceType.setSuperInterfaces(superInterfaceNames);
        }
    }

    private void setImports(InterfaceResourceWrapper interfaceResource, MithraInterfaceType mithraInterfaceType)
    {
        String[] imports = interfaceResource.imports();
        if (imports != null)
        {
            mithraInterfaceType.setImports(Arrays.asList(imports));
        }
    }

    private void gatherInterfaceAttributes(List<AsOfAttributeInterfaceType> asOfAttributeTypes, List<AttributeInterfaceType> attributeTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            if (isInterfaceAsOfAttribute(element))
            {
                addInterfaceAsOfAttribute(element, name, asOfAttributeTypes);
            }
            else if (isInterfaceAttribute(element))
            {
                addInterfaceAttribute(element, name, attributeTypes);
            }
        }
    }

    private SourceAttributeInterfaceType makeInterfaceSourceAttribute(Element element, String name)
    {
        InterfaceSourceAttribute spec = element.getAnnotation(InterfaceSourceAttribute.class);
        SourceAttributeInterfaceType sourceAttributeInterfaceType = new SourceAttributeInterfaceType();
        Type returnType = ((Symbol.MethodSymbol) element).getReturnType();
        String typeName = returnType.toString();
        typeName = typeName.substring(typeName.lastIndexOf(".")+1);
        sourceAttributeInterfaceType.setName(name);
        sourceAttributeInterfaceType.setJavaType(typeName);
        return sourceAttributeInterfaceType;
    }

    private void addInterfaceAsOfAttribute(Element element, String name, List<AsOfAttributeInterfaceType> asOfAttributeTypes)
    {
        InterfaceAsOfAttribute asOfAttribute = element.getAnnotation(InterfaceAsOfAttribute.class);
        if (asOfAttribute != null)
        {
            AsOfAttributeInterfaceType attributeType = makeInterfaceAsOfAttribute(asOfAttribute, name);
            asOfAttributeTypes.add(attributeType);
        }
    }

    private AsOfAttributeInterfaceType makeInterfaceAsOfAttribute(InterfaceAsOfAttribute spec, String name)
    {
        AsOfAttributeInterfaceType asOfAttributeType = new AsOfAttributeInterfaceType();
        asOfAttributeType.setName(name);
        asOfAttributeType.setInfinityDate(spec.infinityDate());
        asOfAttributeType.setInfinityIsNull(spec.infinityIsNull());
        asOfAttributeType.setIsProcessingDate(spec.isProcessingDate());
        asOfAttributeType.setToIsInclusive(spec.toIsInclusive());
        asOfAttributeType.setTimezoneConversion(spec.timezoneConversion().getType());
        return asOfAttributeType;
    }

    private void addInterfaceAttribute(Element element, String name, List<AttributeInterfaceType> attributeTypes)
    {
        InterfaceAttribute spec = element.getAnnotation(InterfaceAttribute.class);
        Type returnType = ((Symbol.MethodSymbol) element).getReturnType();
        AttributeInterfaceType attributeType = new AttributeInterfaceType();
        attributeType.setName(name);
        String typeName = returnType.toString();
        typeName = typeName.substring(typeName.lastIndexOf(".")+1);
        attributeType.setJavaType(typeName);
        attributeTypes.add(attributeType);
    }

    private void gatherInterfaceRelationships(List<RelationshipInterfaceType> relationshipTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            relationshipTypes.add(makeInterfaceRelationship(element, name));
        }
    }

    private RelationshipInterfaceType makeInterfaceRelationship(Element element, String name)
    {
        InterfaceRelationship spec = element.getAnnotation(InterfaceRelationship.class);
        Type returnType = ((Symbol.MethodSymbol) element).getReturnType();
        String relatedObject = returnType.asElement().getSimpleName().toString().replaceAll("Spec", "");

        RelationshipInterfaceType relationshipInterfaceType = new RelationshipInterfaceType();
        relationshipInterfaceType.setName(name);
        relationshipInterfaceType.setCardinality(spec.cardinality().getType());
        relationshipInterfaceType.setParameters(spec.parameters());
        relationshipInterfaceType.setRelatedObject(relatedObject);
        return relationshipInterfaceType;
    }

    private AnnotationParser.ElementsByType partitionInterfaceElements(List<? extends Element> elements)
    {
        AnnotationParser.ElementsByType elementsByType = new AnnotationParser.ElementsByType();
        for (Element element : elements)
        {
            if (isInterfaceAttribute(element))
            {
                elementsByType.addAttribute(element);
            }
            else if (isInterfaceRelationship(element))
            {
                elementsByType.addRelationship(element);
            }
            else if (isInterfaceSourceAttribute(element))
            {
                elementsByType.addSourceAttribute(element);
            }
        }
        return elementsByType;
    }

    private boolean isInterfaceAttribute(Element element)
    {
        return element.getAnnotation(InterfaceAttribute.class) != null;
    }

    private boolean isInterfaceRelationship(Element element)
    {
        return element.getAnnotation(InterfaceRelationship.class) != null;
    }

    private boolean isInterfaceAsOfAttribute(Element element)
    {
        return element.getAnnotation(InterfaceAsOfAttribute.class) != null;
    }

    private boolean isInterfaceSourceAttribute(Element element)
    {
        return element.getAnnotation(InterfaceSourceAttribute.class) != null;
    }

}
