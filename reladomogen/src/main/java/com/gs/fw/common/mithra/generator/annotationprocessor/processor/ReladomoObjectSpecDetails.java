package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ReladomoObject;

import javax.lang.model.element.Element;
import java.util.List;

class ReladomoObjectSpecDetails
{
    private final ReladomoObject reladomoObject;
    private final List<? extends Element> enclosedElements;
    private final String name;
    private final String specName;

    public ReladomoObjectSpecDetails(String name, String specName, ReladomoObject reladomoObject, List<? extends Element> enclosedElements)
    {
        this.name = name;
        this.specName = specName;
        this.reladomoObject = reladomoObject;
        this.enclosedElements = enclosedElements;
    }

    public String getName()
    {
        return name;
    }

    public String getSpecName()
    {
        return specName;
    }

    public ReladomoObject getReladomoObject()
    {
        return reladomoObject;
    }

    public List<? extends Element> getEnclosedElements()
    {
        return enclosedElements;
    }
}