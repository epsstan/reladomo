package com.gs.fw.common.mithra.generator.annotationprocessor;

import com.gs.fw.common.mithra.generator.annotationprocessor.compiler.StringFileObject;
import com.gs.fw.common.mithra.generator.annotationprocessor.compiler.TestJavaCompiler;
import com.gs.fw.common.mithra.generator.annotationprocessor.processor.ReladomoAnnotationProcessor;
import com.gs.fw.common.mithra.generator.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestReladomoAnnotationProcessor
{
    private File targetGeneratedSrcDir;
    private File userSrcDir;
    private File targetClassesDir;
    private File tempDir;

    @Before
    public void setup() throws IOException
    {
        tempDir = new File("/tmp/" + System.currentTimeMillis());
        tempDir.mkdirs();

        userSrcDir = new File(tempDir, "src/main/java");
        userSrcDir.mkdirs();

        targetGeneratedSrcDir = new File(tempDir, "target/generated-src");
        targetGeneratedSrcDir.mkdirs();

        targetClassesDir = new File(tempDir, "target/classes");
        targetClassesDir.mkdirs();

        System.out.println("Using temp dir " + tempDir.getAbsolutePath());
    }

    @After
    public void cleanup()
    {
        //FileUtils.deleteDir(tempDir);
    }

    private StringFileObject addFileToUserSrcDir(String srcPath, String targetPath, String className) throws URISyntaxException, IOException
    {
        String resourceText = FileUtils.readFileAsString(loadFile(srcPath));
        File file = new File(userSrcDir, targetPath);
        FileUtils.createFile(resourceText, file);
        StringFileObject classListFileObject = new StringFileObject(className, resourceText);
        return classListFileObject;
    }

    private List<JavaFileObject> stageTestFiles() throws URISyntaxException, IOException
    {
        List<JavaFileObject> javaFileObjectList = new ArrayList<JavaFileObject>();
        for (String name : new String[] {"CustomerAccountSpec", "CustomerSpec",
                "CustomerDomainListSpec", "ExampleGeneratorsSpec", "TimestampProvider"})
        {
            javaFileObjectList.add(addFileToUserSrcDir(
                    "/annotations_set1/" +  name + ".java",
                    "annotations_set1/" +  name + ".java",
                    name)
            );
        }
        return javaFileObjectList;
    }

    private File loadFile(String classpathResourcePath) throws URISyntaxException
    {
        URL resource = TestReladomoAnnotationProcessor.class.getResource(classpathResourcePath);
        return new File(resource.toURI());
    }

    @Test
    public void testGeneration() throws URISyntaxException, IOException
    {
        List<JavaFileObject> compilationUnits = stageTestFiles();

        ReladomoAnnotationProcessor processor = new ReladomoAnnotationProcessor();
        TestJavaCompiler compiler = new TestJavaCompiler(compilationUnits, processor, userSrcDir, targetGeneratedSrcDir, targetClassesDir);
        Boolean compilationStatus = compiler.compile();
        assertEquals(true, compilationStatus);
    }
}
