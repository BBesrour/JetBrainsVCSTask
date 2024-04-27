package com.example.jetbrainsvcstask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URISyntaxException;

@Controller
public class WebController {

    @Autowired
    private Environment env;

    @RequestMapping("/")
    public RedirectView redirectRootToForm() {
        return new RedirectView("/form");
    }

    @GetMapping("/form")
    public String form() {
        return "form";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam String orgUrl, @RequestParam String accessToken, @RequestParam(name = "ignoreCase", defaultValue = "false") boolean ignoreCase, Model model) throws IOException, URISyntaxException, NotFoundException {
        String searchString = env.getProperty("search.string", "Hello");
        GitHubService gitHubService = new GitHubService(orgUrl, accessToken, ignoreCase, searchString);

        model.addAttribute("orgUrl", gitHubService.getOrgUrl());
        model.addAttribute("orgName", gitHubService.getOrgName());
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("ignoreCase", ignoreCase);

        model.addAttribute("repoNames", gitHubService.getReposName());
        model.addAttribute("reposContainsHello", gitHubService.getReposContainsHello());
        return "form";
    }
}