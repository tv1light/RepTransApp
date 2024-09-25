package com.practice.RepTrans.controllers.github;

import com.practice.RepTrans.resources.ApiPaths;
import com.practice.RepTrans.services.GitHubService;
import com.practice.RepTrans.services.VCService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.GITHUB)
@Data
public class GitHubRepoController{
    private final GitHubService gitHubService;

    @GetMapping("/repos")
    public List<String> getGitHubRepositories() throws IOException {
        return gitHubService.getRepositories();
    }

}
