package dev.railroadide.githubplugin.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.railroadide.core.vcs.connections.AbstractConnection;
import dev.railroadide.core.vcs.connections.ProfileType;
import dev.railroadide.core.vcs.connections.VCSProfile;
import io.github.palexdev.mfxresources.fonts.IconsProviders;
import io.github.palexdev.mfxresources.fonts.fontawesome.FontAwesomeBrands;
import dev.railroadide.githubplugin.GithubConnection;
import dev.railroadide.githubplugin.GithubPlugin;
import dev.railroadide.githubplugin.http.GithubRequests;
import dev.railroadide.githubplugin.util.JsonUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class GithubAccount extends VCSProfile {
    private static final ProfileType TYPE = new ProfileType("Github", FontAwesomeBrands.GITHUB.getDescription(), IconsProviders.FONTAWESOME_BRANDS);

    private final int userId;
    private GithubUser user;
    private char[] accessToken;

    public GithubAccount(int userId) {
        this.userId = userId;
    }

    public GithubAccount(GithubUser user) {
        this.userId = user.id();
        this.user = user;
    }

    public GithubUser getOrRequestUser() {
        if (user == null) {
            user = GithubRequests.requestUser(getUserId());
        }

        return user;
    }

    public static List<GithubAccount> listFromJson(JsonElement json) {
        GithubPlugin.LOGGER.debug("Loading Github accounts from JSON, json: {}", json);

        if (json == null || !json.isJsonArray())
            return new ArrayList<>();

        var jsonArray = json.getAsJsonArray();

        List<GithubAccount> accounts = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            if (!element.isJsonObject())
                continue;

            JsonObject jsonObject = element.getAsJsonObject();
            int userId = JsonUtils.getAsInt(jsonObject, "userId");
            String alias = JsonUtils.getStringOrNull(jsonObject, "alias");

            var account = new GithubAccount(userId);
            account.aliasProperty().set(alias);
            accounts.add(account);

            GithubPlugin.LOGGER.debug("Loaded Github account: userId={}, alias={}", userId, alias);
        }

        return accounts;
    }

    public static JsonElement listToJson(List<GithubAccount> accounts) {
        var jsonArray = new JsonArray();
        for (GithubAccount account : accounts) {
            var jsonObject = new JsonObject();
            jsonObject.addProperty("userId", account.getUserId());
            jsonObject.addProperty("alias", account.getAlias());
            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }

    public void setAccessToken(char[] accessToken) {
        if(this.accessToken != null) {
            clearAccessToken();
        }

        if(accessToken == null || accessToken.length == 0) {
            this.accessToken = null;
            return;
        }

        this.accessToken = accessToken;
    }

    public String getAndClearAccessToken() {
        if (accessToken == null || accessToken.length == 0)
            return null;

        var token = new String(accessToken);
        clearAccessToken();
        return token;
    }

    public void clearAccessToken() {
        if (accessToken != null) {
            Arrays.fill(accessToken, '\0');
            accessToken = null;
        }
    }

    @Override
    public AbstractConnection createConnection() {
        return new GithubConnection(this);
    }

    @Override
    public ProfileType getType() {
        return TYPE;
    }
}
