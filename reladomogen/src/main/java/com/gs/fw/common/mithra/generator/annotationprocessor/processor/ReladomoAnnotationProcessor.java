package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.MithraGenerator;
import com.gs.fw.common.mithra.generator.MithraXMLObjectTypeParser;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;
import com.sun.tools.javac.code.Symbol;

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
    public static final String NON_GENERATED_SRC_DIR = "userSrcDir";
    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private Messager messager;
    private File userSrcDir;
    private List<GeneratedFile> generatedFiles = new ArrayList<GeneratedFile>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();

        // todo : barf if not set or invalid path
        // todo : change to relative path
        this.userSrcDir = new File(processingEnv.getOptions().get(NON_GENERATED_SRC_DIR));
    }

    @Override
    public Set<String> getSupportedOptions()
    {
        Set<String> options = new HashSet<String>();
        //todo : change to relative path
        options.add(NON_GENERATED_SRC_DIR);
        return options;
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
            relocateGeneratedFiles();
        } else
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
        List<? extends Element> e = element.getEnclosedElements();
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

    private void processGeneratorSpec(ReladomoGeneratorSpec generatorSpec)
    {
        boolean b = generatorSpec.generateConcreteClasses();
        String s = generatorSpec.generatedDir();
        boolean b1 = generatorSpec.generateGscListMethod();
        try
        {
            generatorSpec.domain();
        }
        catch (MirroredTypeException e)
        {
            DeclaredType declaredType = (DeclaredType) e.getTypeMirror();
            Element lisSpecElement = typeUtils.asElement(declaredType);
            ReladomoListSpec listSpec = lisSpecElement.getAnnotation(ReladomoListSpec.class);
            ObjectResourceSpec[] objectResourceSpecs = listSpec.resources();
            for (ObjectResourceSpec objectResourceSpec : objectResourceSpecs)
            {
                try
                {
                    objectResourceSpec.name();
                }
                catch (MirroredTypeException e1)
                {
                    declaredType = (DeclaredType) e1.getTypeMirror();
                    Element objectSpecElement = typeUtils.asElement(declaredType);
                    ReladomoObjectSpec objectSpec = objectSpecElement.getAnnotation(ReladomoObjectSpec.class);
                    String tableName = objectSpec.defaultTableName();
                    List<? extends Element> reladomoObjectSpecElements = objectSpecElement.getEnclosedElements();
                    for (Element reladomoObjectSpecElement : reladomoObjectSpecElements)
                    {
                        AsOfAttributeSpec asOfAttributeSpec = reladomoObjectSpecElement.getAnnotation(AsOfAttributeSpec.class);
                        if (asOfAttributeSpec != null)
                        {
                            System.out.println(asOfAttributeSpec.fromColumnName());
                        }
                        StringAttributeSpec stringAttributeSpec = reladomoObjectSpecElement.getAnnotation(StringAttributeSpec.class);
                        if (stringAttributeSpec != null)
                        {
                            System.out.println(stringAttributeSpec.columnName());
                        }
                    }
                }
            }
        }
    }

    private void processAnnotatedClass(Element annotatedElement)
    {
        ReladomoObjectSpec objectSpec = annotatedElement.getAnnotation(ReladomoObjectSpec.class);
        // do stuff
    }

    private List<GeneratedFile> generateFiles(Filer filer, String fullyQualifiedXmlPath)
    {
        MithraGenerator generator = new MithraGenerator();
        MithraXMLObjectTypeParser parser = new MithraXMLObjectTypeParser(fullyQualifiedXmlPath);
        generator.setMithraObjectTypeParser(parser);

        // todo : this just temporary fix to keep the generator happy
        // when re-factored, the generator will use a different parser that does not need this
        generator.setGeneratedDir("/tmp");

        AnnotationProcessorFileManager fileManager = new AnnotationProcessorFileManager(filer, userSrcDir);
        generator.setGeneratedFileManager(fileManager);

        try
        {
            generator.execute();
        } catch (Throwable e)
        {
            //todo : log to messager
            e.printStackTrace();
        }
        return fileManager.getGeneratedFiles();
    }

    private void relocateGeneratedFiles()
    {
        if (this.generatedFiles.isEmpty())
        {
            return;
        }
        for (GeneratedFile generatedFile : this.generatedFiles)
        {
            if (!generatedFile.existsInDir(this.userSrcDir))
            {
                try
                {
                    generatedFile.relocateTo(this.userSrcDir);
                } catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }

    }
}
