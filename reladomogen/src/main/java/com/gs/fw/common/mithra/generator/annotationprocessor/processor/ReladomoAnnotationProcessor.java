package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.MithraGenerator;
import com.gs.fw.common.mithra.generator.MithraXMLObjectTypeParser;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoObjectSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
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
        types.add(ReladomoGeneratorSpec.class.getCanonicalName());
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
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ReladomoGeneratorSpec.class);
        if (annotatedElements.isEmpty())
        {
            return;
        }
        if (annotatedElements.size() > 1)
        {
            messager.printMessage(Diagnostic.Kind.ERROR, "Expected 1 but found " + annotatedElements.size() + " generator specs in the classpath");
            return;
        }
        Element generatorSpec = annotatedElements.iterator().next();
        if (generatorSpec.getKind() != ElementKind.INTERFACE)
        {
            messager.printMessage(Diagnostic.Kind.ERROR, ReladomoGeneratorSpec.class.getSimpleName() + " annotation can be applied to interfaces only");
            return;
        }
        processGeneratorSpec(generatorSpec);
    }

    private void processGeneratorSpec(Element element)
    {
        ReladomoGeneratorSpec generatorSpec = element.getAnnotation(ReladomoGeneratorSpec.class);
        
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
