package com.codealike.client.intellij;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Created by daniel on 4/21/17.
 */
@State(
        name = "Codealike",
        storages = {
                @Storage("codealike.xml")
        }
)
public class ProjectConfig implements PersistentStateComponent<ProjectConfig.Config> {
    static class Config {
        public String projectId;
    }

    Config config;

    public ProjectConfig() {
        this.config = new Config();
    }

    public UUID getProjectId() {
        return (config.projectId == null) ? null : UUID.fromString(config.projectId);
    }

    public void setProjectId(UUID projectId) {
        this.config.projectId = projectId.toString();
    }

    @Nullable
    @Override
    public Config getState() {
        return config;
    }

    @Override
    public void loadState(Config state) {
        config = state;
    }

    @Nullable
    public static ProjectConfig getInstance(Project project) {
        return ServiceManager.getService(project, ProjectConfig.class);
    }
}
