package com.dingding.milou.scanner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.collections4.EnumerationUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PackageScanner {

    private PackageScanner() {
    }

    /**
     * URL protocol for an entry from a jar file: "jar"
     */
    private static final String URL_PROTOCOL_JAR = "jar";
    /**
     * URL protocol for an entry from a zip file: "zip"
     */
    private static final String URL_PROTOCOL_ZIP = "zip";
    /**
     * URL protocol for an entry from a WebSphere jar file: "wsjar"
     */
    private static final String URL_PROTOCOL_WSJAR = "wsjar";
    /**
     * URL protocol for an entry from a JBoss jar file: "vfszip"
     */
    private static final String URL_PROTOCOL_VFSZIP = "vfszip";
    /**
     * URL protocol for an entry from an OC4J jar file: "code-source"
     */
    private static final String URL_PROTOCOL_CODE_SOURCE = "code-source";
    /**
     * Separator between JAR URL and file path within the JAR
     */
    private static final String JAR_URL_SEPARATOR = "!/";
    /**
     * URL prefix for loading from the file system: "file:"
     */
    private static final String FILE_URL_PREFIX = "file:";
    /**
     * 文件夹隔离符。
     */
    private static final String FOLDER_SEPARATOR = "/";

    /**
     * 递归加载 packageName 指定的包名下面的所有的类。
     */
    public static List<Class<?>> getStubClass(String packageName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        try {
            ClassPathResource[] resources = getClassPathResources(StringUtils.replace(packageName, ".", "/"), cl);
            List<Class<?>> result = Lists.newArrayList();
            for (ClassPathResource resource : resources) {
                String urlPath = resource.getUrl().getPath();
                if (!urlPath.endsWith(".class")) {
                    continue;
                }
                Class<?> cls = resolveClass(cl, resource);
                if (cls != null) {
                    result.add(cls);
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("load stub resource error");
        }
    }

    /**
     * 递归地在类路径中以指定的类加载器获取 dirPath 指定的目录下面所有的资源。
     */
    public static ClassPathResource[] getClassPathResources(String dirPath, ClassLoader cl) throws IOException {
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        URL[] roots = getRoots(dirPath, cl);
        Set<ClassPathResource> result = new LinkedHashSet<ClassPathResource>(16);
        for (URL root : roots) {
            if (isJarResource(root)) {
                result.addAll(doFindPathMatchingJarResources(root));
            } else {
                result.addAll(doFindPathMatchingFileResources(root, dirPath));
            }
        }
        return result.toArray(new ClassPathResource[result.size()]);
    }

    private static URL[] getRoots(String dirPath, ClassLoader cl) throws IOException {
        Enumeration<URL> resources = cl.getResources(dirPath);
        List<URL> resourceUrls = EnumerationUtils.toList(resources);
        return resourceUrls.toArray(new URL[resourceUrls.size()]);
    }

    private static Collection<ClassPathResource> doFindPathMatchingJarResources(URL rootUrl) throws IOException {
        URLConnection con = rootUrl.openConnection();
        JarFile jarFile;
        String rootEntryPath;
        boolean newJarFile = false;
        if (con instanceof JarURLConnection) {
            // Should usually be the case for traditional JAR files.
            JarURLConnection jarCon = (JarURLConnection) con;
            jarCon.setUseCaches(true);
            jarFile = jarCon.getJarFile();
            JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
        } else {
            // No JarURLConnection -> need to resort to URL file parsing.
            // We'll assume URLs of the format "jar:path!/entry", with the protocol
            // being arbitrary as long as following the entry format.
            // We'll also handle paths with and without leading "file:" prefix.
            String urlFile = rootUrl.getFile();
            int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
            if (separatorIndex != -1) {
                String jarFileUrl = urlFile.substring(0, separatorIndex);
                rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
                jarFile = getJarFile(jarFileUrl);
            } else {
                jarFile = new JarFile(urlFile);
                rootEntryPath = "";
            }
            newJarFile = true;
        }
        try {
            if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
                // Root entry path must end with slash to allow for proper matching.
                // The Sun JRE does not return a slash here, but BEA JRockit does.
                rootEntryPath = rootEntryPath + "/";
            }
            Set<ClassPathResource> result = new LinkedHashSet<ClassPathResource>(8);
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    String relativePath = entryPath.substring(rootEntryPath.length());
                    String rootPath = rootUrl.getPath();
                    rootPath = rootPath.endsWith("/") ? rootPath : rootPath + "/";
                    String newPath = applyRelativePath(rootPath, relativePath);
                    String classPathPath = applyRelativePath(rootEntryPath, relativePath);
                    result.add(new ClassPathResource(new URL(newPath), classPathPath));
                }
            }
            return result;
        } finally {
            // Close jar file, but only if freshly obtained -
            // not from JarURLConnection, which might cache the file reference.
            if (newJarFile) {
                jarFile.close();
            }
        }
    }

    private static Collection<ClassPathResource> doFindPathMatchingFileResources(URL rootUrl, String dirPath)
            throws IOException {
        String filePath = rootUrl.getFile();
        File file = new File(filePath);
        File rootDir = file.getAbsoluteFile();
        return doFindMatchingFileSystemResources(rootDir, dirPath);
    }

    private static Collection<ClassPathResource> doFindMatchingFileSystemResources(File rootDir, String dirPath)
            throws IOException {
        Set<File> allFiles = Sets.newLinkedHashSet();
        retrieveAllFiles(rootDir, allFiles);
        String classPathRoot = parseClassPathRoot(rootDir, dirPath);
        Set<ClassPathResource> result = new LinkedHashSet<ClassPathResource>(allFiles.size());
        for (File file : allFiles) {
            String absolutePath = file.getAbsolutePath();
            URL url = new URL("file:///" + absolutePath);
            String classPathPath = absolutePath.substring(classPathRoot.length());
            classPathPath = StringUtils.replace(classPathPath, "\\", "/");
            result.add(new ClassPathResource(url, classPathPath));
        }
        return result;
    }

    private static String parseClassPathRoot(File rootDir, String dirPath) {
        String absolutePath = rootDir.getAbsolutePath();
        absolutePath = StringUtils.replace(absolutePath, "\\", "/");
        int lastIndex = absolutePath.lastIndexOf(dirPath);
        String result = absolutePath.substring(0, lastIndex);
        if (!result.endsWith("/")) {
            result = result + "/";
        }
        return result;
    }

    private static Class<?> resolveClass(ClassLoader cl, ClassPathResource resource) {
        String className = resolveClassName(resource);
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        List<Class<?>> list = getStubClass("com.dingding");
        for (Class<?> class1 : list) {
            System.out.println(class1.getName());
        }
    }

    private static void retrieveAllFiles(File dir, Set<File> allFiles) {
        File[] subFiles = dir.listFiles();
        assert subFiles != null;
        allFiles.addAll(Arrays.asList(subFiles));
        for (File subFile : subFiles) {
            if (subFile.isDirectory()) {
                retrieveAllFiles(subFile, allFiles);
            }
        }
    }

    private static String applyRelativePath(String path, String relativePath) {
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
                newPath += FOLDER_SEPARATOR;
            }
            return newPath + relativePath;
        } else {
            return relativePath;
        }
    }

    private static JarFile getJarFile(String jarFileUrl) throws IOException {
        if (jarFileUrl.startsWith(FILE_URL_PREFIX)) {
            try {
                return new JarFile(toURI(jarFileUrl).getSchemeSpecificPart());
            } catch (URISyntaxException ex) {
                // Fallback for URLs that are not valid URIs (should hardly ever happen).
                return new JarFile(jarFileUrl.substring(FILE_URL_PREFIX.length()));
            }
        } else {
            return new JarFile(jarFileUrl);
        }
    }

    private static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location, " ", "%20"));
    }

    private static boolean isJarResource(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol) ||
                URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol) || (URL_PROTOCOL_CODE_SOURCE
                .equals(protocol) && url.getPath().contains(JAR_URL_SEPARATOR)));
    }

    private static String resolveClassName(ClassPathResource resource) {
        String path = resource.getClassPath();
        String className = path.substring(0, path.length() - ".class".length());
        className = StringUtils.replace(className, "/", ".");
        return className;
    }

    public static class ClassPathResource {

        private URL url;

        private String classPath;

        public ClassPathResource(URL url, String classPath) {
            this.url = url;
            this.classPath = classPath;
        }

        public URL getUrl() {
            return url;
        }

        public String getClassPath() {
            return classPath;
        }
    }

}
