package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.MithraGenerator;
import com.gs.fw.common.mithra.generator.MithraXMLObjectTypeParser;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ClassListXmlSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XmlAnnotationProcessor extends AbstractProcessor
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
        types.add(ClassListXmlSpec.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (roundEnv.processingOver())
        {
            relocateGeneratedFiles();
        }
        else
        {
            processClassList(roundEnv);
        }
        // no other processor needs to process this annotation
        return true;
    }

    private void processClassList(RoundEnvironment roundEnv)
    {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ClassListXmlSpec.class);
        if (annotatedElements.isEmpty())
        {
            return;
        }
        //process only the first one for this poc
        Element element = annotatedElements.iterator().next();
        if (element.getKind() != ElementKind.CLASS)
        {
            return;
        }
        ClassListXmlSpec classListAnnotation = element.getAnnotation(ClassListXmlSpec.class);
        String fullyQualifiedXmlPath = classListAnnotation.xmlPath();
        this.generatedFiles.addAll(generateFiles(filer, fullyQualifiedXmlPath));
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
        }
        catch (Throwable e)
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
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }

    }
}
