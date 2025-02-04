/**
 * Copyright 2003 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.Attributes;

import static java.lang.System.getLogger;


/**
 * Provides access to voices for all of FreeTTS. There is only one instance of
 * the VoiceManager.
 * <p>
 * Each call to getVoices() creates a new instance of each voice.
 *
 * @see Voice
 * @see VoiceDirectory
 */
public class VoiceManager {

    private static final Logger logger = getLogger(VoiceManager.class.getName());

    private static final VoiceManager INSTANCE;

    private static final String PATH_SEPARATOR;

    /**
     * we only want one class loader, otherwise the static information for
     * loaded classes would be duplicated for each class loader
     */
    private static final DynamicClassLoader CLASSLOADER;

    static {
        PATH_SEPARATOR = File.pathSeparator;
        INSTANCE = new VoiceManager();
        ClassLoader parent = VoiceManager.class.getClassLoader();
        CLASSLOADER = new DynamicClassLoader(new URI[0], parent);
    }

    /**
     * Do not allow creation from outside.
     */
    private VoiceManager() {
    }

    /**
     * Gets the instance of the VoiceManager
     *
     * @return a VoiceManager
     */
    public static VoiceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Provide an array of all voices available to FreeTTS.
     * <p>
     * First, if the "freetts.voices" property is set, it is assumed to be a
     * comma-separated list of VoiceDirectory classnames (e.g.,
     * "-Dfreetts.voices=com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"
     * ). If this property exists, the VoiceManager will use only this property
     * to find voices -- no other method described below will be used. The
     * primary purpose for this property is testing and for use with WebStart.
     *
     * <p>
     * Second, the file internal_voices.txt is looked for in the same directory
     * as VoiceManager.class. If the file does not exist, the VoiceManager moves
     * on. Next, it looks for voices.txt in the same directory as freetts.jar.
     * If the file does not exist, the VoiceManager moves on. Next, if the
     * property "freetts.voicesfile" is defined, then that file is read in. If
     * the property is defined and the file does not exist, then an error is
     * raised.
     *
     * <p>
     * Every voice's file that is read in contains a list of VoiceDirectory class
     * names.
     *
     * <p>
     * Next, the voice manager looks for freetts voice jarfiles that may exist
     * in well-known locations. The directory that contains freetts.jar is
     * searched for voice jarfiles, then directories specified by the
     * "freetts.voicespath" system property. Any jarfile whose Manifest contains
     * "FreeTTSVoiceDefinition: true" is assumed to be a FreeTTS voice, and the
     * Manifest's "Main-Class" entry is assumed to be the name of the voice
     * directory. The dependencies of the voice jarfiles specified by the
     * "Class-Path" Manifest entry are also loaded.
     *
     * <p>
     * The VoiceManager instantiates each voice directory and calls getVoices()
     * on each.
     *
     * @return the array of new instances of all available voices
     */
    public Voice[] getVoices() {
        UniqueVector<Voice> voices = new UniqueVector<>();
        Collection<VoiceDirectory> voiceDirectories;
        try {
            voiceDirectories = getVoiceDirectories();
        } catch (IOException e) {
            throw new Error(e.getMessage(), e);
        }
        for (VoiceDirectory dir : voiceDirectories) {
            voices.addArray(dir.getVoices());
        }

        Voice[] voiceArray = new Voice[voices.size()];
        return voices.toArray(voiceArray);
    }

    /**
     * Prints detailed information about all available voices.
     *
     * @return a String containing the information
     */
    public String getVoiceInfo() {
        StringBuilder infoString = new StringBuilder();
        Collection<VoiceDirectory> voiceDirectories;
        try {
            voiceDirectories = getVoiceDirectories();
        } catch (IOException e) {
            throw new Error(e.getMessage(), e);
        }
        for (VoiceDirectory dir : voiceDirectories) {
            infoString.append(dir.toString());
        }
        return infoString.toString();
    }

    /**
     * Creates an array of all voice directories of all available voices using
     * the criteria specified by the contract for {@link #getVoices()}.
     *
     * @return the voice directories
     * @throws IOException error loading a voice directory
     * @see #getVoices()
     */
    private Collection<VoiceDirectory> getVoiceDirectories() throws IOException {
        try {
            // If there is a freetts.voices property, it means two
            // things: 1) it is a comma separated list of class names
            // 2) no other attempts to find voices should be
            // made
            //
            // The main purpose for this property is to allow for
            // voices to be found via WebStart.
            //
            String voiceClasses = System.getProperty("freetts.voices");
            if (voiceClasses != null) {
                return getVoiceDirectoryNamesFromProperty(voiceClasses);
            }

            // Get voice directory names from voices files
            UniqueVector<String> voiceDirectoryNames = getVoiceDirectoryNamesFromFiles();

            // Get list of voice jars
            UniqueVector<URI> pathURLs = getVoiceJarURLs();
            voiceDirectoryNames.addVector(getVoiceDirectoryNamesFromJarURLs(pathURLs));

            // Get dependencies
            // Copy of vector made because vector may be modified by
            // each call to getDependencyURLs
            URI[] voiceJarURLs = pathURLs.toArray(new URI[pathURLs.size()]);
            for (URI voiceJarURL : voiceJarURLs) {
                getDependencyURLs(voiceJarURL, pathURLs);
            }

            // If the voice jars have already been added to the classpath
            // we avoid to add them a second time.
            boolean noexpansion = Boolean.getBoolean("freetts.nocpexpansion");
            if (!noexpansion) {
                // Extend class path
                for (int i = 0; i < pathURLs.size(); i++) {
                    CLASSLOADER.addUniqueURL(pathURLs.get(i));
                }
            }

            // Create an instance of each voice directory
            UniqueVector<VoiceDirectory> voiceDirectories = new UniqueVector<>();
            ServiceLoader<VoiceDirectory> directories = ServiceLoader.load(VoiceDirectory.class);
            for (VoiceDirectory directory : directories) {
                voiceDirectories.add(directory);
            }

            for (int i = 0; i < voiceDirectoryNames.size(); i++) {
                @SuppressWarnings("unchecked")
                Class<VoiceDirectory> c = (Class<VoiceDirectory>) Class.forName(
                        voiceDirectoryNames.get(i), true, CLASSLOADER);
                voiceDirectories.add(c.getDeclaredConstructor().newInstance());
            }

            return voiceDirectories.elements();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new Error("Unable to load voice directory. " + e, e);
        }
    }

    /**
     * Gets VoiceDirectory instances by parsing a comma separated String of
     * VoiceDirectory class names.
     */
    private static Collection<VoiceDirectory> getVoiceDirectoryNamesFromProperty(String voiceClasses)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

        String[] classnames = voiceClasses.split(",");

        Collection<VoiceDirectory> directories = new java.util.ArrayList<>();

        for (String classname : classnames) {
            @SuppressWarnings("unchecked")
            Class<VoiceDirectory> c = (Class<VoiceDirectory>) CLASSLOADER.loadClass(classname);
            directories.add(c.getDeclaredConstructor().newInstance());
        }

        return directories;
    }

    /**
     * Recursively gets the urls of the class paths that url is dependent on.
     * <p>
     * Conventions specified in
     * http://java.sun.com/j2se/1.4.1/docs/guide/extensions/spec.html#bundled
     * are followed.
     *
     * @param url            the url to recursively check. If it ends with a "/" then it is
     *                       presumed to be a directory, and is not checked. Otherwise it
     *                       is assumed to be a jar, and its manifest is read to get the
     *                       urls Class-Path entry. These urls are passed to this method
     *                       recursively.
     * @param dependencyURLs a vector containing all of the dependent urls found. This
     *                       parameter is modified as urls are added to it.
     * @throws IOException error openig the URL connection
     */
    private static void getDependencyURLs(URI url, UniqueVector<URI> dependencyURLs) throws IOException {
        String urlDirName = getURLDirName(url);
        if (url.getScheme().equals("jar")) { // only check deps of jars

            // read in Class-Path attribute of jar Manifest
            JarURLConnection jarConnection = (JarURLConnection) url.toURL().openConnection();
            Attributes attributes = jarConnection.getMainAttributes();
            String fullClassPath = attributes.getValue(Attributes.Name.CLASS_PATH);
            if (fullClassPath == null || fullClassPath.isEmpty()) {
                return; // no classpaths to add
            }

            // The URLs are separated by one or more spaces
            String[] classPath = fullClassPath.split("\\s+");
            URI classPathURL;
            for (String s : classPath) {
                if (s.endsWith("/")) { // assume directory
                    classPathURL = URI.create("file:" + urlDirName + s);
                } else { // assume jar
                    classPathURL = URI.create("jar:file:" + urlDirName + s + "!/");
                }

                // don't get in a recursive loop if two jars
                // are mutually dependant
                if (!dependencyURLs.contains(classPathURL)) {
                    dependencyURLs.add(classPathURL);
                    getDependencyURLs(classPathURL, dependencyURLs);
                }
            }
        }
    }

    /**
     * Gets the names of the subclasses of VoiceDirectory that are listed in the
     * voices.txt files.
     *
     * @return a vector containing the String names of the voice directories
     * @throws IOException error reading voice files.
     */
    private UniqueVector<String> getVoiceDirectoryNamesFromFiles() throws IOException {
        UniqueVector<String> voiceDirectoryNames = new UniqueVector<>();

        // first, load internal_voices.txt
        InputStream is = this.getClass().getResourceAsStream("internal_voices.txt");
        if (is != null) { // if it doesn't exist, move on
            UniqueVector<String> voices = getVoiceDirectoryNamesFromInputStream(is);
            voiceDirectoryNames.addVector(voices);
        }

        // next, try loading voices.txt
        try {
            voiceDirectoryNames.addVector(getVoiceDirectoryNamesFromFile(getBaseDirectory() + "voices.txt"));
        } catch (IOException e) {
            logger.log(Level.TRACE, e.getMessage(), e);
            // do nothing
        }

        // last, read voices from property freetts.voicesfile
        String voicesFile = System.getProperty("freetts.voicesfile");
        if (voicesFile != null) {
            voiceDirectoryNames.addVector(getVoiceDirectoryNamesFromFile(voicesFile));
        }

        return voiceDirectoryNames;
    }

    /**
     * Gets the voice directory class names from a list of urls specifying voice
     * jarfiles. The class name is specified as the Main-Class in the manifest
     * of the jarfiles.
     *
     * @param urls a UniqueVector of URLs that refer to the voice jarfiles
     * @return a UniqueVector of Strings representing the voice directory class
     * names
     */
    private static UniqueVector<String> getVoiceDirectoryNamesFromJarURLs(UniqueVector<URI> urls) {
        try {
            UniqueVector<String> voiceDirectoryNames = new UniqueVector<>();
            for (int i = 0; i < urls.size(); i++) {
                JarURLConnection jarConnection = (JarURLConnection) urls.get(i).toURL().openConnection();
                Attributes attributes = jarConnection.getMainAttributes();
                String mainClass = attributes.getValue(Attributes.Name.MAIN_CLASS);
                if (mainClass == null || mainClass.trim().isEmpty()) {
                    throw new Error("No Main-Class found in jar " + urls.get(i));
                }

                voiceDirectoryNames.add(mainClass);
            }
            return voiceDirectoryNames;
        } catch (IOException e) {
            throw new Error("Error reading jarfile manifests.");
        }
    }

    /**
     * Gets the list of voice jarfiles. Voice jarfiles are searched for in the
     * same directory as freetts.jar and the directories specified by the
     * freetts.voicespath system property. Voice jarfiles are defined by the
     * manifest entry "FreeTTSVoiceDefinition: true"
     *
     * @return a vector of URLs refering to the voice jarfiles.
     */
    private UniqueVector<URI> getVoiceJarURLs() {
        UniqueVector<URI> voiceJarURLs = new UniqueVector<>();

        // check in same directory as freetts.jar
        try {
            String baseDirectory = getBaseDirectory();
            if (!baseDirectory.isEmpty()) { // not called from a jar
                voiceJarURLs.addVector(getVoiceJarURLsFromDir(baseDirectory));
            }
        } catch (IOException e) {
            logger.log(Level.TRACE, e.getMessage(), e);
            // do nothing
        }

        // search voicespath
        String voicesPath = System.getProperty("freetts.voicespath", "");
        if (!voicesPath.isEmpty()) {
            String[] dirNames = voicesPath.split(PATH_SEPARATOR);
            for (String dirName : dirNames) {
                try {
                    voiceJarURLs.addVector(getVoiceJarURLsFromDir(dirName));
                } catch (FileNotFoundException e) {
                    throw new Error("Error loading jars from voicespath " + dirName + ". ");
                }
            }
        }

        return voiceJarURLs;
    }

    /**
     * Gets the list of voice jarfiles in a specific directory.
     *
     * @return a vector of URLs refering to the voice jarfiles
     * @see #getVoiceJarURLs()
     */
    private static UniqueVector<URI> getVoiceJarURLsFromDir(String dirName)
            throws FileNotFoundException {
        try {
            UniqueVector<URI> voiceJarURLs = new UniqueVector<>();
            File dir = new File(URI.create("file://" + dirName));
            if (!dir.isDirectory()) {
                throw new FileNotFoundException("File is not a directory: " + dirName);
            }
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isFile() && (!file.isHidden()) && file.getName().endsWith(".jar")) {
                    URI jarURL = file.toURI();
                    jarURL = URI.create("jar:file:" + jarURL.getPath() + "!/");
                    JarURLConnection jarConnection = (JarURLConnection) jarURL.toURL().openConnection();
                    // if it is not a real jar file, we will end up
                    // with a null set of attributes.

                    Attributes attributes = jarConnection.getMainAttributes();
                    if (attributes != null) {
                        String isVoice = attributes.getValue("FreeTTSVoiceDefinition");
                        if (isVoice != null && isVoice.trim().equals("true")) {
                            voiceJarURLs.add(jarURL);
                        }
                    }
                }
            }
            return voiceJarURLs;
        } catch (IOException e) {
            throw new Error("Error reading jars from directory " + dirName + ". ");
        }
    }

    /**
     * Provides a string representation of all voices available to FreeTTS.
     *
     * @return a String which is a space-delimited list of voice names. If there
     * is more than one voice, then the word "or" appears before the
     * last one.
     */
    public String toString() {
        StringBuilder names = new StringBuilder();
        Voice[] voices = getVoices();
        for (int i = 0; i < voices.length; i++) {
            if (i == voices.length - 1) {
                if (i == 0) {
                    names.append(voices[i].getName());
                } else {
                    names.append("or ");
                    names.append(voices[i].getName());
                }
            } else {
                names.append(voices[i].getName());
                names.append(" ");
            }
        }
        return names.toString();
    }

    /**
     * Check if there is a voice provides with the given name.
     *
     * @param voiceName the name of the voice to check
     * @return <b>true</b> if FreeTTS has a voice available with the name
     * <b>voiceName</b>, else <b>false</b>.
     */
    public boolean contains(String voiceName) {
        return (getVoice(voiceName) != null);
    }

    /**
     * Get a Voice with a given name.
     *
     * @param voiceName the name of the voice to get.
     * @return the Voice that has the same name as <b>voiceName</b> if one
     * exists, else <b>null</b>
     */
    public Voice getVoice(String voiceName) {
        Voice[] voices = getVoices();
        for (Voice voice : voices) {
            if (voice.getName().equals(voiceName)) {
                return voice;
            }
        }
        return null;
    }

    /**
     * Get the directory that the jar file containing this class resides in.
     *
     * @return the name of the directory with a trailing "/" (or equivalent for
     * the particular operating system), or "" if unable to determine.
     * (For example this class does not reside inside a jar file).
     */
    private String getBaseDirectory() throws IOException {
        try {
            String name = this.getClass().getName();
            int lastdot = name.lastIndexOf('.');
            if (lastdot != -1) { // remove package information
                name = name.substring(lastdot + 1);
            }

            URI url = this.getClass().getResource(name + ".class").toURI();
            return getURLDirName(url);
        } catch (NullPointerException | URISyntaxException | MalformedURLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Gets the directory name from a URL
     *
     * @param url the url to parse
     * @return the String representation of the directory name in a URL
     */
    private static String getURLDirName(URI url) throws MalformedURLException {
        String urlFileName = url.toURL().getPath();
        int i = urlFileName.lastIndexOf('!');
        if (i == -1) {
            i = urlFileName.length();
        }
        int dir = urlFileName.lastIndexOf("/", i);
        if (!urlFileName.startsWith("file:")) {
            return "";
        }
        return urlFileName.substring(5, dir) + "/";
    }

    /**
     * Get the names of the voice directories from a voices file. Blank lines
     * and lines beginning with "#" are ignored. Beginning and trailing
     * whitespace is ignored.
     *
     * @param fileName the name of the voices file to read from
     * @return a vector of the names of the VoiceDirectory subclasses
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static UniqueVector<String> getVoiceDirectoryNamesFromFile(String fileName)
            throws FileNotFoundException, IOException {
        InputStream is = Files.newInputStream(Paths.get(fileName));
        return getVoiceDirectoryNamesFromInputStream(is);
    }

    /**
     * Get the names of the voice directories from an input stream. Blank lines
     * and lines beginning with "#" are ignored. Beginning and trailing
     * whitespace is ignored.
     *
     * @param is the input stream to read from
     * @return a vector of the names of the VoiceDirectory subclasses
     * @throws IOException error reading from the input stream
     */
    private static UniqueVector<String> getVoiceDirectoryNamesFromInputStream(InputStream is) throws IOException {
        UniqueVector<String> names = new UniqueVector<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (!line.startsWith("#") && !line.isEmpty()) {
                names.add(line);
            }
        }
        return names;
    }

    /**
     * Gets the class loader used for loading dynamically detected jars. This is
     * useful to get resources out of jars that may be in the class path of this
     * class loader but not in the class path of the system class loader.
     *
     * @return the class loader
     */
    public static URLClassLoader getVoiceClassLoader() {
        return CLASSLOADER;
    }
}


/**
 * The DynamicClassLoader provides a means to add urls to the classpath after
 * the class loader has already been instantiated.
 */
class DynamicClassLoader extends URLClassLoader {

    private java.util.HashSet<URI> classPath;

    /**
     * Constructs a new URLClassLoader for the given URLs. The URLs will be
     * searched in the order specified for classes and resources after first
     * searching in the specified parent class loader. Any URL that ends with a
     * '/' is assumed to refer to a directory. Otherwise, the URL is assumed to
     * refer to a JAR file which will be downloaded and opened as needed.
     * <p>
     * If there is a security manager, this method first calls the security
     * manager's checkCreateClassLoader method to ensure creation of a class
     * loader is allowed.
     *
     * @param urls   the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @throws SecurityException if a security manager exists and its checkCreateClassLoader
     *                           method doesn't allow creation of a class loader.
     */
    public DynamicClassLoader(URI[] urls, ClassLoader parent) {
        super(Arrays.stream(urls)
                .map(uri -> { try { return uri.toURL(); } catch (MalformedURLException e) { throw new UncheckedIOException(e); }})
                .toArray(URL[]::new), parent);
        classPath = new HashSet<>(urls.length);
        Collections.addAll(classPath, urls);
    }

    /**
     * Add a URL to a class path only if it has not already been added.
     *
     * @param url the url to add to the class path
     */
    public synchronized void addUniqueURL(URI url) throws MalformedURLException {
        // Avoid loading of the freetts.jar.
        String name = url.toString();
        if (!classPath.contains(url) && (!name.contains("freetts.jar"))) {
            super.addURL(url.toURL());
            classPath.add(url);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                loadedClass = findClass(name);
            } catch (ClassNotFoundException e) {
                // Swallow exception
                // does not exist locally
            }
            if (loadedClass == null) {
                loadedClass = super.loadClass(name);
            }
        }
        return loadedClass;
    }
}


/**
 * Provides a vector whose elements are always unique. The advantage over a Set
 * is that the elements are still ordered in the way they were added. If an
 * element is added that already exists, then nothing happens.
 */
class UniqueVector<T> {

    private Set<T> elementSet;
    private List<T> elementVector;

    /**
     * Creates a new vector
     */
    public UniqueVector() {
        elementSet = new HashSet<>();
        elementVector = new ArrayList<>();
    }

    /**
     * Add an object o to the vector if it is not already present as defined by
     * the function HashSet.contains(o)
     *
     * @param o the object to add
     */
    public void add(T o) {
        if (!contains(o)) {
            elementSet.add(o);
            elementVector.add(o);
        }
    }

    /**
     * Appends all elements of a vector to this vector. Only unique elements are
     * added.
     *
     * @param v the vector to add
     */
    public void addVector(UniqueVector<T> v) {
        for (int i = 0; i < v.size(); i++) {
            add(v.get(i));
        }
    }

    /**
     * Appends all elements of an array to this vector. Only unique elements are
     * added.
     *
     * @param a the array to add
     */
    public void addArray(T[] a) {
        for (T t : a) {
            add(t);
        }
    }

    /**
     * Gets the number of elements currently in vector.
     *
     * @return the number of elements in vector
     */
    public int size() {
        return elementVector.size();
    }

    /**
     * Checks if an element is present in the vector. The check follows the
     * convention of HashSet contains() function, so performance can be expected
     * to be a constant factor.
     *
     * @param o the object to check
     * @return true if element o exists in the vector, else false.
     */
    public boolean contains(T o) {
        return elementSet.contains(o);
    }

    /**
     * Gets an element from a vector.
     *
     * @param index the index into the vector from which to retrieve the element
     * @return the object at index <b>index</b>
     */
    public T get(int index) {
        return elementVector.get(index);
    }

    /**
     * Creates an array of the elements in the vector. Follows conventions of
     * Vector.toArray().
     *
     * @return an array representation of the object
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        return (T[]) elementVector.toArray();
    }

    /**
     * Creates an array of the elements in the vector. Follows conventions of
     * Vector.toArray(Object[]).
     *
     * @return an array representation of the object
     */
    public T[] toArray(T[] a) {
        return elementVector.toArray(a);
    }

    /**
     * Returns the entries of this vector.
     *
     * @return elements.
     */
    public Collection<T> elements() {
        return elementVector;
    }
}
