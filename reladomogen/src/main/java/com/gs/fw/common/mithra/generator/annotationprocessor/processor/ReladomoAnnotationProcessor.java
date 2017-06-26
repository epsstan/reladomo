package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.MithraGenerator;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;
import com.gs.fw.common.mithra.generator.metamodel.AsOfAttributeType;
import com.gs.fw.common.mithra.generator.metamodel.AttributeType;
import com.gs.fw.common.mithra.generator.metamodel.MithraObject;
import com.gs.fw.common.mithra.generator.metamodel.PropertyType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReladomoAnnotationProcessor extends AbstractProcessor
{
    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedOptions()
    {
        return new HashSet<String>();
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        Set<String> types = new HashSet<String>();
        types.add(ReladomoGeneratorsSpec.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (roundEnv.processingOver())
        {
            // relocateGeneratedFiles();
        }
        else
        {
            process(roundEnv);
        }
        // no other processor needs to process this annotation
        return true;
    }

    private void process(RoundEnvironment roundEnv)
    {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ReladomoGeneratorsSpec.class);
        if (annotatedElements.isEmpty())
        {
            return;
        }
        if (annotatedElements.size() > 1)
        {
            messager.printMessage(Diagnostic.Kind.ERROR, "Expected 1 but found " + annotatedElements.size() + " generator specs in the classpath");
            return;
        }
        Element generatorsSpec = annotatedElements.iterator().next();
        if (generatorsSpec.getKind() != ElementKind.INTERFACE)
        {
            messager.printMessage(Diagnostic.Kind.ERROR, ReladomoGeneratorsSpec.class.getSimpleName() + " annotation can be applied to interfaces only");
            return;
        }
        processGeneratorsSpec(generatorsSpec);
    }

    private void processGeneratorsSpec(Element element)
    {
        ReladomoGeneratorsSpec generatorsSpec = element.getAnnotation(ReladomoGeneratorsSpec.class);
        ReladomoGeneratorSpec[] generators = generatorsSpec.generators();
        if (generators == null || generators.length == 0)
        {
            messager.printMessage(Diagnostic.Kind.ERROR, ReladomoGeneratorsSpec.class.getSimpleName() + " annotation can be applied to interfaces only");
            return;
        }
        for (ReladomoGeneratorSpec spec : generators)
        {
            processGeneratorSpec(spec);
        }
    }

    private ReladomoListSpec getReladomoListSpec(ReladomoGeneratorSpec generatorSpec)
    {
        try
        {
            generatorSpec.domain();
            throw new RuntimeException("Failed to get type mirror exception");
        }
        catch (MirroredTypeException e)
        {
            DeclaredType declaredType = (DeclaredType) e.getTypeMirror();
            Element listSpecElement = typeUtils.asElement(declaredType);
            return listSpecElement.getAnnotation(ReladomoListSpec.class);
        }
    }

    static class ReladomoObjectSpecDetails
    {

        private final ReladomoObjectSpec reladomoObjectSpec;
        private final List<? extends Element> enclosedElements;
        private final String name;
        private final String specName;

        public ReladomoObjectSpecDetails(String name, String specName, ReladomoObjectSpec reladomoObjectSpec, List<? extends Element> enclosedElements)
        {
            this.name = name;
            this.specName = specName;
            this.reladomoObjectSpec = reladomoObjectSpec;
            this.enclosedElements = enclosedElements;
        }
    }

    private ReladomoObjectSpecDetails getReladomoObjectSpec(ObjectResourceSpec objectResourceSpec)
    {
        try
        {
            objectResourceSpec.name();
            throw new RuntimeException("Failed to get type mirror exception");
        }
        catch (MirroredTypeException e1)
        {
            DeclaredType declaredType = (DeclaredType) e1.getTypeMirror();
            Element objectSpecElement = typeUtils.asElement(declaredType);
            String specName = objectSpecElement.getSimpleName().toString();
            if (!specName.endsWith("Spec"))
            {
                throw new IllegalArgumentException("ReladmoObjectSpec class name as to end with 'Spec'. Found name " + specName);
            }
            return new ReladomoObjectSpecDetails(
                    specName.replaceAll("Spec", ""),
                    specName,
                    objectSpecElement.getAnnotation(ReladomoObjectSpec.class),
                    objectSpecElement.getEnclosedElements());
        }
    }

    private void processGeneratorSpec(ReladomoGeneratorSpec generatorSpec)
    {
        boolean generateConcreteClasses = generatorSpec.generateConcreteClasses();
        String nonGeneratedDir = generatorSpec.nonGeneratedDir();
        boolean generateGscListMethod = generatorSpec.generateGscListMethod();

        // todo : set other properties on generator
        MithraGenerator mithraGenerator = new MithraGenerator();
        mithraGenerator.setGenerateConcreteClasses(generateConcreteClasses);
        mithraGenerator.setGenerateGscListMethod(generateGscListMethod);
        // todo : how to set this ?
        mithraGenerator.setGeneratedDir("/tmp/foo1");

        AnnotationParser annotationParser = new AnnotationParser();
        mithraGenerator.setMithraObjectTypeParser(annotationParser);

        ReladomoListSpec reladomoListSpec = getReladomoListSpec(generatorSpec);
        ObjectResourceSpec[] objectResourceSpecs = reladomoListSpec.resources();
        for (ObjectResourceSpec objectResourceSpec : objectResourceSpecs)
        {
            ReladomoObjectSpecDetails reladomoObjectSpecDetails = getReladomoObjectSpec(objectResourceSpec);
            MithraObject mithraObject = processReladomoObject(reladomoObjectSpecDetails);
            annotationParser.addMithraObject(mithraObject,
                    reladomoObjectSpecDetails.name,
                    reladomoObjectSpecDetails.specName,
                    objectResourceSpec,
                    reladomoObjectSpecDetails.reladomoObjectSpec,
                    reladomoListSpec);
        }
        List<GeneratedFile> generatedFiles = generateFiles(mithraGenerator, filer, nonGeneratedDir);
        relocateGeneratedFiles(generatedFiles, nonGeneratedDir);
    }

    private MithraObject processReladomoObject(ReladomoObjectSpecDetails reladomoObjectSpecDetails)
    {
        ReladomoObjectSpec reladomoObjectSpec = reladomoObjectSpecDetails.reladomoObjectSpec;

        MithraObject mithraObject = new MithraObject();
        mithraObject.setPackageName(reladomoObjectSpec.packageName());
        mithraObject.setClassName(reladomoObjectSpecDetails.name);
        mithraObject.setDefaultTable(reladomoObjectSpec.defaultTableName());

        List<AsOfAttributeType> asOfAttributeTypes = new ArrayList<AsOfAttributeType>();
        List<AttributeType> attributeTypes = new ArrayList<AttributeType>();

        for (Element reladomoObjectSpecElement : reladomoObjectSpecDetails.enclosedElements)
        {
            boolean primaryKey = isPrimaryKeyAttribute(reladomoObjectSpecElement);
            String name = reladomoObjectSpecElement.getSimpleName().toString();
            AsOfAttributeSpec asOfAttributeSpec = reladomoObjectSpecElement.getAnnotation(AsOfAttributeSpec.class);
            if (asOfAttributeSpec != null)
            {
                asOfAttributeTypes.add(makeAsOfAttribute(asOfAttributeSpec, name));
            }
            StringAttributeSpec stringAttributeSpec  = reladomoObjectSpecElement.getAnnotation(StringAttributeSpec.class);
            if (stringAttributeSpec != null)
            {
                attributeTypes.add(makeStringAttribute(stringAttributeSpec, name, primaryKey));
            }
            IntAttributeSpec intAttributeSpec  = reladomoObjectSpecElement.getAnnotation(IntAttributeSpec.class);
            if (intAttributeSpec != null)
            {
                attributeTypes.add(makeIntAttribute(intAttributeSpec, name, primaryKey));
            }
        }
        mithraObject.setAsOfAttributes(asOfAttributeTypes);
        mithraObject.setAttributes(attributeTypes);
        return mithraObject;
    }

    private boolean isPrimaryKeyAttribute(Element reladomoObjectSpecElement)
    {
        PrimaryKeySpec primaryKeySpec = reladomoObjectSpecElement.getAnnotation(PrimaryKeySpec.class);
        if (primaryKeySpec == null)
        {
            return false;
        }
        return true;
    }

    private AttributeType makeStringAttribute(StringAttributeSpec spec, String name, boolean primaryKey)
    {
        AttributeType attributeType = new AttributeType();
        attributeType.setName(name);
        attributeType.setJavaType("String");
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setMaxLength(spec.maxLength());
        attributeType.setTruncate(spec.truncate());
        attributeType.setTrim(spec.trim());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setPrimaryKey(primaryKey);

        //todo : is properties applicable ?

        return attributeType;
    }

    private AttributeType makeIntAttribute(IntAttributeSpec spec, String name, boolean primaryKey)
    {
        AttributeType attributeType = new AttributeType();
        attributeType.setName(name);
        attributeType.setJavaType("int");
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setUseForOptimisticLocking(spec.useForOptimisticLocking());
        attributeType.setPrimaryKey(primaryKey);

        //todo : is properties applicable ?
        //todo : precision/scale

        return attributeType;
    }

    private AsOfAttributeType makeAsOfAttribute(AsOfAttributeSpec spec, String name)
    {
        AsOfAttributeType asOfAttributeType = new AsOfAttributeType();
        asOfAttributeType.setName(name);
        asOfAttributeType.setSetAsString(spec.setAsString());
        asOfAttributeType.setFromColumnName(spec.fromColumnName());
        asOfAttributeType.setToColumnName(spec.toColumnName());
        asOfAttributeType.setFinalGetter(spec.finalGetters());
        asOfAttributeType.setDefaultIfNotSpecified(spec.defaultIfNotSpecified());
        asOfAttributeType.setFutureExpiringRowsExist(spec.futureExpiringRowsExist());
        asOfAttributeType.setInfinityDate(spec.infinityDate());
        asOfAttributeType.setInfinityIsNull(spec.infinityIsNull());
        asOfAttributeType.setIsProcessingDate(spec.isProcessingDate());
        asOfAttributeType.setToIsInclusive(spec.toIsInclusive());
        asOfAttributeType.setTimezoneConversion(spec.timezoneConversion().toType());
        asOfAttributeType.setTimestampPrecision(spec.timestampPrecision().toType());
        asOfAttributeType.setPoolable(spec.poolable());
        asOfAttributeType.setProperties(extractProperties(spec));
        return asOfAttributeType;
    }

    private List<PropertyType> extractProperties(AsOfAttributeSpec spec)
    {
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        for (PropertySpec propertySpec : spec.properties())
        {
            PropertyType propType = new PropertyType();
            propType.setKey(propertySpec.key());
            propType.setValue(propertySpec.value());
            propertyTypes.add(propType);
        }
        return propertyTypes;
    }

    private List<GeneratedFile> generateFiles(MithraGenerator mithraGenerator, Filer filer, String userSrcDir)
    {
        File userSrcDirFile = new File(userSrcDir);
        AnnotationProcessorFileManager fileManager = new AnnotationProcessorFileManager(filer, userSrcDirFile);
        mithraGenerator.setGeneratedFileManager(fileManager);

        try
        {
            mithraGenerator.execute();
        }
        catch (Throwable e)
        {
            //todo : log to messager
            e.printStackTrace();
        }
        return fileManager.getGeneratedFiles();
    }

    private void relocateGeneratedFiles(List<GeneratedFile> generatedFiles, String userSrcDir)
    {
        File userSrcDirFile = new File(userSrcDir);
        if (generatedFiles.isEmpty())
        {
            return;
        }
        for (GeneratedFile generatedFile : generatedFiles)
        {
            if (!generatedFile.existsInDir(userSrcDirFile))
            {
                try
                {
                    generatedFile.relocateTo(userSrcDirFile);
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

}
