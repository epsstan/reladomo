package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.ReladomoInterface;

import javax.lang.model.element.Element;
import java.util.List;

class ReladomoInterfaceSpecDetails
{
    private final ReladomoInterface reladomoInterface;
    private final List<? extends Element> enclosedElements;
    private final String name;
    private final String specName;

    public ReladomoInterfaceSpecDetails(String name, String specName, ReladomoInterface reladomoInterface, List<? extends Element> enclosedElements)
    {
        this.name = name;
        this.specName = specName;
        this.reladomoInterface = reladomoInterface;
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

    public ReladomoInterface getReladomoInterface()
    {
        return reladomoInterface;
    }

    public List<? extends Element> getEnclosedElements()
    {
        return enclosedElements;
    }
}