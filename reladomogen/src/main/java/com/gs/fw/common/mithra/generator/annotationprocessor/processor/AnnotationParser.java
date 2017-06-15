package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.*;
import com.gs.fw.common.mithra.generator.metamodel.MithraBaseObjectType;
import com.gs.fw.common.mithra.generator.metamodel.MithraGeneratorParserException;
import com.gs.fw.common.mithra.generator.metamodel.MithraInterfaceType;
import com.gs.fw.common.mithra.generator.metamodel.MithraType;
import com.gs.fw.common.mithra.generator.util.AwaitingThreadExecutor;
import com.gs.fw.common.mithra.generator.util.ChopAndStickResource;
import com.gs.fw.common.mithra.generator.util.FullFileBuffer;
import com.gs.fw.common.mithra.generator.util.SerialResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.zip.CRC32;

import static org.javacc.parser.JavaCCGlobals.fileName;

public class AnnotationParser implements MithraObjectTypeParser
{
    private CRC32 crc32 = new CRC32();

    private static final int IO_THREADS = 1;
    private AwaitingThreadExecutor executor;
    private Throwable executorError;
    private ChopAndStickResource chopAndStickResource = new ChopAndStickResource(new Semaphore(Runtime.getRuntime().availableProcessors()),
            new Semaphore(IO_THREADS), new SerialResource());

    private Map<String, MithraObjectTypeWrapper> mithraObjects = new ConcurrentHashMap<String, MithraObjectTypeWrapper>();
    private List<MithraObjectTypeWrapper> sortedMithraObjects;
    private Map<String, MithraEmbeddedValueObjectTypeWrapper> mithraEmbeddedValueObjects = new ConcurrentHashMap<String, MithraEmbeddedValueObjectTypeWrapper>();
    private List<MithraEmbeddedValueObjectTypeWrapper> sortedMithraEmbeddedValueObjects;
    private Map<String, MithraEnumerationTypeWrapper> mithraEnumerations = new ConcurrentHashMap<String, MithraEnumerationTypeWrapper>();
    private List<MithraEnumerationTypeWrapper> sortedMithraEnumerations;
    private Map<String, MithraInterfaceType> mithraInterfaces = new ConcurrentHashMap<String, MithraInterfaceType>();

    private boolean generateFileHeaders = false;
    private boolean ignoreNonGeneratedAbstractClasses = false;
    private boolean ignoreTransactionalMethods = false;
    private boolean ignorePackageNamingConvention = false;
    private boolean defaultFinalGetters = false;
    private boolean forceOffHeap = false;

    private ThreadLocal<FullFileBuffer> fullFileBufferThreadLocal = new ThreadLocal<FullFileBuffer>();

    private Logger logger;

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void setForceOffHeap(boolean forceOffHeap)
    {
        this.forceOffHeap = forceOffHeap;
    }

    @Override
    public void setDefaultFinalGetters(boolean defaultFinalGetters)
    {
        this.defaultFinalGetters = defaultFinalGetters;
    }

    public Map<String, MithraObjectTypeWrapper> getMithraObjects()
    {
        return this.mithraObjects;
    }

    public Map<String, MithraEmbeddedValueObjectTypeWrapper> getMithraEmbeddedValueObjects()
    {
        return this.mithraEmbeddedValueObjects;
    }

    public Map<String, MithraEnumerationTypeWrapper> getMithraEnumerations()
    {
        return this.mithraEnumerations;
    }

    public Map<String,MithraInterfaceType> getMithraInterfaces()
    {
        return mithraInterfaces;
    }

    public File parse() throws MithraGeneratorException
    {
        try
        {
            parserAnnotations();
            //todo : remove file
            return new File("/tmp/foo");
        }
        catch (Throwable e)
        {
            throw new MithraGeneratorException(e);
        }
    }

    public void parserAnnotations()
    {
        MithraType result;
        try
        {
            //todo : fix logger
            //this.logger.debug(obj.getClass().getName() + ": " + MithraType.class.getName());

            long start = System.currentTimeMillis();
            int normalObjects = this.parseMithraObjects();
            int pureObjects = this.parseMithraPureObjects();
            int tempObjects = this.parseMithraTempObjects();
            int embeddedObjects = this.parseMithraEmbeddedValueObjects();
            int enumerations = this.parseMithraEnumerations();
            int mithraInterfaceObjects = this.parseMithraInterfaceObjects();
            //todo : use messager
            String msg = ": parsed ";
            msg = concatParsed(msg, normalObjects, "normal");
            msg = concatParsed(msg, pureObjects, "pure");
            msg = concatParsed(msg, tempObjects, "temp");
            msg = concatParsed(msg, embeddedObjects, "embedded");
            msg = concatParsed(msg, enumerations, "enumeration");
            msg = concatParsed(msg, mithraInterfaceObjects, "mithraInterface");
            msg += " Mithra objects in "+(System.currentTimeMillis() - start)+" ms.";
            this.logger.info(msg);
        }
        catch (MithraGeneratorParserException e)
        {
            throw new MithraGeneratorException("Unable to parse "+ fileName, e);
        }
        catch (IOException e)
        {
            throw new MithraGeneratorException("Unable to read file "+ fileName, e);
        }
    }

    private int parseMithraObjects() throws FileNotFoundException
    {
        /*
        List<MithraObjectResourceType> mithraObjectList = mithraType.getMithraObjectResources();
        chopAndStickResource.resetSerialResource();
        for (int i = 0; i <mithraObjectList.size(); i++)
        {
            final MithraObjectResourceType mithraObjectResourceType = mithraObjectList.get(i);
            final String objectName = mithraObjectResourceType.getName();
            getExecutor().submit(new GeneratorTask(i)
            {
                public void run()
                {
                    MithraBaseObjectType mithraObject = parseMithraObject(objectName, mithraObjects, fileProvider, this, "normal");
                    if (mithraObject != null)
                    {
                        String objectFileName = objectName + ".xml";
                        boolean isGenerateInterfaces = !mithraObjectResourceType.isGenerateInterfacesSet() ? mithraType.isGenerateInterfaces() : mithraObjectResourceType.isGenerateInterfaces();
                        boolean enableOffHeap = !mithraObjectResourceType.isEnableOffHeapSet() ? mithraType.isEnableOffHeap() || forceOffHeap : mithraObjectResourceType.isEnableOffHeap();
                        MithraObjectTypeWrapper wrapper = new MithraObjectTypeWrapper(mithraObject, objectFileName, importSource, isGenerateInterfaces, ignorePackageNamingConvention, MithraXMLObjectTypeParser.this.logger);
                        wrapper.setGenerateFileHeaders(generateFileHeaders);
                        wrapper.setReplicated(mithraObjectResourceType.isReplicated());
                        wrapper.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
                        wrapper.setIgnoreTransactionalMethods(ignoreTransactionalMethods);
                        wrapper.setReadOnlyInterfaces(mithraObjectResourceType.isReadOnlyInterfacesSet() ? mithraObjectResourceType.isReadOnlyInterfaces() : mithraType.isReadOnlyInterfaces());
                        wrapper.setDefaultFinalGetters(defaultFinalGetters);
                        wrapper.setEnableOffHeap(enableOffHeap);
                        mithraObjects.put(mithraObjectResourceType.getName(), wrapper);
                    }
                }
            });
        }
        waitForExecutorWithCheck();
        return mithraObjectList.size();
        */
        return 0;
    }

    private int parseMithraPureObjects()
    {
        /*
        List<MithraPureObjectResourceType> mithraPureObjectList = mithraType.getMithraPureObjectResources();
        if (!mithraPureObjectList.isEmpty())
        {
            chopAndStickResource.resetSerialResource();
            for (int i=0; i< mithraPureObjectList.size();i++)
            {
                final MithraPureObjectResourceType mithraPureObjectResourceType = mithraPureObjectList.get(i);
                getExecutor().submit(new GeneratorTask(i)
                {
                    public void run()
                    {
                        String objectName = mithraPureObjectResourceType.getName();
                        MithraBaseObjectType mithraObject = parseMithraObject(objectName, mithraObjects, fileProvider, this, "pure");
                        if (mithraObject != null)
                        {
                            String objectFileName = objectName + ".xml";
                            boolean enableOffHeap = !mithraPureObjectResourceType.isEnableOffHeapSet() ? mithraType.isEnableOffHeap() || forceOffHeap : mithraPureObjectResourceType.isEnableOffHeap();
                            MithraObjectTypeWrapper wrapper = new MithraObjectTypeWrapper(mithraObject, objectFileName, importSource, false, ignorePackageNamingConvention, MithraXMLObjectTypeParser.this.logger);
                            wrapper.setGenerateFileHeaders(generateFileHeaders);
                            wrapper.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
                            wrapper.setIgnoreTransactionalMethods(ignoreTransactionalMethods);
                            wrapper.setPure(true);
                            wrapper.setEnableOffHeap(enableOffHeap);
                            mithraObjects.put(mithraPureObjectResourceType.getName(), wrapper);
                        }
                    }
                });
            }
            waitForExecutorWithCheck();
        }
        return mithraPureObjectList.size();
        */
        return 0;
    }

    private int parseMithraTempObjects()
    {
        /*
        List<MithraTempObjectResourceType> mithraTempObjectList = mithraType.getMithraTempObjectResources();
        if (mithraTempObjectList.size() > 0)
        {
            chopAndStickResource.resetSerialResource();
            for (int i=0; i<mithraTempObjectList.size();i++)
            {
                final MithraTempObjectResourceType mithraTempObjectResourceType = mithraTempObjectList.get(i);
                getExecutor().submit(new GeneratorTask(i)
                {
                    public void run()
                    {
                        String objectName = mithraTempObjectResourceType.getName();
                        MithraBaseObjectType mithraObject = parseMithraObject(objectName, mithraObjects, fileProvider, this, "temp");
                        if (mithraObject != null)
                        {
                            String objectFileName = objectName + ".xml";
                            MithraObjectTypeWrapper wrapper = new MithraObjectTypeWrapper(mithraObject, objectFileName, importSource, false, ignorePackageNamingConvention, MithraXMLObjectTypeParser.this.logger);
                            wrapper.setGenerateFileHeaders(generateFileHeaders);
                            wrapper.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
                            wrapper.setIgnoreTransactionalMethods(ignoreTransactionalMethods);
                            wrapper.setTemporary(true);
                            mithraObjects.put(mithraTempObjectResourceType.getName(), wrapper);
                        }
                    }
                });
            }
            waitForExecutorWithCheck();
        }
        return mithraTempObjectList.size();
        */
        return 0;
    }

    private int parseMithraEmbeddedValueObjects()
    {
        /*
        List<MithraEmbeddedValueObjectResourceType> mithraEmbeddedValueObjectList = mithraType.getMithraEmbeddedValueObjectResources();
        if (!mithraEmbeddedValueObjectList.isEmpty())
        {
            chopAndStickResource.resetSerialResource();
            for (int i=0;i<mithraEmbeddedValueObjectList.size();i++)
            {
                final MithraEmbeddedValueObjectResourceType mithraEmbeddedValueObjectResourceType = mithraEmbeddedValueObjectList.get(i);
                getExecutor().submit(new GeneratorTask(i)
                {
                    public void run()
                    {
                        String objectName = mithraEmbeddedValueObjectResourceType.getName();
                        MithraEmbeddedValueObjectType evo = (MithraEmbeddedValueObjectType) parseMithraBaseObject(objectName, mithraEmbeddedValueObjects, fileProvider, this);
                        if (evo != null)
                        {
                            String objectFileName = objectName + ".xml";
                            MithraEmbeddedValueObjectTypeWrapper wrapper = new MithraEmbeddedValueObjectTypeWrapper(evo, objectFileName, importSource);
                            wrapper.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
                            mithraEmbeddedValueObjects.put(mithraEmbeddedValueObjectResourceType.getName(), wrapper);
                        }
                    }
                });
            }
            waitForExecutorWithCheck();
        }
        return mithraEmbeddedValueObjectList.size();
        */
        return 0;
    }

    private int parseMithraEnumerations()
    {
        /*
        List<MithraEnumerationResourceType> mithraEnumerationList = (List<MithraEnumerationResourceType>) mithraType.getMithraEnumerationResources();
        if (!mithraEnumerationList.isEmpty())
        {
            chopAndStickResource.resetSerialResource();
            for (int i=0;i<mithraEnumerationList.size();i++)
            {
                final MithraEnumerationResourceType mithraEnumerationResourceType = mithraEnumerationList.get(i);
                getExecutor().submit(new GeneratorTask(i)
                {
                    public void run()
                    {
                        String enumerationName = mithraEnumerationResourceType.getName();
                        MithraEnumerationType enumeration = (MithraEnumerationType) parseMithraBaseObject(enumerationName, mithraEnumerations, fileProvider, this);
                        if (enumeration != null)
                        {
                            String enumerationFileName = enumerationName + ".xml";
                            MithraEnumerationTypeWrapper wrapper = new MithraEnumerationTypeWrapper(enumeration, enumerationFileName, importSource);
                            mithraEnumerations.put(mithraEnumerationResourceType.getName(), wrapper);
                        }
                    }
                });
            }
            waitForExecutorWithCheck();
        }
        return mithraEnumerationList.size();
        */
        return 0;
    }

    private MithraInterfaceType parseMithraInterfaceType()
    {
        /*
        MithraInterfaceType mithraObject = (MithraInterfaceType) parseMithraType(objectName, objectMap, fileProvider, task);
        mithraObject.setReadOnlyInterfaces(isReadOnlyInterfaces);
        mithraObject.setImportedSource(importedSource);
        checkClassName(objectName, mithraObject.getClassName());
        mithraObject.postInitialize(objectName);
        return mithraObject;
        */
        return null;
    }

    private MithraBaseObjectType parseMithraObject()
    {
        //return parseMithraBaseObject(objectName, objectMap, fileProvider, task);
        return null;
    }

    private void checkClassName(String objectName, String className)
    {
        if (objectName.contains("/"))
        {
            objectName = objectName.substring(objectName.lastIndexOf('/') + 1);
        }
        if (!objectName.equals(className))
        {
            throw new MithraGeneratorException("XML filename: '" + objectName + "' must match class name specified: '" + className + "'");
        }
    }

    private int parseMithraInterfaceObjects() throws FileNotFoundException
    {
        /*
        List<MithraInterfaceResourceType> mithraObjectList = mithraType.getMithraInterfaceResources();
        chopAndStickResource.resetSerialResource();
        for (int i = 0; i < mithraObjectList.size(); i++)
        {
            final MithraInterfaceResourceType mithraObjectResourceType = mithraObjectList.get(i);
            final String objectName = mithraObjectResourceType.getName();
            getExecutor().submit(new GeneratorTask(i)
            {
                public void run()
                {
                    MithraInterfaceType mithraObject = parseMithraInterfaceType(objectName, mithraInterfaces, fileProvider, this, mithraObjectResourceType.isReadOnlyInterfaces(), importSource);
                    if (mithraObject != null)
                    {
                        mithraInterfaces.put(mithraObjectResourceType.getName(), mithraObject);
                    }
                }
            });
        }

        waitForExecutorWithCheck();
        return mithraObjectList.size();
        */
        return 0;
    }

    private Object parseMithraType()
    {
        /*
        Object mithraObject = null;
        this.logger.info("Reading " + objectName);
        if (!fileProvider.excludeObject(objectName))
        {
            if (objectMap.containsKey(objectName))
            {
                throw new MithraGeneratorException("Attempted to add object " + objectName + " twice");
            }
            String objectFileName = objectName + ".xml";
            InputStream objectFileIs = null;
            boolean serialAquired = false;
            try
            {
                MithraGeneratorImport.FileInputStreamWithSize streamWithSize = fileProvider.getFileInputStream(objectFileName);
                FullFileBuffer ffb = getFullFileBuffer();
                chopAndStickResource.acquireIoResource();
                try
                {
                    ffb.bufferFile(streamWithSize.getInputStream(), (int) streamWithSize.getSize());
                }
                finally
                {
                    chopAndStickResource.releaseIoResource();
                }
                task.acquireSerialResource();
                serialAquired = true;
                try
                {
                    ffb.updateCrc(crc32);
                }
                finally
                {
                    task.releaseSerialResource();
                }
                chopAndStickResource.acquireCpuResource();
                try
                {
                    objectFileIs = streamWithSize.getInputStream();
                    mithraObject = new MithraGeneratorUnmarshaller().parse(ffb.getBufferedInputStream(), objectFileName);
                }
                finally
                {
                    chopAndStickResource.releaseCpuResource();
                }
            }
            catch (FileNotFoundException e)
            {
                throw new MithraGeneratorException("Unable to find " + objectFileName, e);
            }
            catch (MithraGeneratorParserException e)
            {
                throw new MithraGeneratorException("Unable to parse " + objectFileName+" "+e.getMessage(), e);
            }
            catch (IOException e)
            {
                throw new MithraGeneratorException("Unable to read x" + objectFileName, e);
            }
            finally
            {
                if (!serialAquired)
                {
                    task.acquireSerialResource();
                    task.releaseSerialResource();
                }
                closeIs(objectFileIs);
            }
        }
        else
        {
            this.logger.info("Skipping " + objectName + ", excluded");
        }
        return mithraObject;
        */
        return null;
    }

    private MithraBaseObjectType parseMithraBaseObject()
    {
        /*
        MithraBaseObjectType mithraObject = (MithraBaseObjectType) parseMithraType(objectName, objectMap, fileProvider, task);
        checkClassName(objectName, mithraObject.getClassName());
        return mithraObject;
        */

        return null;
    }


    private String concatParsed(String msg, int count, String type)
    {
        if (count > 0)
        {
            msg += count + " " +type+", ";
        }
        return msg;
    }

    /*
    private void waitForExecutorWithCheck()
    {
        getExecutor().waitUntilDone();
        if (executorError != null)
        {
            throw new MithraGeneratorException("exception while generating", executorError);
        }
    }
    */

    private void closeIs(InputStream is)
    {
        if (is != null)
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                throw new MithraGeneratorException("Exception closing InputStream",e);
            }
        }
    }

    public ChopAndStickResource getChopAndStickResource()
    {
        return chopAndStickResource;
    }

    /*
    public AwaitingThreadExecutor getExecutor()
    {
        if (executor == null)
        {
            executor = new AwaitingThreadExecutor(Runtime.getRuntime().availableProcessors()+IO_THREADS, "Mithra Generator");
            executor.setExceptionHandler(new AutoShutdownThreadExecutor.ExceptionHandler() {
                public void handleException(AutoShutdownThreadExecutor executor, Runnable target, Throwable exception)
                {
                    executor.shutdownNow();
                    MithraXMLObjectTypeParser.this.logger.error("Error in runnable target. Shutting down queue "+exception.getClass().getName()+" :"+exception.getMessage());
                    executorError = exception;
                }
            });
        }
        return executor;
    }
    */

    private class ParentClassComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            MithraObjectTypeWrapper left = (MithraObjectTypeWrapper) o1;
            MithraObjectTypeWrapper right = (MithraObjectTypeWrapper) o2;

            Map<String, MithraObjectTypeWrapper> mithraObjects = AnnotationParser.this.getMithraObjects();
            int result = left.getHierarchyDepth(mithraObjects) - right.getHierarchyDepth(mithraObjects);
            if (result == 0)
            {
                result = left.getClassName().compareTo(right.getClassName());
            }
            return result;
        }

    }

    public abstract class GeneratorTask implements Runnable
    {
        private int resourceNumber;

        public GeneratorTask(int resourceNumber)
        {
            this.resourceNumber = resourceNumber;
        }

        public void acquireSerialResource()
        {
            getChopAndStickResource().acquireSerialResource(resourceNumber);
        }

        public void releaseSerialResource()
        {
            getChopAndStickResource().releaseSerialResource();
        }
    }

    private void createSortedList()
    {
        this.sortedMithraObjects = new ArrayList<MithraObjectTypeWrapper>(this.mithraObjects.values());
        Collections.sort(this.sortedMithraObjects, new ParentClassComparator());
    }

    private void createSortedEmbeddedValueObjectList()
    {
        this.sortedMithraEmbeddedValueObjects = new ArrayList<MithraEmbeddedValueObjectTypeWrapper>(this.mithraEmbeddedValueObjects.values());
    }

    public List<MithraEmbeddedValueObjectTypeWrapper> getSortedMithraEmbeddedValueObjects()
    {
        return sortedMithraEmbeddedValueObjects;
    }

    private void createSortedEnumerationsList()
    {
        this.sortedMithraEnumerations = new ArrayList<MithraEnumerationTypeWrapper>(this.mithraEnumerations.values());
    }

    private FullFileBuffer getFullFileBuffer()
    {
        FullFileBuffer result = fullFileBufferThreadLocal.get();
        if (result == null)
        {
            result = new FullFileBuffer();
            fullFileBufferThreadLocal.set(result);
        }
        return result;
    }
}
