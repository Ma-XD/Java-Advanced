package info.kgeorgiy.ja.dziubenko.implementor;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Generates class implementation by {@link Class type token} of interface to <var>.java</var> or <var>.jar</var> file.
 *
 * @author Max Dziubenko
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Tests input {@link Class token} and {@link Path root} before starting implementation.
     *
     * @param token interface to be implemented
     * @param root  directory where implementation will be saved
     * @throws ImplerException if parameters are null or token is not interface or is private
     */
    private static void testInput(final Class<?> token, final Path root) throws ImplerException {
        if (token == null) {
            throw new ImplerException("Token is null");
        }
        if (root == null) {
            throw new ImplerException("Root is null");
        }
        if (!token.isInterface()) {
            throw new ImplerException("Input class is not interface");
        }
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Input interface is private");
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        testInput(token, root);

        final Path outFile = getOutFile(root, token, ".java");

        createParentDirectory(outFile);
        generateCode(outFile, token);
    }

    /**
     * Generates {@link Path} of implementation class file.
     *
     * @param token         interface to be implemented
     * @param root          directory where implementation will be saved
     * @param fileExtension file extension
     * @return {@link Path} of implementation class file
     * @throws ImplerException if converting to path was failed
     */
    private static Path getOutFile(final Path root, final Class<?> token, final String fileExtension) throws ImplerException {
        try {
            return root.resolve(
                    generateClassFullName(token).replace('.', File.separatorChar)
                            + fileExtension);
        } catch (InvalidPathException e) {
            throw new ImplerException("Can't get path of out file: " + e.getMessage(), e);
        }
    }

    /**
     * Generates {@link String} simple name of implementation class.
     *
     * @param token interface to be implemented
     * @return {@link String} simple name
     */
    private static String generateClassSimpleName(final Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Generates {@link String} full name of implementation class.
     *
     * @param token interface to be implemented
     * @return {@link String} full name
     */
    private static String generateClassFullName(final Class<?> token) {
        return token.getPackageName() + "." + generateClassSimpleName(token);
    }

    /**
     * Tries to create parent directory of implementation class.
     *
     * @param path of implementation class file
     */

    private static void createParentDirectory(final Path path) {
        final Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException | SecurityException ignored) {
            }
        }
    }

    /**
     * Writes generated implementation code to file.
     *
     * @param outFile path of implementation class file
     * @param token   interface to be implemented
     * @throws ImplerException if writing errors
     */
    private static void generateCode(final Path outFile, final Class<?> token) throws ImplerException {
        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            writer.write(generatePackage(token));
            writer.write(generateClassSignature(token));
            for (Method method : getNotStaticMethods(token)) {
                writer.newLine();
                writer.write(generateMethod(method));
            }
            writer.write("}");
        } catch (IOException | SecurityException e) {
            throw new ImplerException("Can't write to file: " + e.getMessage(), e);
        }
    }

    /**
     * Generates {@link String} package of implementation class.
     *
     * @param token interface to be implemented
     * @return {@link String} of package
     */
    private static String generatePackage(final Class<?> token) {
        return format("""
                        package %s;
                        						
                        """,
                token.getPackageName());
    }

    /**
     * Generates {@link String} class signature of implementation class.
     *
     * @param token interface to be implemented
     * @return {@link String} of class signature
     */
    private static String generateClassSignature(final Class<?> token) {
        return format("""
                        public class %s implements %s {
                        """,
                generateClassSimpleName(token),
                token.getCanonicalName());
    }

    /**
     * Gets {@link List list} of not static methods in {@link Class token} class.
     *
     * @param token interface to be implemented
     * @return {@link List} of not static {@link Method methods}
     */
    private static List<Method> getNotStaticMethods(final Class<?> token) {
        return Arrays.stream(token.getMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .toList();
    }

    /**
     * Generates {@link String} default realization of method.
     *
     * @param method to be realized
     * @return {@link String} of method realization
     */

    private static String generateMethod(final Method method) {
        return format("""
                            public %s %s(%s)%s {
                                %s
                            }
                        """,
                method.getReturnType().getCanonicalName(),
                method.getName(),
                generateParameters(method),
                generateThrowsExceptions(method),
                generateReturn(method));
    }

    /**
     * Generates {@link String} exceptions in realizing method signature if there exist.
     *
     * @param method to be realized
     * @return {@link String} of method exceptions or empty
     */

    private static String generateThrowsExceptions(final Method method) {
        Class<?>[] exceptions = method.getExceptionTypes();

        return exceptions.length == 0
                ? ""
                : " throws " +
                Arrays.stream(exceptions)
                        .map(Class::getName)
                        .collect(Collectors.joining(", "));
    }

    /**
     * Generates {@link String} parameters of realizing method.
     *
     * @param method to be realized
     * @return {@link String} of method parameters
     */

    private static String generateParameters(final Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter ->
                        parameter.getType().getCanonicalName() + " " + parameter.getName())
                .collect(Collectors.joining(", "));
    }

    /**
     * Generates returned value of realizing method if it is not void.
     *
     * @param method to be realized
     * @return {@link String} of returned or empty
     */

    private static String generateReturn(final Method method) {
        return method.getReturnType() == void.class
                ? ""
                : "return " + generateDefaultReturnedValue(method.getReturnType()) + ";";
    }

    /**
     * Generates {@link String} default returned value of realizing method.
     *
     * @param returnType type token of returned
     * @return {@link String} of default return
     */
    private static String generateDefaultReturnedValue(final Class<?> returnType) {
        return returnType.isPrimitive()
                ? returnType == boolean.class ? "true" : "0"
                : null;
    }

    /**
     * Generates string code with {@link System#lineSeparator()} and substitutes arguments into <var>%s</var> places in text.
     *
     * @param textBlock format
     * @param args      to substitute
     * @return {@link String} of generated code
     */

    private static String format(String textBlock, Object... args) {
        return textBlock.replace("\n", System.lineSeparator()).formatted(args);
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            Path tempRoot = Path.of("TEMP_JA");
            try {
                implement(token, tempRoot);
                compile(tempRoot, token);
                generateJar(token, jarFile, tempRoot);
            } finally {
                clean(tempRoot);
            }
        } catch (IOException e) {
            throw new ImplerException("Can't clean temp file: " + e.getMessage(), e);
        }
    }

    /**
     * Compiles implementation class.
     *
     * @param tempRoot temp directory where class is implemented
     * @param token    interface to be implemented
     * @throws ImplerException if compile is null or errors while compiling
     */
    public static void compile(final Path tempRoot, final Class<?> token) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Can't find java compiler.");
        }

        final String file = getOutFile(tempRoot, token, ".java").toString();
        final String classpath = tempRoot + File.pathSeparator + getClassPath(token);

        final int exitCode = compiler.run(null, null, null,
                "-encoding", "UTF-8",
                file,
                "-cp", classpath);
        if (exitCode != 0) {
            throw new ImplerException("Can't compile temp file.");
        }
    }

    /**
     * Gets {@link String} class path of {@link Class token}.
     *
     * @param token interface to be implemented
     * @return {@link String} class path
     * @throws ImplerException if error while getting string path
     */
    private static String getClassPath(Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new ImplerException("Can't get class path: " + e.getMessage(), e);
        }
    }

    /**
     * Generates jar file with implementation.
     *
     * @param token    interface to be implemented
     * @param jarFile  target <var>.jar</var> file
     * @param tempRoot temp directory where class is implemented
     * @throws ImplerException if error with creating <var>.jar</var> file
     */
    private static void generateJar(Class<?> token, Path jarFile, Path tempRoot) throws ImplerException {
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {
            jarOutputStream.putNextEntry(new ZipEntry(
                    generateClassFullName(token).replace('.', '/') + ".class"));

            final Path outFile = getOutFile(tempRoot, token, ".class");
            Files.copy(outFile, jarOutputStream);
        } catch (IOException | SecurityException e) {
            throw new ImplerException("Can't create jar file: " + e.getMessage(), e);
        }
    }

    /**
     * Simple recursive delete visitor.
     */

    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Clean all temp files after creates <var>.jar</var> file.
     *
     * @param tempRoot temp directory where class is implemented
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static void clean(final Path tempRoot) throws IOException {
        if (Files.exists(tempRoot)) {
            Files.walkFileTree(tempRoot, DELETE_VISITOR);
        }
    }

    /**
     * Example of args to run main.
     */
    private static final String EXPECTED_ARGS = """
            Run with args:
                <class-name> <root-path>
            or
                -jar <class-name> <file.jar>""";


    /**
     * Parse args and do implementation.
     * <p>
     * Run with args:
     * <ul>
     *     <li>&#60class-name&#62 &#60root-path&#62 for {@link Implementor#implement(Class, Path)}
     *     <li>-jar &#60class-name&#62 &#60jar-file&#62 for {@link Implementor#implementJar(Class, Path)}
     * </ul>
     *
     * @param args 2 for <var>.java</var> or 3 for <var>.jar</var> implementation file
     * @see Impler
     * @see JarImpler
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 3) {
            System.err.println("Illegal number of arguments. " + EXPECTED_ARGS);
            return;
        }

        try {
            final Implementor implementor = new Implementor();
            if (args.length == 3 && args[0].equals("-jar")) {
                implementor.implementJar(getClassByName(args[1]), getPathByName(args[2]));
            } else if (args.length == 2) {
                implementor.implement(getClassByName(args[0]), getPathByName(args[1]));
            } else {
                System.err.println("Illegal arguments. " + EXPECTED_ARGS);
            }
        } catch (InvalidPathException e) {
            System.err.println("Invalid path: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Can't find interface: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Can't generate realization class: " + e.getMessage());
        }
    }

    /**
     * Null safety {@link Class} getter by name.
     *
     * @param className of getting class
     * @return {@link Class} for name
     * @throws ClassNotFoundException if class cannot be located
     */
    private static Class<?> getClassByName(String className) throws ClassNotFoundException {
        if (className == null) {
            throw new ClassNotFoundException("Class name is null");
        }
        return Class.forName(className);
    }

    /**
     * Null safety {@link Path} getter by name.
     *
     * @param pathName of getting path
     * @return {@link Path} for name
     * @throws InvalidPathException if the path string cannot be converted to a Path.
     */
    private static Path getPathByName(String pathName) {
        if (pathName == null) {
            throw new InvalidPathException(String.valueOf((Object) null), "Path is null");
        }
        return Path.of(pathName);
    }
}
