package org.echocat.jomon.process;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.io.File.pathSeparatorChar;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.split;
import static org.echocat.jomon.process.ExecutableDiscovery.FileSystem.currentFileSystem;
import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class ExecutableDiscovery {

    @Nonnull
    private static final ExecutableDiscovery INSTANCE = new ExecutableDiscovery();

    @Nonnull
    public static ExecutableDiscovery getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static ExecutableDiscovery executableDiscovery() {
        return getInstance();
    }

    @Nullable
    public File discover(@Nonnull Task task) {
        final List<File> searhInPaths = getSearhInPathsFor(task);
        return discoverIn(task, searhInPaths);
    }

    @Nonnull
    protected List<File> getSearhInPathsFor(@Nonnull Task task) {
        final List<File> result = new ArrayList<>();
        for (String environmentVariable : task.getEnvironmentVariables()) {
            result.addAll(getSearhInPathsFor(task, environmentVariable));
        }
        result.addAll(getBaseSearhInPathsFor("PATH"));
        return result;
    }

    @Nonnull
    protected List<File> getSearhInPathsFor(@Nonnull Task task, @Nonnull String environmentVariable) {
        final List<File> bases = getBaseSearhInPathsFor(environmentVariable);
        final List<File> result = new ArrayList<>(bases.size());
        for (File base : bases) {
            final List<String> subDirectories = task.getSubDirectories();
            if (subDirectories.isEmpty()) {
                result.add(base);
            } else {
                for (String subDirectory : subDirectories) {
                    result.add(new File(base, subDirectory));
                }
            }
        }
        return result;
    }

    @Nonnull
    protected List<File> getBaseSearhInPathsFor(@Nonnull String environmentVariable) {
        final String plainPaths = getenv(environmentVariable);
        final String[] paths = plainPaths != null ? split(plainPaths, pathSeparatorChar) : null;
        final List<File> result = new ArrayList<>(paths != null ? paths.length : 0);
        if (paths != null) {
            for (String path : paths) {
                result.add(new File(path));
            }
        }
        return result;
    }

    @Nullable
    protected File discoverIn(@Nonnull Task task, @Nonnull List<File> searhInPaths) {
        File result = null;
        for (File searhInPath : searhInPaths) {
            result = discoverIn(task, searhInPath);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Nullable
    protected File discoverIn(@Nonnull Task task, @Nonnull File searhInPath) {
        File result = null;
        final List<String> executableNames = task.getExecutableNames();
        for (String executableName : executableNames) {
            result = discoverIn(task, searhInPath, executableName);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Nullable
    protected File discoverIn(@Nonnull Task task, @Nonnull File searhInPath, @Nonnull String executableName) {
        File result = null;
        for (String extension : currentFileSystem().getExecutableExtensions()) {
            result = discoverIn(task, searhInPath, executableName, extension);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Nullable
    protected File discoverIn(@SuppressWarnings("UnusedParameters") @Nonnull Task task, @Nonnull File searhInPath, @Nonnull String executableName, @Nonnull String extension) {
        final File potentialFile = new File(searhInPath, executableName + extension);
        return potentialFile.canExecute() ? potentialFile : null;
    }

    public static class Task {

        @Nonnull
        public static Task executableDiscoveryTask(@Nonnull String... executableNames) {
            return new Task().withExecutableNames(executableNames);
        }

        @Nonnull
        public static Task executable(@Nonnull String... executableNames) {
            return executableDiscoveryTask(executableNames);
        }

        @Nonnull
        private final List<String> _executableNames = new ArrayList<>(1);
        @Nonnull
        private final List<String> _environmentVariables = new ArrayList<>();
        @Nonnull
        private final List<String> _subDirectories = new ArrayList<>();

        private boolean _autoDetectExtensions = true;

        @Nonnull
        public Task withExecutableNames(@Nonnull String... names) {
            // noinspection ConstantConditions
            if (names == null || names.length <= 0 || names[0] == null) {
                throw new IllegalArgumentException("There is a minimum of 1 name required.");
            }
            addAll(_executableNames, names);
            return this;
        }

        @Nonnull
        public Task withExecutableName(@Nonnull String name) {
            return withExecutableNames(name);
        }

        @Nonnull
        public Task withinEnvironmentVariables(@Nullable String... names) {
            addAll(_environmentVariables, names);
            return this;
        }

        @Nonnull
        public Task withinEnvironmentVariable(@Nullable String name) {
            if (name != null) {
                _environmentVariables.add(name);
            }
            return this;
        }

        @Nonnull
        public Task searchInSubDirectories(@Nullable String... paths) {
            addAll(_subDirectories, paths);
            return this;
        }

        @Nonnull
        public Task searchInSubDirectory(@Nullable String path) {
            if (path != null) {
                _subDirectories.add(path);
            }
            return this;
        }

        @Nonnull
        public Task autoDetectExtensions(boolean autoDetectExtensions) {
            _autoDetectExtensions = autoDetectExtensions;
            return this;
        }

        @Nonnull
        public Task autoDetectExtensions() {
            return autoDetectExtensions(true);
        }

        @Nonnull
        public Task doNotAutoDetectExtensions() {
            return autoDetectExtensions(false);
        }

        @Nonnull
        public List<String> getExecutableNames() {
            return _executableNames;
        }

        @Nonnull
        public List<String> getEnvironmentVariables() {
            return _environmentVariables;
        }

        @Nonnull
        public List<String> getSubDirectories() {
            return _subDirectories;
        }

        public boolean isAutoDetectExtensions() {
            return _autoDetectExtensions;
        }

    }

    public static enum FileSystem {
        windowsBased(false, ".exe", ".cmd", ".bat", ".pif"),
        linuxBased(true, "", ".sh"),
        macosxBased(true, "", ".sh");

        private static final FileSystem CURRENT_FILE_SYSTEM = discoverCurrentFileSystem();

        @Nonnull
        private static FileSystem discoverCurrentFileSystem() {
            final String osName = getProperty("os.name", "unknown").toLowerCase();
            final FileSystem result;
            if (osName.contains("windows")) {
                result = windowsBased;
            } else if (osName.contains("linux")) {
                result = linuxBased;
            } else if (osName.contains("mac") || osName.contains("darwin")) {
                result = macosxBased;
            } else {
                throw new IllegalStateException("This JVM does not run on an supported operating system, for operating with chromedriver.");
            }
            return result;
        }

        @Nonnull
        private final List<String> _executableExtensions;
        private final boolean _caseSensitive;

        FileSystem(boolean caseSensitive, @Nonnull String... executableExtensions) {
            _caseSensitive = caseSensitive;
            final List<String> targetExtensions = new ArrayList<>(executableExtensions.length);
            for (String extension : executableExtensions) {
                targetExtensions.add(caseSensitive ? extension : extension.toUpperCase());
            }
            _executableExtensions = asImmutableList(targetExtensions);
        }

        @Nonnull
        public List<String> getExecutableExtensions() {
            return _executableExtensions;
        }

        public boolean hasExecutableExtension(@Nonnull String extension) {
            return _executableExtensions.contains(_caseSensitive ? extension : extension.toUpperCase());
        }

        @Nonnull
        public static FileSystem currentFileSystem() {
            return CURRENT_FILE_SYSTEM;
        }

    }

}
