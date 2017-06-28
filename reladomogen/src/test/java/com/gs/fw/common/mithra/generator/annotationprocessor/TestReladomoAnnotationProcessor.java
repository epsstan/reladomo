package com.gs.fw.common.mithra.generator.annotationprocessor;

import com.gs.fw.common.mithra.generator.annotationprocessor.compiler.StringFileObject;
import com.gs.fw.common.mithra.generator.annotationprocessor.compiler.TestJavaCompiler;
import com.gs.fw.common.mithra.generator.annotationprocessor.processor.ReladomoAnnotationProcessor;
import com.gs.fw.common.mithra.generator.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
        FileUtils.deleteDir(tempDir);
    }

    private StringFileObject addFileToUserSrcDir(String srcPath, String targetPath, String className) throws URISyntaxException, IOException
    {
        String resourceText = FileUtils.readFileAsString(loadFile(srcPath));
        resourceText = resourceText.replaceAll("__NON_GENERATED_DIR__", userSrcDir.getAbsolutePath());
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
                    "/com/test1/specs/" +  name + ".java",
                    "com/test1/specs/" +  name + ".java",
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

        String[] files1 = getSortedFileNamesInDir(getUserSrcChildDir(userSrcDir, "com.test1.specs"));
        Assert.assertArrayEquals("mismatch in spec sources",
                new String[]{
                        "CustomerAccountSpec.java", "CustomerDomainListSpec.java", "CustomerSpec.java",
                        "ExampleGeneratorsSpec.java", "TimestampProvider.java"},
                files1);

        String[] files2 = getSortedFileNamesInDir(getUserSrcChildDir(userSrcDir, "com.test1.domain"));
        Assert.assertArrayEquals("mismatch in generated sources",
                new String[]{
                        "Customer.java", "CustomerAccount.java", "CustomerAccountDatabaseObject.java",
                        "CustomerAccountList.java", "CustomerDatabaseObject.java", "CustomerList.java"},
                files2);

        String[] files3 = getSortedFileNamesInDir(getUserSrcChildDir(targetGeneratedSrcDir, "com.test1.domain"));
        Assert.assertArrayEquals("mismatch in generated sources",
                new String[]{
                        "CustomerAbstract.java", "CustomerAccountAbstract.java",
                        "CustomerAccountData.java", "CustomerAccountDatabaseObjectAbstract.java", "CustomerAccountFinder.java", "CustomerAccountListAbstract.java", "CustomerData.java", "CustomerDatabaseObjectAbstract.java", "CustomerFinder.java", "CustomerListAbstract.java"},
                files3);
    }

    private File getUserSrcChildDir(File userSrcDir, String packageName)
    {
        File dir = userSrcDir;
        String[] tokens = packageName.split("\\.");
        for (String token : tokens)
        {
            dir = new File(dir, token);
        }
        return dir;
    }

    private String[] getSortedFileNamesInDir(File dir)
    {
        File[] files = dir.listFiles();
        List<String> fileNames =  new ArrayList<String>();
        for (File file : files)
        {
            fileNames.add(file.getName());
        }
        Collections.sort(fileNames);
        return fileNames.toArray(new String[]{});
    }
}
