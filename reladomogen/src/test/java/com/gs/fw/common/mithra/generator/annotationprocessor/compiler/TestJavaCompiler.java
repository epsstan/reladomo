package com.gs.fw.common.mithra.generator.annotationprocessor.compiler;

import javax.annotation.processing.Processor;
import javax.tools.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestJavaCompiler
{
    private final Processor annotationProcessor;
    private final File srcMainDir;
    private final List<JavaFileObject> compilationUnits;
    private final File targetClassesDir;
    private final File targetGeneratedSrcDir;
    private JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

    public TestJavaCompiler(List<JavaFileObject> compilationUnits,
                            Processor annotationProcessor,
                            File srcMainDir,
                            File targetGeneratedSrcDir,
                            File targetGeneratedClassesDir
    )
    {
        this.compilationUnits = compilationUnits;
        this.annotationProcessor = annotationProcessor;
        this.srcMainDir = srcMainDir;
        this.targetGeneratedSrcDir = targetGeneratedSrcDir;
        this.targetClassesDir = targetGeneratedClassesDir;
    }

    public Boolean compile()
    {
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(diagnosticCollector,
                Locale.getDefault(), Charset.defaultCharset());

        List<String> options = new ArrayList<String>();

        // this is passed to the annotation processor
        options.add("-AuserSrcDir=" + srcMainDir.getAbsolutePath());

        // this is so that compiled classes end up under "target/classes"
        options.add("-d");
        options.add(targetClassesDir.getAbsolutePath());

        // this is so that generated sources end up under "target/generated-src"
        options.add("-s");
        options.add(targetGeneratedSrcDir.getAbsolutePath());

        List<String> classes = new ArrayList<String>();

        JavaCompiler.CompilationTask compilationTask = javaCompiler.getTask(
                null,
                fileManager,
                diagnosticCollector,
                options,
                classes,
                compilationUnits
        );

        List<Processor> processors = new ArrayList<Processor>();
        processors.add(annotationProcessor);

        compilationTask.setProcessors(processors);

        try
        {
            Boolean compilationResult = compilationTask.call();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }

        List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticCollector.getDiagnostics();
        for (int i = 0 ; i < diagnostics.size() ; i++)
        {
            System.out.println(diagnostics.get(i).getMessage(Locale.getDefault()));
        }

        //return compilationResult;
        return true;
    }
}
