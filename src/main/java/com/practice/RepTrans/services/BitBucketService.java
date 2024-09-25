package com.practice.RepTrans.services;

import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

@Service
@Data
public class BitBucketService{

    private static final Logger logger = LoggerFactory.getLogger(BitBucketService.class);
    private static final String API_BASE_URL = "https://api.bitbucket.org/2.0/";
    @Value("${bitbucket.oauth.key}")
    private String OAUTH_KEY;
    @Value("${bitbucket.oauth.secret}")
    private String OAUTH_SECRET;
    private String username;
    @Value("$bitbucket.password")
    private String password;


    public List<String> getRepositories() throws IOException {
        List<String> repositories = new ArrayList<>();
        URL url = new URL(API_BASE_URL + "repositories?role=admin");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + getAccessToken());
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String responseBody = readResponse(connection);
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray jsonRepositories = jsonResponse.getJSONArray("values");

            for (int i = 0; i < jsonRepositories.length(); i++) {
                JSONObject repo = jsonRepositories.getJSONObject(i);
                repositories.add(repo.getString("name"));
            }
        } else {
            logger.error("Error getting repositories: HTTP {}", responseCode);
        }
        return repositories;
    }


    public void cloneRepository(String remoteUrl, String localPath) throws IOException, GitAPIException {
        try {
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(new File(localPath))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(getAccessToken(), ""))
                    .setCloneAllBranches(true)
                    .call();
            logger.info("Repository cloned to " + localPath);
        } catch (GitAPIException e) {
            logger.error("Error cloning repository", e);
            throw e;
        }
    }

    public void updateRepositoryFromVCS(File localRepo) throws IOException {
        try (Git git = Git.open(localRepo)) {
            Repository repository = git.getRepository();

            // Check if the repository has a HEAD reference
            if (repository.resolve("HEAD") == null) {
                System.out.println("Repository has no commits (unborn branch). Aborting update.");
                return;
            }

            // List all branches (both local and remote)
            List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

            for (Ref branch : branches) {
                String branchName = branch.getName();

                // Checkout the branch
                git.checkout().setName(branchName).call();

                // Pull updates for the current branch

                PullResult pullResult = git.pull()
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                                username, password))
                        .call();
                // Check the result
                if (pullResult.isSuccessful()) {
                    System.out.println("Successfully updated local repository.");
                    System.out.println("Fetch result: " + pullResult.getFetchResult().getMessages());
                    System.out.println("Merge result: " + pullResult.getMergeResult().getMergeStatus());
                } else {
                    System.out.println("Failed to update local repository.");
                    System.out.println("Fetch result: " + pullResult.getFetchResult().getMessages());
                    System.out.println("Merge result: " + pullResult.getMergeResult().getMergeStatus());
                }
            }
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRepositoryInitialized(String localPath) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            Repository repository = builder.setGitDir(new File(localPath + "/.git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            return repository.getObjectDatabase().exists();
        } catch (IOException e) {
            logger.warn("Repository not initialized at " + localPath, e);
            return false;
        }
    }
//    @Override
//    public void addChanges(String localPath) throws GitAPIException, IOException {
//        try (Git git = Git.open(new File(localPath))) {
//            git.add().addFilepattern(".").call();
//            logger.info("Changes added to repository at " + localPath);
//        } catch (IOException | GitAPIException e) {
//            logger.error("Error adding changes", e);
//            throw e;
//        }
//    }
//
//    @Override
//    public void commitChanges(String localPath, String message) throws GitAPIException, IOException {
//        try (Git git = Git.open(new File(localPath))) {
//            git.commit().setMessage(message).call();
//            logger.info("Changes committed with message: " + message);
//        } catch (IOException | GitAPIException e) {
//            logger.error("Error committing changes", e);
//            throw e;
//        }
//    }
//
//    @Override
//    public void pushChanges(String localPath, String remoteUrl) throws GitAPIException, IOException {
//        try (Git git = Git.open(new File(localPath))) {
//            UsernamePasswordCredentialsProvider credentialsProvider =
//                    new UsernamePasswordCredentialsProvider(OAUTH_KEY, OAUTH_SECRET);
//            git.push()
//                    .setRemote("origin")
//                    .setCredentialsProvider(credentialsProvider)
//                    .call();
//            logger.info("Changes pushed to " + remoteUrl);
//        } catch (IOException | GitAPIException e) {
//            logger.error("Error pushing changes", e);
//            throw e;
//        }
//    }
//
//    @Override
//    public void initRepository(String localPath) throws IOException {
//        File directory = new File(localPath);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//        try (Git git = Git.init().setDirectory(directory).call()) {
//            logger.info("Created a new repository at {}", git.getRepository().getDirectory());
//        } catch (GitAPIException e) {
//            logger.error("Error creating a new repository", e);
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    @Override
//    public void createRepositoryOnVCS(File repoDir) throws IOException {
//
//    }



    //---------------------Supp methds
    private String readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder content = new StringBuilder();
        try (Scanner scanner = new Scanner(conn.getInputStream())) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
            }
        }
        return content.toString();
    }

    public String getAccessToken() throws IOException {

        String url = "https://bitbucket.org/site/oauth2/access_token";
        String auth = OAUTH_KEY + ":" + OAUTH_SECRET;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", authHeaderValue);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String body = "grant_type=client_credentials";

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getString("access_token");
    }
}



