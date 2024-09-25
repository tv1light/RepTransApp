package com.practice.RepTrans.repositoryManagers;

import com.practice.RepTrans.services.GitHubService;
import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Service
public class GitHubRepositoryManagerService{

    private static final Logger logger = LoggerFactory.getLogger(GitHubRepositoryManagerService.class);

    @Autowired
    private GitHubService gitHubService;
    @Value("${github.username}")
    private String githubUsername;
    @Value("${local.base.directory}")
    private String localBaseDirectory;

    public String updateRepositoryOnCloud(String localRepoName) throws IOException, GitAPIException {

        String localPath = localBaseDirectory + "/" + localRepoName;
        File localRepo = new File(localPath);
        GHRepository remoteRepo;
        if (localRepo.exists()) {
            gitHubService.createRepositoryOnVCS(localRepo);
        }
        else {
            throw new IllegalArgumentException("Repository " + localRepoName + " does not exist on local directory.");
        }
        return "Ok";
    }

    public String updateAllRepositoriesOnCloud() throws IOException, GitAPIException {
        File localReposDir = new File(localBaseDirectory);
        File[] repositories = localReposDir.listFiles(File::isDirectory);

        if (repositories == null) {
            throw new IOException("No repositories found in the specified path");
        }

        for (File repoDir : repositories) {
            String localPath = localBaseDirectory + repoDir.getName();
               gitHubService.createRepositoryOnVCS(repoDir);
        }
        return "Updated";
    }

//    public String updateAllRepositoriesOnLocal(String localBasePath) throws IOException, GitAPIException {
//        List<String> repositories = gitHubService.getRepositories();
//        for (String repo : repositories) {
//            String localPath = localBasePath + "/" + repo;
//            try {
//                if (!gitHubService.isRepositoryInitialized(localPath)) {
//                    gitHubService.cloneRepository(getRemoteUrl(repo), localPath);
//                } else {
//                    try (Git git = Git.open(new File(localPath))) {
//                        git.pull().call();
//                    }
//                }
//            } catch (IOException | GitAPIException e) {
//                logger.error("Error updating repository on local: " + repo, e);
//            }
//        }
//        return "Ok";
//    }
//
//    private String getRemoteUrl(String repo) {
//        return "https://github.com/" + githubUsername + "/" + repo + ".git";
//    }

//    public String updateRepositoryOnLocal(String localBasePath, int repoIndex) throws IOException, GitAPIException {
//        List<String> repositories = gitHubService.getRepositories();
//        if (repoIndex < 0 || repoIndex >= repositories.size()) {
//            throw new IllegalArgumentException("Invalid repository index");
//        }
//        String repo = repositories.get(repoIndex);
//        String localPath = localBasePath + "/" + repo;
//        try {
//            if (!gitHubService.isRepositoryInitialized(localPath)) {
//                gitHubService.cloneRepository(getRemoteUrl(repo), localPath);
//            } else {
//                try (Git git = Git.open(new File(localPath))) {
//                    git.pull().call();
//                }
//            }
//        } catch (IOException | GitAPIException e) {
//            logger.error("Error updating repository on local: " + repo, e);
//        }
//        return "Ok";
//    }
}

