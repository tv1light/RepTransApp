package com.practice.RepTrans.controllers.bitbucket;

import com.practice.RepTrans.repositoryManagers.BitBucketRepositoryManagerService;
import com.practice.RepTrans.repositoryManagers.RepositoryManagerService;
import com.practice.RepTrans.resources.ApiPaths;
import lombok.Data;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(ApiPaths.UPDATE_BITBUCKET)
@Data
public class UpdateBitBucketReposController{

    @Autowired
    private BitBucketRepositoryManagerService bitBucketRepositoryManagerService;

    @GetMapping("/local/{name}")
    public String updateLocalRepo(@PathVariable String name) throws IOException, GitAPIException {
        return bitBucketRepositoryManagerService.updateRepositoryOnLocal(name);
    }

    @GetMapping("/local-all")
    public String updateAllLocalRepos() throws IOException, GitAPIException {
        return bitBucketRepositoryManagerService.updateAllRepositoriesOnLocal();
    }

    @GetMapping("/remote/{name}")
    public String updateRemoteRepo(@PathVariable String name) throws IOException {
        return "Updated all repositories";
    }

    @GetMapping("/remote-all")
    public String updateAllRemoteRepos() throws IOException {
        return "Updated all repositories";
    }
}
