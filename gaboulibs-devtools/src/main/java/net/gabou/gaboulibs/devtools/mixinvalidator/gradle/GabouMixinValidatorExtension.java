package net.gabou.gaboulibs.devtools.mixinvalidator.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class GabouMixinValidatorExtension {
    private final Project project;

    @Inject
    public GabouMixinValidatorExtension(Project project) {
        this.project = project;
        getConfigFile().convention(project.getLayout().getProjectDirectory().file("mixin-validator.json"));
        getFailOnError().convention(true);
        getFailOnWarning().convention(false);
    }

    public abstract RegularFileProperty getConfigFile();

    public abstract Property<Boolean> getFailOnError();

    public abstract Property<Boolean> getFailOnWarning();

    public void setConfigFile(Object configFile) {
        getConfigFile().set(project.getLayout().file(project.provider(() -> project.file(configFile))));
    }

    public void setFailOnError(boolean failOnError) {
        getFailOnError().set(failOnError);
    }

    public void setFailOnWarning(boolean failOnWarning) {
        getFailOnWarning().set(failOnWarning);
    }
}
