package com.practice.RepTrans.repositoryManagers;

import com.practice.RepTrans.services.BitBucketService;
import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
@Service
public class BitBucketRepositoryManagerService{

    private Logger logger = LoggerFactory.getLogger(RepositoryManagerService.class);
    @Autowired
    private BitBucketService bitBucketService;

    @Value("${local.base.directory}")
    private String localBaseDirectory;
    @Value("${bitbucket.name}")
    private String bitbucketName;
    @Value("${bitbucket.workspace}")
    private String bitbucketWorkspace;
    @Value("$bitbucket.username")


    public String updateRepositoryOnLocal(String name) throws IOException, GitAPIException {

        List<String> remoteRepos = bitBucketService.getRepositories();

        File localReposDir = new File(localBaseDirectory);
        File[] repositories = localReposDir.listFiles(File::isDirectory);

        String localPath = localBaseDirectory + "/" + name;
        File localRepo = new File(localPath);

        if(!remoteRepos.contains(name)){
            throw new IllegalArgumentException("Repository " + name + " does not exist on BitBucket.");
        }

        if (!localRepo.exists()) {
            // The Repository doesn't exist locally, clone it

            String remoteUrl = "https://x-token-auth:" + bitBucketService.getAccessToken() + "@bitbucket.org/" + bitbucketWorkspace + "/" + name.toLowerCase() + ".git";
            bitBucketService.cloneRepository(remoteUrl, localPath);
            return "Repository " + name + " cloned successfully.";
        }
        else {
            bitBucketService.updateRepositoryFromVCS(localRepo);
            return "Repository " + name + " updated successfully.";
        }
    }

    public String updateAllRepositoriesOnLocal() throws IOException, GitAPIException {

        List<String> remoteRepos = bitBucketService.getRepositories();

        for (String repo : remoteRepos) {
            String localPath = localBaseDirectory + "/" + repo;
            File localRepo = new File(localPath);
            try {
                if (!bitBucketService.isRepositoryInitialized(localPath)) {
                    String remoteUrl = "https://x-token-auth:" + bitBucketService.getAccessToken() + "@bitbucket.org/" +
                            bitbucketWorkspace + "/" + repo.toLowerCase() + ".git";
                    bitBucketService.cloneRepository(remoteUrl, localPath);
                } else {
                    try (Git git = Git.open(new File(localPath))) {
                        git.pull().call();
                    }
                }
            } catch (IOException | GitAPIException e) {
                logger.error("Error updating repository on local: " + repo, e);
            }
        }
        return "Updated";
    }
//
//    public void updateRepositoryOnCloud(String localBasePath) throws IOException, GitAPIException {
//        List<String> repositories = bitBucketService.getRepositories();
//        String localPath = localBasePath + "/" + repo;
//        try {
//            if (!bitBucketService.isRepositoryInitialized(localPath)) {
//                bitBucketService.cloneRepository(repo, localPath);
//            }
//            bitBucketService.addChanges(localPath);
//            bitBucketService.commitChanges(localPath, "Update repository on cloud");
//            bitBucketService.pushChanges(localPath, repo);
//        } catch (IOException | GitAPIException e) {
//            logger.error("Error updating repository on cloud: " + repo, e);
//        }
//    }
//
//
//    public void updateAllRepositoriesOnCloud(String localBasePath) throws IOException, GitAPIException {
//        List<String> repositories = bitBucketService.getRepositories();
//        for (String repo : repositories) {
//            String localPath = localBasePath + "/" + repo;
//            try {
//                if (!bitBucketService.isRepositoryInitialized(localPath)) {
//                    bitBucketService.cloneRepository(repo, localPath);
//                }
//                bitBucketService.addChanges(localPath);
//                bitBucketService.commitChanges(localPath, "Update all repositories on cloud");
//                bitBucketService.pushChanges(localPath, repo);
//            } catch (IOException | GitAPIException e) {
//                logger.error("Error updating repository on cloud: " + repo, e);
//            }
//        }
//    }
}
