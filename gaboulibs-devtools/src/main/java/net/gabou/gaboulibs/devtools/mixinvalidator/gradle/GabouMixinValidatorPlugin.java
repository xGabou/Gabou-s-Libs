package net.gabou.gaboulibs.devtools.mixinvalidator.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

public final class GabouMixinValidatorPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        GabouMixinValidatorExtension extension = project.getExtensions().create(
                "gabouMixinValidator",
                GabouMixinValidatorExtension.class,
                project
        );

        TaskProvider<ValidateMixinsTask> validateMixins = project.getTasks().register("validateMixins", ValidateMixinsTask.class, task -> {
            task.setGroup("verification");
            task.setDescription("Validates SpongePowered Mixin configs and target classes.");
            task.getProjectDirectory().set(project.getLayout().getProjectDirectory());
            task.getConfigFile().convention(extension.getConfigFile());
            task.getFailOnError().convention(extension.getFailOnError());
            task.getFailOnWarning().convention(extension.getFailOnWarning());
        });

        project.getPlugins().withType(JavaPlugin.class, ignored -> configureJavaProject(project, validateMixins));
    }

    private static void configureJavaProject(Project project, TaskProvider<ValidateMixinsTask> validateMixins) {
        JavaPluginExtension java = project.getExtensions().findByType(JavaPluginExtension.class);
        if (java == null) {
            return;
        }

        SourceSetContainer sourceSets = java.getSourceSets();
        SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        validateMixins.configure(task -> {
            task.dependsOn(project.getTasks().named(JavaPlugin.CLASSES_TASK_NAME));
            task.getValidationClasspath().from(main.getOutput());
            task.getValidationClasspath().from(project.getConfigurations().named(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME));
            task.getValidationClasspath().from(project.getConfigurations().named(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));
            task.getResourceDirs().from(main.getResources().getSrcDirs());
            task.getSourceDirs().from(main.getAllJava().getSrcDirs());
        });
    }
}
