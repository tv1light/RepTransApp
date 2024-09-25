package com.practice.RepTrans.services;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface VCService {
    List<String> getRepositories() throws IOException;
    void cloneRepository(String remoteUrl, String localPath) throws IOException, GitAPIException;
    void addChanges(String localPath) throws GitAPIException, IOException;
    void commitChanges(String localPath, String message) throws GitAPIException, IOException;
    void pushChanges(String localPath, String remoteUrl) throws GitAPIException, IOException;
    void initRepository(String localPath) throws IOException;
    boolean isRepositoryInitialized(String localPath);
    void createRepositoryOnVCS(File repoDir) throws IOException;
    void updateRepositoryFromVCS(File localRepo) throws IOException;
}
