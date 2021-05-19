package com.jitterted.moborg.adapter.in.web;

import com.jitterted.moborg.domain.HuddleId;

public class RegistrationForm {
    private String name;
    private String githubUsername;
    private String discordUsername;
    private long huddleId;

    // used by Spring to create the incoming form
    public RegistrationForm() {
    }

    // used by code to pre-populate ID
    public RegistrationForm(HuddleId huddleId) {
        this.huddleId = huddleId.id();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public long getHuddleId() {
        return huddleId;
    }

    public void setHuddleId(long huddleId) {
        this.huddleId = huddleId;
    }

    public String getDiscordUsername() {
        return discordUsername;
    }

    public void setDiscordUsername(String discordUsername) {
        this.discordUsername = discordUsername;
    }
}
