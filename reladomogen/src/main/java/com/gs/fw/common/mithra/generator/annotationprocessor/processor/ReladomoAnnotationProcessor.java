package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.MithraGenerator;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorsSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoListSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

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
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
    private ArrayList<GeneratedFile> filesToCleanup;
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.filesToCleanup = new ArrayList<GeneratedFile>();

        JavacProcessingEnvironment javacProcessingEnvironment = (JavacProcessingEnvironment)processingEnv;
        this.trees = Trees.instance(javacProcessingEnvironment);
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
            cleanup();
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
        try
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
            Element generatorSpecElement = annotatedElements.iterator().next();
            if (generatorSpecElement.getKind() != ElementKind.INTERFACE)
            {
                messager.printMessage(Diagnostic.Kind.ERROR, ReladomoGeneratorsSpec.class.getSimpleName() + " annotation can be applied to interfaces only");
                return;
            }
            processGeneratorsSpec(generatorSpecElement);
        }
        catch (Throwable e)
        {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate classes");
            // todo : how to send this to the messager
            e.printStackTrace();
        }
    }

    private void processGeneratorsSpec(Element element) throws IOException
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

    private ReladomoListSpecWrapper getReladomoListSpec(ReladomoGeneratorSpec generatorSpec)
    {
        try
        {
            generatorSpec.domain();
            throw new RuntimeException("Failed to get type mirror exception");
        }
        catch (MirroredTypeException e)
        {
            DeclaredType declaredType = (DeclaredType) e.getTypeMirror();
            Element element = typeUtils.asElement(declaredType);
            ReladomoListSpec spec = element.getAnnotation(ReladomoListSpec.class);
            return new ReladomoListSpecWrapper(spec, element, element.getSimpleName().toString(),
                    elementUtils, typeUtils, trees);
        }
    }

    /*
        We generate a new generator and parser for each spec
     */
    private void processGeneratorSpec(ReladomoGeneratorSpec generatorSpec) throws IOException
    {
        MithraGenerator mithraGenerator = makeGenerator(generatorSpec);
        String nonGeneratedDir = mithraGenerator.getNonGeneratedDir();
        File userSrcDir = new File(nonGeneratedDir);
        List<GeneratedFile> generatedFiles = generateFiles(mithraGenerator, filer, userSrcDir);
        this.filesToCleanup.addAll(relocateGeneratedFiles(generatedFiles, userSrcDir));
    }

    private MithraGenerator makeGenerator(ReladomoGeneratorSpec generatorSpec) throws IOException
    {
        String nonGeneratedDir = generatorSpec.nonGeneratedDir();
        boolean generateConcreteClasses = generatorSpec.generateConcreteClasses();
        boolean generateGscListMethod = generatorSpec.generateGscListMethod();

        // todo : set other properties on generator
        MithraGenerator mithraGenerator = new MithraGenerator();
        mithraGenerator.setGenerateConcreteClasses(generateConcreteClasses);
        mithraGenerator.setGenerateGscListMethod(generateGscListMethod);
        mithraGenerator.setNonGeneratedDir(nonGeneratedDir);

        ReladomoListSpecWrapper reladomoListSpecWrapper = getReladomoListSpec(generatorSpec);

        // this is to keep the generator happy
        File fakeClassList = createFakeClassList(reladomoListSpecWrapper.getSpecName());
        String generatedDir = fakeClassList.getParentFile().getAbsolutePath();
        mithraGenerator.setGeneratedDir(generatedDir);

        AnnotationParser annotationParser = new AnnotationParser(this.typeUtils, this.elementUtils, this.trees,
                                                                reladomoListSpecWrapper, fakeClassList);
        mithraGenerator.setMithraObjectTypeParser(annotationParser);

        return mithraGenerator;
    }

    private List<GeneratedFile> generateFiles(MithraGenerator mithraGenerator, Filer filer, File userSrcDir)
    {
        AnnotationProcessorFileManager fileManager = new AnnotationProcessorFileManager(filer, userSrcDir);
        mithraGenerator.setGeneratedFileManager(fileManager);
        mithraGenerator.execute();
        return fileManager.getGeneratedFiles();
    }

    private List<GeneratedFile> relocateGeneratedFiles(List<GeneratedFile> generatedFiles, File userSrcDir) throws IOException
    {
        List<GeneratedFile> filesToCleanup = new ArrayList<GeneratedFile>();
        if (generatedFiles.isEmpty())
        {
            return filesToCleanup;
        }
        for (GeneratedFile generatedFile : generatedFiles)
        {
            if (!generatedFile.existsInDir(userSrcDir))
            {
                generatedFile.relocateTo(userSrcDir);
                filesToCleanup.add(generatedFile);
            }
        }
        return filesToCleanup;
    }

    private void cleanup()
    {
        for (GeneratedFile file : filesToCleanup)
        {
            file.fullPath().delete();
        }
    }

    /*
        This method does not really create a file as it does not write anything to the FileObject.
        This method is so that we can get the absolute path of the generated src dir
     */
    private File createFakeClassList(String name) throws IOException
    {
        FileObject tempResource = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", name);
        String path = tempResource.toUri().getPath();
        if (!path.startsWith("file://"))
        {
            path  = "file://" + path;
        }
        return new File(URI.create(path));
    }

    static class S extends TreeScanner
    {
        @Override
        public void visitAssert(JCTree.JCAssert jcAssert)
        {
            System.out.println("");
        }
    }
}
