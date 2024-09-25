package com.practice.RepTrans.services;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



@Service
public class GitHubService{

    private static final String API_BASE_URL = "https://api.github.com/";
    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    @Value("${github.username}")
    private String githubUsername;

    @Value("${github.token}")
    private String githubToken;

    public List<String> getRepositories() {
        List<String> repositories = new ArrayList<>();
        try {
            URL url = new URL(API_BASE_URL + "user/repos?type=owner");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + githubToken);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String responseBody = readResponse(connection);
                JSONArray jsonRepositories = new JSONArray(responseBody);

                for (int i = 0; i < jsonRepositories.length(); i++) {
                    JSONObject repo = jsonRepositories.getJSONObject(i);
                    repositories.add(repo.getString("name"));
                }
            } else {
                logger.error("Error getting repositories: HTTP {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("Exception while fetching repositories", e);
        }
        return repositories;
    }

    public void cloneRepository(String remoteUrl, String localPath) throws GitAPIException {
        try {
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(new File(localPath))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                    .setCloneAllBranches(true)
                    .call();
            logger.info("Repository cloned to " + localPath);
        } catch (GitAPIException e) {
            logger.error("Error cloning repository", e);
            throw e;
        }
    }

    public void createRepositoryOnVCS(File repoDir) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
        String repoName = repoDir.getName();
        System.out.println("Processing repository: " + repoName);

        GHRepository ghRepo;
        try {
            ghRepo = github.getRepository(githubUsername + "/" + repoName);
            System.out.println("Repository exists on GitHub: " + repoName);
        } catch (IOException e) {
            System.out.println("Repository doesn't exist on GitHub. Creating: " + repoName);
            ghRepo = github.createRepository(repoName)
                    .private_(true)
                    .create();
        }

        String remoteUrl = ghRepo.getHttpTransportUrl();

        try (Git git = Git.open(repoDir)) {
            // Add remote if it doesn't exist
            try {
                git.remoteAdd()
                        .setName("origin")
                        .setUri(new URIish(remoteUrl))
                        .call();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            // Push all branches to remote
            git.push().setPushAll().setRemote("origin")
            .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
            .call();
            System.out.println("Successfully pushed repository: " + repoName);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

//    public void commitChanges(String localPath, String message) {
//        try {
//            Git git = Git.open(new File(localPath));
//            git.commit().setMessage(message).call();
//            git.close();
//        } catch (IOException | GitAPIException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void addChanges(String localPath) {
//        try {
//            Git git = Git.open(new File(localPath));
//            git.add().addFilepattern(".").call();
//            git.close();
//        } catch (IOException | GitAPIException e) {
//            e.printStackTrace();
//        }
//    }
//
////    public void pushChanges(String localPath) {
////        try {
////            Git git = Git.open(new File(localPath));
////            UsernamePasswordCredentialsProvider credentialsProvider =
////                    new UsernamePasswordCredentialsProvider(githubUsername, githubToken);
////            git.push()
////                    .setCredentialsProvider(credentialsProvider)
////                    .setRemote(remoteUrl)
////                    .call();
////            git.close();
////        } catch (IOException | GitAPIException e) {
////            e.printStackTrace();
////        }
////    }
//
//    public void initRepository(String localPath) throws IOException {
//        File localDir = new File(localPath);
//        if (!localDir.exists()) {
//            localDir.mkdirs();
//        }
//        Repository repository = new FileRepository(new File(localPath, ".git"));
//        repository.create();
//    }
//
//    public boolean isRepositoryInitialized(String localPath) {
//        File gitDir = new File(localPath, ".git");
//        return gitDir.exists();
//    }

    public void updateRepositoryFromVCS(File localRepo) throws IOException {

    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
    }
}

