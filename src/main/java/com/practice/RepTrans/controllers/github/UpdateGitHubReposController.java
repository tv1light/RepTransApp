package com.practice.RepTrans.controllers.github;

import com.practice.RepTrans.repositoryManagers.GitHubRepositoryManagerService;
import com.practice.RepTrans.resources.ApiPaths;
import lombok.Data;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(ApiPaths.UPDATE_GITHUB)
@Data
public class UpdateGitHubReposController{

    GitHubRepositoryManagerService gitHubRepositoryManagerService;

    @GetMapping("/local/{name}")
    public String updateLocalRepo(@PathVariable String name) throws IOException {
        return "Updated";
    }

    @GetMapping("/local-all")
    public String updateAllLocalRepos() throws IOException {
        return "Updated all repositories";
    }

    @GetMapping("/remote/{name}")
    public String updateRemoteRepo(@PathVariable String name) throws IOException, GitAPIException {
        return gitHubRepositoryManagerService.updateRepositoryOnCloud(name);
    }

    @GetMapping("/remote-all")
    public String updateAllRemoteRepos() throws IOException, GitAPIException {
        return gitHubRepositoryManagerService.updateAllRepositoriesOnCloud();
    }
}
