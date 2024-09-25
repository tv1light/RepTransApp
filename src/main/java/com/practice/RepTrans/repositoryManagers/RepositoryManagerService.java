package com.practice.RepTrans.repositoryManagers;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public interface RepositoryManagerService{
    public String updateRepositoryOnLocal(String remoteRepositoryName) throws IOException, GitAPIException;
    public String updateRepositoryOnCloud(String localRepositoryName) throws IOException, GitAPIException;
    public String updateAllRepositoriesOnCloud(String localBasePath) throws IOException, GitAPIException;
    public String updateAllRepositoriesOnLocal(String localBasePath) throws IOException, GitAPIException;

}
