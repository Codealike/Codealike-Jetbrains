package com.codealike.client.intellij;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
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
    Config config;

    public ProjectConfig() {
        this.config = new Config();
    }

    @Nullable
    public static ProjectConfig getInstance(Project project) {
        return project.getService(ProjectConfig.class);
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

    static class Config {
        public String projectId;
    }
}
