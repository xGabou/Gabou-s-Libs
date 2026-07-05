package net.gabou.gaboulibs.devtools.mixinvalidator.gradle;

import net.gabou.gaboulibs.devtools.mixinvalidator.MixinValidatorMain;
import net.gabou.gaboulibs.devtools.mixinvalidator.ValidationReport;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.util.stream.Collectors;

public abstract class ValidateMixinsTask extends DefaultTask {
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getConfigFile();

    @Input
    public abstract Property<Boolean> getFailOnError();

    @Input
    public abstract Property<Boolean> getFailOnWarning();

    @Internal
    public abstract DirectoryProperty getProjectDirectory();

    @Classpath
    public abstract ConfigurableFileCollection getValidationClasspath();

    @InputFiles
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getResourceDirs();

    @InputFiles
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSourceDirs();

    @TaskAction
    public void validate() {
        MixinValidatorMain.ValidationOptions options = new MixinValidatorMain.ValidationOptions();
        options.setProjectDir(getProjectDirectory().get().getAsFile().toPath());
        options.setConfigFile(getConfigFile().get().getAsFile().toPath());
        options.setClasspath(getValidationClasspath().getFiles().stream().map(file -> file.toPath()).collect(Collectors.toList()));
        options.setResourceDirs(getResourceDirs().getFiles().stream().map(file -> file.toPath()).collect(Collectors.toList()));
        options.setSourceDirs(getSourceDirs().getFiles().stream().map(file -> file.toPath()).collect(Collectors.toList()));
        options.setFailOnError(getFailOnError().get());
        options.setFailOnWarning(getFailOnWarning().get());

        ValidationReport report = MixinValidatorMain.validate(options);
        report.print(System.out);

        if (MixinValidatorMain.shouldFail(report, options.isFailOnError(), options.isFailOnWarning())) {
            throw new GradleException("Mixin validation failed.");
        }
    }
}
