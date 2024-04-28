package com.example.jetbrainsvcstask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;


@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

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

    @PostMapping("/repos")
    public String submit(@RequestParam String orgUrl, @RequestParam String accessToken, @RequestParam(name = "ignoreCase", defaultValue = "false") boolean ignoreCase, Model model) throws IOException, URISyntaxException, NotFoundException {
        String searchString = env.getProperty("search.string", "Hello");
        GitHubOrgService gitHubOrgService = new GitHubOrgService(orgUrl, accessToken);

        List<String> reposName = gitHubOrgService.setReposName();
        List<Boolean> reposContainsHello = gitHubOrgService.setReposContainsHello(reposName, searchString, ignoreCase);
        if (reposName.size() != reposContainsHello.size()) {
            logger.error("Number of repository names and repository contents do not match");
            throw new RuntimeException("Number of repositories, repository names, and repository contents do not match");
        }

        model.addAttribute("orgUrl", gitHubOrgService.getOrgUrl());
        model.addAttribute("orgName", gitHubOrgService.getOrgName());
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("ignoreCase", ignoreCase);

        model.addAttribute("reposName", reposName);
        model.addAttribute("reposContainsHello", reposContainsHello);
        return "form";
    }

    @PostMapping("/webhooks")
    public String listWebhooks(@RequestParam String orgUrl, @RequestParam String accessToken, Model model) throws IOException, URISyntaxException, NotFoundException {
        GitHubOrgService gitHubOrgService = new GitHubOrgService(orgUrl, accessToken);
        List<GitHubWebhook> webhooks = gitHubOrgService.getWebhooks();
        model.addAttribute("orgUrl", gitHubOrgService.getOrgUrl());
        model.addAttribute("orgName", gitHubOrgService.getOrgName());
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("webhooks", webhooks);
        return "form";
    }

    @PostMapping("/create-webhook")
    public String createWebhook(@RequestParam String orgUrl, @RequestParam String accessToken, @RequestParam String webhookUrl, @RequestParam String webhookSecret, @RequestParam String events, @RequestParam(name = "active", defaultValue = "false") boolean active, Model model) throws IOException, URISyntaxException, NotFoundException {
        GitHubOrgService gitHubOrgService = new GitHubOrgService(orgUrl, accessToken);
        GitHubWebhook webhook = new GitHubWebhook();
        webhook.setConfig(new GitHubWebhook.Config());
        webhook.getConfig().setConfigUrl(webhookUrl);
        webhook.getConfig().setContentType("json");
        webhook.setEvents(List.of(events.split(",")));
        webhook.setActive(active);
        webhook.getConfig().setSecret(webhookSecret);
        webhook.setName("web");
        gitHubOrgService.createWebhook(webhook);
        model.addAttribute("orgUrl", gitHubOrgService.getOrgUrl());
        model.addAttribute("orgName", gitHubOrgService.getOrgName());
        model.addAttribute("accessToken", accessToken);
        return "form";
    }
}