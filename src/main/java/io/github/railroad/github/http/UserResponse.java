package io.github.railroad.github.http;

import com.google.gson.JsonObject;

public record UserResponse(String login, int id, String nodeId, String avatarUrl, String gravatarId, String url,
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

    public static UserResponse fromJson(JsonObject json) {
        return new UserResponse(
                json.get("login").getAsString(),
                json.get("id").getAsInt(),
                json.get("node_id").getAsString(),
                json.get("avatar_url").getAsString(),
                json.get("gravatar_id").getAsString(),
                json.get("url").getAsString(),
                json.get("html_url").getAsString(),
                json.get("followers_url").getAsString(),
                json.get("following_url").getAsString(),
                json.get("gists_url").getAsString(),
                json.get("starred_url").getAsString(),
                json.get("subscriptions_url").getAsString(),
                json.get("organizations_url").getAsString(),
                json.get("repos_url").getAsString(),
                json.get("events_url").getAsString(),
                json.get("received_events_url").getAsString(),
                json.get("type").getAsString(),
                json.get("user_view_type").getAsString(),
                json.get("site_admin").getAsBoolean(),
                json.has("name") ? json.get("name").getAsString() : null,
                json.has("company") ? json.get("company").getAsString() : null,
                json.has("blog") ? json.get("blog").getAsString() : null,
                json.has("location") ? json.get("location").getAsString() : null,
                json.has("email") && !json.get("email").isJsonNull() ? json.get("email").getAsString() : null,
                json.has("hireable") && !json.get("hireable").isJsonNull() ? json.get("hireable").getAsBoolean() : null,
                json.has("bio") ? json.get("bio").getAsString() : null,
                json.has("twitter_username") && !json.get("twitter_username").isJsonNull() ?
                        (json.get("twitter_username")).getAsString() : null,
                json.has("public_repos") ? json.get("public_repos").getAsInt() : 0,
                json.has("public_gists") ? json.get("public_gists").getAsInt() : 0,
                json.has("followers") ? json.get("followers").getAsInt() : 0,
                json.has("following") ? json.get("following").getAsInt() : 0,
                json.get("created_at").getAsString(),
                json.get("updated_at").getAsString(),
                json.has("private_gists") ? json.get("private_gists").getAsInt() : null,
                json.has("total_private_repos") ? json.get("total_private_repos").getAsInt() : null,
                json.has("owned_private_repos") ? json.get("owned_private_repos").getAsInt() : null,
                json.has("disk_usage") ? json.get("disk_usage").getAsInt() : null,
                json.has("collaborators") ? json.get("collaborators").getAsInt() : null,
                json.has("two_factor_authentication") ? json.get("two_factor_authentication").getAsBoolean() : null,
                json.has("plan") ? Plan.fromJson(json.getAsJsonObject("plan")) : null,
                json.has("business_plan") ? json.get("business_plan").getAsBoolean() : null,
                json.has("ldap_dn") ? json.get("ldap_dn").getAsString() : null
        );
    }
}
