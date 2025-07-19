package io.github.railroad.github.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.railroad.core.gson.GsonLocator;
import io.github.railroad.core.vcs.Repository;
import io.github.railroad.core.vcs.RepositoryTypes;
import lombok.Getter;

import java.util.Map;

import static io.github.railroad.github.util.JsonUtils.*;

@Getter
public class GithubRepository extends Repository {
    private final int id;
    private final String nodeId;
    private final String name;
    private final String fullName;
    private final GithubUser owner;
    private final boolean privateRepo;
    private final String htmlUrl;
    private final String description;
    private final boolean fork;
    private final String url;
    private final String archiveUrl;
    private final String assigneesUrl;
    private final String blobsUrl;
    private final String branchesUrl;
    private final String collaboratorsUrl;
    private final String commentsUrl;
    private final String commitsUrl;
    private final String compareUrl;
    private final String contentsUrl;
    private final String contributorsUrl;
    private final String deploymentsUrl;
    private final String downloadsUrl;
    private final String eventsUrl;
    private final String forksUrl;
    private final String gitCommitsUrl;
    private final String gitRefsUrl;
    private final String gitTagsUrl;
    private final String gitUrl; // nullable
    private final String issueCommentUrl;
    private final String issueEventsUrl;
    private final String issuesUrl;
    private final String keysUrl;
    private final String labelsUrl;
    private final String languagesUrl;
    private final String mergesUrl;
    private final String milestonesUrl;
    private final String notificationsUrl;
    private final String pullsUrl;
    private final String releasesUrl;
    private final String sshUrl; // nullable
    private final String stargazersUrl;
    private final String statusesUrl;
    private final String subscribersUrl;
    private final String subscriptionUrl;
    private final String tagsUrl;
    private final String teamsUrl;
    private final String treesUrl;
    private final String cloneUrl; // nullable
    private final String mirrorUrl; // nullable
    private final String hooksUrl; // nullable
    private final String svnUrl; // nullable
    private final String homepage; // nullable
    private final String language; // nullable
    private final Integer forksCount; // nullable
    private final Integer stargazersCount; // nullable
    private final Integer watchersCount; // nullable
    private final Integer size; // nullable
    private final String defaultBranch; // nullable
    private final Integer openIssuesCount; // nullable
    private final Boolean isTemplate; // nullable
    private final String[] topics; // nullable
    private final Boolean hasIssues; // nullable
    private final Boolean hasProjects; // nullable
    private final Boolean hasWiki; // nullable
    private final Boolean hasPages; // nullable
    private final Boolean hasDownloads; // nullable
    private final Boolean hasDiscussions; // nullable
    private final Boolean archived; // nullable
    private final Boolean disabled; // nullable
    private final String visibility; // nullable
    private final String pushedAt; // nullable
    private final String createdAt; // nullable
    private final String updatedAt; // nullable
    private final Map<String, Boolean> permissions; // nullable
    private final String roleName; // nullable
    private final String tempCloneToken; // nullable
    private final Boolean deleteBranchOnMerge; // nullable
    private final Integer subscribersCount; // nullable
    private final Integer networkCount; // nullable
    private final CodeOfConduct codeOfConduct; // nullable
    private final License license; // nullable
    private final Integer forks; // nullable
    private final Integer openIssues; // nullable
    private final Integer watchers; // nullable
    private final Boolean allowForking; // nullable
    private final Boolean webCommitSignoffRequired; // nullable
    private final Map<String, SecurityAndAnalysisRecord> securityAndAnalysis; // nullable

    public static GithubRepository fromJson(JsonObject json) {
        Gson gson = GsonLocator.getInstance();
        return new GithubRepository(
                getAsInt(json, "id"),
                getStringOrNull(json, "node_id"),
                getStringOrNull(json, "name"),
                getStringOrNull(json, "full_name"),
                GithubUser.fromJson(json.getAsJsonObject("owner")),
                getAsBoolean(json, "private"),
                getStringOrNull(json, "html_url"),
                getStringOrNull(json, "description"),
                getAsBoolean(json, "fork"),
                getStringOrNull(json, "url"),
                getStringOrNull(json, "archive_url"),
                getStringOrNull(json, "assignees_url"),
                getStringOrNull(json, "blobs_url"),
                getStringOrNull(json, "branches_url"),
                getStringOrNull(json, "collaborators_url"),
                getStringOrNull(json, "comments_url"),
                getStringOrNull(json, "commits_url"),
                getStringOrNull(json, "compare_url"),
                getStringOrNull(json, "contents_url"),
                getStringOrNull(json, "contributors_url"),
                getStringOrNull(json, "deployments_url"),
                getStringOrNull(json, "downloads_url"),
                getStringOrNull(json, "events_url"),
                getStringOrNull(json, "forks_url"),
                getStringOrNull(json, "git_commits_url"),
                getStringOrNull(json, "git_refs_url"),
                getStringOrNull(json, "git_tags_url"),
                getStringOrNull(json, "git_url"),
                getStringOrNull(json, "issue_comment_url"),
                getStringOrNull(json, "issue_events_url"),
                getStringOrNull(json, "issues_url"),
                getStringOrNull(json, "keys_url"),
                getStringOrNull(json, "labels_url"),
                getStringOrNull(json, "languages_url"),
                getStringOrNull(json, "merges_url"),
                getStringOrNull(json, "milestones_url"),
                getStringOrNull(json, "notifications_url"),
                getStringOrNull(json, "pulls_url"),
                getStringOrNull(json, "releases_url"),
                getStringOrNull(json, "ssh_url"),
                getStringOrNull(json, "stargazers_url"),
                getStringOrNull(json, "statuses_url"),
                getStringOrNull(json, "subscribers_url"),
                getStringOrNull(json, "subscription_url"),
                getStringOrNull(json, "tags_url"),
                getStringOrNull(json, "teams_url"),
                getStringOrNull(json, "trees_url"),
                getStringOrNull(json, "clone_url"),
                getStringOrNull(json, "mirror_url"),
                getStringOrNull(json, "hooks_url"),
                getStringOrNull(json, "svn_url"),
                getStringOrNull(json, "homepage"),
                getStringOrNull(json, "language"),
                getIntegerOrNull(json, "forks_count"),
                getIntegerOrNull(json, "stargazers_count"),
                getIntegerOrNull(json, "watchers_count"),
                getIntegerOrNull(json, "size"),
                getStringOrNull(json, "default_branch"),
                getIntegerOrNull(json, "open_issues_count"),
                getBooleanOrNull(json, "is_template"),
                json.has("topics") ? gson.fromJson(json.get("topics"), String[].class) : null,
                getBooleanOrNull(json, "has_issues"),
                getBooleanOrNull(json, "has_projects"),
                getBooleanOrNull(json, "has_wiki"),
                getBooleanOrNull(json, "has_pages"),
                getBooleanOrNull(json, "has_downloads"),
                getBooleanOrNull(json, "has_discussions"),
                getBooleanOrNull(json, "archived"),
                getBooleanOrNull(json, "disabled"),
                getStringOrNull(json, "visibility"),
                getStringOrNull(json, "pushed_at"),
                getStringOrNull(json, "created_at"),
                getStringOrNull(json, "updated_at"),
                json.has("permissions") ? gson.fromJson(json.get("permissions"), new TypeToken<Map<String, Boolean>>() {}.getType()) : null,
                getStringOrNull(json, "role_name"),
                getStringOrNull(json, "temp_clone_token"),
                getBooleanOrNull(json, "delete_branch_on_merge"),
                getIntegerOrNull(json, "subscribers_count"),
                getIntegerOrNull(json, "network_count"),
                json.has("code_of_conduct") ? CodeOfConduct.fromJson(json.get("code_of_conduct")) : null,
                json.has("license") ? License.fromJson(json.get("license")) : null,
                getIntegerOrNull(json, "forks"),
                getIntegerOrNull(json, "open_issues"),
                getIntegerOrNull(json, "watchers"),
                getBooleanOrNull(json, "allow_forking"),
                getBooleanOrNull(json, "web_commit_signoff_required"),
                json.has("security_and_analysis") ? SecurityAndAnalysisRecord.fromJson(json.get("security_and_analysis").getAsJsonObject()) : null
        );
    }

    public GithubRepository(int id, String nodeId, String name, String fullName, GithubUser owner, boolean privateRepo, String htmlUrl, String description, boolean fork, String url, String archiveUrl, String assigneesUrl, String blobsUrl, String branchesUrl, String collaboratorsUrl, String commentsUrl, String commitsUrl, String compareUrl, String contentsUrl, String contributorsUrl, String deploymentsUrl, String downloadsUrl, String eventsUrl, String forksUrl, String gitCommitsUrl, String gitRefsUrl, String gitTagsUrl, String gitUrl, String issueCommentUrl, String issueEventsUrl, String issuesUrl, String keysUrl, String labelsUrl, String languagesUrl, String mergesUrl, String milestonesUrl, String notificationsUrl, String pullsUrl, String releasesUrl, String sshUrl, String stargazersUrl, String statusesUrl, String subscribersUrl, String subscriptionUrl, String tagsUrl, String teamsUrl, String treesUrl, String cloneUrl, String mirrorUrl, String hooksUrl, String svnUrl, String homepage, String language, Integer forksCount, Integer stargazersCount, Integer watchersCount, Integer size, String defaultBranch, Integer openIssuesCount, Boolean isTemplate, String[] topics, Boolean hasIssues, Boolean hasProjects, Boolean hasWiki, Boolean hasPages, Boolean hasDownloads, Boolean hasDiscussions, Boolean archived, Boolean disabled, String visibility, String pushedAt, String createdAt, String updatedAt, Map<String, Boolean> permissions, String roleName, String tempCloneToken, Boolean deleteBranchOnMerge, Integer subscribersCount, Integer networkCount, CodeOfConduct codeOfConduct, License license, Integer forks, Integer openIssues, Integer watchers, Boolean allowForking, Boolean webCommitSignoffRequired, Map<String, SecurityAndAnalysisRecord> securityAndAnalysis) {
        super(RepositoryTypes.GIT);
        this.id = id;
        this.nodeId = nodeId;
        this.name = name;
        this.fullName = fullName;
        this.owner = owner;
        this.privateRepo = privateRepo;
        this.htmlUrl = htmlUrl;
        this.description = description;
        this.fork = fork;
        this.url = url;
        this.archiveUrl = archiveUrl;
        this.assigneesUrl = assigneesUrl;
        this.blobsUrl = blobsUrl;
        this.branchesUrl = branchesUrl;
        this.collaboratorsUrl = collaboratorsUrl;
        this.commentsUrl = commentsUrl;
        this.commitsUrl = commitsUrl;
        this.compareUrl = compareUrl;
        this.contentsUrl = contentsUrl;
        this.contributorsUrl = contributorsUrl;
        this.deploymentsUrl = deploymentsUrl;
        this.downloadsUrl = downloadsUrl;
        this.eventsUrl = eventsUrl;
        this.forksUrl = forksUrl;
        this.gitCommitsUrl = gitCommitsUrl;
        this.gitRefsUrl = gitRefsUrl;
        this.gitTagsUrl = gitTagsUrl;
        this.gitUrl = gitUrl;
        this.issueCommentUrl = issueCommentUrl;
        this.issueEventsUrl = issueEventsUrl;
        this.issuesUrl = issuesUrl;
        this.keysUrl = keysUrl;
        this.labelsUrl = labelsUrl;
        this.languagesUrl = languagesUrl;
        this.mergesUrl = mergesUrl;
        this.milestonesUrl = milestonesUrl;
        this.notificationsUrl = notificationsUrl;
        this.pullsUrl = pullsUrl;
        this.releasesUrl = releasesUrl;
        this.sshUrl = sshUrl;
        this.stargazersUrl = stargazersUrl;
        this.statusesUrl = statusesUrl;
        this.subscribersUrl = subscribersUrl;
        this.subscriptionUrl = subscriptionUrl;
        this.tagsUrl = tagsUrl;
        this.teamsUrl = teamsUrl;
        this.treesUrl = treesUrl;
        this.cloneUrl = cloneUrl;
        this.mirrorUrl = mirrorUrl;
        this.hooksUrl = hooksUrl;
        this.svnUrl = svnUrl;
        this.homepage = homepage;
        this.language = language;
        this.forksCount = forksCount;
        this.stargazersCount = stargazersCount;
        this.watchersCount = watchersCount;
        this.size = size;
        this.defaultBranch = defaultBranch;
        this.openIssuesCount = openIssuesCount;
        this.isTemplate = isTemplate;
        this.topics = topics;
        this.hasIssues = hasIssues;
        this.hasProjects = hasProjects;
        this.hasWiki = hasWiki;
        this.hasPages = hasPages;
        this.hasDownloads = hasDownloads;
        this.hasDiscussions = hasDiscussions;
        this.archived = archived;
        this.disabled = disabled;
        this.visibility = visibility;
        this.pushedAt = pushedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.permissions = permissions;
        this.roleName = roleName;
        this.tempCloneToken = tempCloneToken;
        this.deleteBranchOnMerge = deleteBranchOnMerge;
        this.subscribersCount = subscribersCount;
        this.networkCount = networkCount;
        this.codeOfConduct = codeOfConduct;
        this.license = license;
        this.forks = forks;
        this.openIssues = openIssues;
        this.watchers = watchers;
        this.allowForking = allowForking;
        this.webCommitSignoffRequired = webCommitSignoffRequired;
        this.securityAndAnalysis = securityAndAnalysis;

        setRepositoryType(RepositoryTypes.GIT);
        setRepositoryURL(htmlUrl);
        setRepositoryCloneURL(cloneUrl != null ? cloneUrl : gitUrl);
        setRepositoryName(fullName);
    }

    public record CodeOfConduct(String key, String name, String url, String htmlUrl, String body) {
        public static CodeOfConduct fromJson(JsonElement jsonElem) {
            if (jsonElem == null || !jsonElem.isJsonObject())
                return null;

            JsonObject json = jsonElem.getAsJsonObject();
            return new CodeOfConduct(
                    getStringOrNull(json, "key"),
                    getStringOrNull(json, "name"),
                    getStringOrNull(json, "url"),
                    getStringOrNull(json, "html_url"),
                    getStringOrNull(json, "body")
            );
        }
    }

    public record License(String key, String name, String spdxId, String url, String nodeId) {
        public static License fromJson(JsonElement jsonElem) {
            if (jsonElem == null || !jsonElem.isJsonObject())
                return null;

            JsonObject json = jsonElem.getAsJsonObject();
            return new License(
                    getStringOrNull(json, "key"),
                    getStringOrNull(json, "name"),
                    getStringOrNull(json, "spdx_id"),
                    getStringOrNull(json, "url"),
                    getStringOrNull(json, "node_id")
            );
        }
    }

    public record SecurityAndAnalysisRecord(Status status) {
        public static Map<String, SecurityAndAnalysisRecord> fromJson(JsonObject json) {
            Gson gson = GsonLocator.getInstance();
            return gson.fromJson(json, new TypeToken<Map<String, SecurityAndAnalysisRecord>>() {
            }.getType());
        }

        public enum Status {
            ENABLED, DISABLED, UNKNOWN
        }
    }
}
