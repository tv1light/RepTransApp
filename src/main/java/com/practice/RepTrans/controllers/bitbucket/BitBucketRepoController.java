package com.practice.RepTrans.controllers.bitbucket;

import com.practice.RepTrans.resources.ApiPaths;
import com.practice.RepTrans.services.BitBucketService;
import com.practice.RepTrans.services.VCService;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.BITBUCKET)
@Data
public class BitBucketRepoController{


    private final BitBucketService bitBucketService;

    @GetMapping("/repos")
    public List<String> getBitBucketRepositories() throws IOException {
        return bitBucketService.getRepositories();
    }
}
