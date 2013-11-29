package org.echocat.jomon.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Properties;

@SuppressWarnings("InstanceVariableNamingConvention")
public abstract class ProjectEnabledMojoSupport extends MojoSupport {

    @Component(role = MavenProject.class, hint = "project")
    private MavenProject project;

    @Nonnull
    protected MavenProject getProject() throws MojoExecutionException {
        return get(project, "project");
    }

    @Nonnull
    protected Properties getProjectProperties() throws MojoExecutionException {
        return getProject().getProperties();
    }

    @Nullable
    protected String findProjectProperty(@Nonnull String key, @Nullable String defaultValue) throws MojoExecutionException {
        final String result = getProjectProperties().getProperty(key);
        return result != null ? result : defaultValue;
    }

    @Nullable
    protected String findProjectProperty(@Nonnull String key) throws MojoExecutionException {
        return findProjectProperty(key, null);
    }

    @Nonnull
    protected String getProjectProperty(@Nonnull String key) throws MojoExecutionException {
        final String result = findProjectProperty(key);
        if (result == null) {
            throw new MojoExecutionException("The property '" + key + "' of project does not exist.");
        }
        return result;
    }

    protected void setProjectProperty(@Nonnull String key, @Nullable String value) throws MojoExecutionException {
        getProjectProperties().setProperty(key, value);
    }

    protected void removeProjectProperty(@Nonnull String key) throws MojoExecutionException {
        getProjectProperties().remove(key);
    }

}
