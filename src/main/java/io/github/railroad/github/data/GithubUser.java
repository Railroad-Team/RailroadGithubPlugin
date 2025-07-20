package io.github.railroad.github.data;

import com.google.gson.JsonObject;

import static io.github.railroad.github.util.JsonUtils.*;

public record GithubUser(String login, int id, String nodeId, String avatarUrl, String gravatarId, String url,
                         String htmlUrl, String followersUrl, String followingUrl, String gistsUrl, String starredUrl,
                         String subscriptionsUrl, String organizationsUrl, String reposUrl, String eventsUrl,
                         String receivedEventsUrl, String type, String userViewType, boolean siteAdmin, String name,
                         String company, String blog, String location, String email, Boolean hireable, String bio,
                         String twitterUsername, int publicRepos, int publicGists, int followers, int following,
                         String createdAt, String updatedAt, Integer privateGists, Integer totalPrivateRepos,
                         Integer ownedPrivateRepos, Integer diskUsage, Integer collaborators,
                         Boolean twoFactorAuthentication, Plan plan, Boolean businessPlan, String ldapDn) {
    public record Plan(int collaborators, String name, int space, int privateRepos) {
        public static Plan fromJson(JsonObject json) {
            return new Plan(
                    json.get("collaborators").getAsInt(),
                    json.get("name").getAsString(),
                    json.get("space").getAsInt(),
                    json.get("private_repos").getAsInt()
            );
        }
    }

    public static GithubUser fromJson(JsonObject json) {
        return new GithubUser(
                getStringOrNull(json, "login"),
                getAsInt(json, "id"),
                getStringOrNull(json, "node_id"),
                getStringOrNull(json, "avatar_url"),
                getStringOrNull(json, "gravatar_id"),
                getStringOrNull(json, "url"),
                getStringOrNull(json, "html_url"),
                getStringOrNull(json, "followers_url"),
                getStringOrNull(json, "following_url"),
                getStringOrNull(json, "gists_url"),
                getStringOrNull(json, "starred_url"),
                getStringOrNull(json, "subscriptions_url"),
                getStringOrNull(json, "organizations_url"),
                getStringOrNull(json, "repos_url"),
                getStringOrNull(json, "events_url"),
                getStringOrNull(json, "received_events_url"),
                getStringOrNull(json, "type"),
                getStringOrNull(json, "user_view_type"),
                getAsBoolean(json, "site_admin"),
                getStringOrNull(json, "name"),
                getStringOrNull(json, "company"),
                getStringOrNull(json, "blog"),
                getStringOrNull(json, "location"),
                getStringOrNull(json, "email"),
                getBooleanOrNull(json, "hireable"),
                getStringOrNull(json, "bio"),
                getStringOrNull(json, "twitter_username"),
                getAsInt(json, "public_repos"),
                getAsInt(json, "public_gists"),
                getAsInt(json, "followers"),
                getAsInt(json, "following"),
                getStringOrNull(json, "created_at"),
                getStringOrNull(json, "updated_at"),
                getIntegerOrNull(json, "private_gists"),
                getIntegerOrNull(json, "total_private_repos"),
                getIntegerOrNull(json, "owned_private_repos"),
                getIntegerOrNull(json, "disk_usage"),
                getIntegerOrNull(json, "collaborators"),
                getBooleanOrNull(json, "two_factor_authentication"),
                json.has("plan") ? Plan.fromJson(json.getAsJsonObject("plan")) : null,
                getBooleanOrNull(json, "business_plan"),
                getStringOrNull(json, "ldap_dn")
        );
    }
}
