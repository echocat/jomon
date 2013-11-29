package org.echocat.jomon.maven.plugins;

import com.google.common.base.Function;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.slf4j.impl.StaticLoggerBinder;

import javax.annotation.Nonnull;

public abstract class MojoSupport extends AbstractMojo {

    @Override
    public void setLog(Log log) {
        super.setLog(log);
        StaticLoggerBinder.getSingleton().setLog(log);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            call();
        } catch (MojoFailureException | MojoExecutionException e) {
            throw e;
        } catch (final Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public abstract void call() throws Exception;

    @Nonnull
    protected <T> T get(@Nonnull T value, @Nonnull String propertyName) throws MojoExecutionException {
        if (value == null) {
            throw new MojoExecutionException("No " + propertyName + " set.");
        }
        return value;
    }

    @Nonnull
    protected <T, F> T get(@Nonnull F value, @Nonnull String propertyName, @Nonnull Function<F, T> function) throws MojoExecutionException {
        final F original = get(value, propertyName);
        return function.apply(original);
    }

}
