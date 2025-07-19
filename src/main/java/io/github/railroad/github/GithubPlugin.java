package io.github.railroad.github;

import com.google.gson.reflect.TypeToken;
import io.github.railroad.core.registry.Registry;
import io.github.railroad.core.secure_storage.SecureTokenStore;
import io.github.railroad.core.settings.Setting;
import io.github.railroad.core.settings.SettingCategory;
import io.github.railroad.core.settings.SettingCodec;
import io.github.railroad.github.data.GithubAccount;
import io.github.railroad.github.ui.GithubAccountsPane;
import io.github.railroad.logger.Logger;
import io.github.railroad.railroadpluginapi.Plugin;
import io.github.railroad.railroadpluginapi.PluginContext;
import io.github.railroad.railroadpluginapi.Registries;
import io.github.railroad.railroadpluginapi.services.VCSService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GithubPlugin implements Plugin {
    public static final SecureTokenStore TOKEN_STORAGE = new SecureTokenStore("github");

    public static Logger logger;

    @Override
    public void onEnable(PluginContext context) {
        if (context == null)
            throw new IllegalArgumentException("PluginContext cannot be null");

        logger = context.getLogger(); // TODO: setLogger

        VCSService vcsService = context.getService(VCSService.class);
        if (vcsService == null)
            throw new IllegalStateException("VCSService is not available in the context.");

        SettingCodec<List<GithubAccount>, GithubAccountsPane> codec = SettingCodec.<List<GithubAccount>, GithubAccountsPane>builder()
                .id("github:accounts")
                .nodeToValue(GithubAccountsPane::getAccounts)
                .valueToNode((accounts, pane) -> pane.setAccounts(accounts))
                .jsonDecoder(json -> {
                    List<GithubAccount> accounts = GithubAccount.listFromJson(json);
                    for (GithubAccount account : accounts) {
                        vcsService.addProfile(account);
                    }

                    return accounts;
                })
                .jsonEncoder(GithubAccount::listToJson)
                .createNode(GithubAccountsPane::new)
                .build();

        Registry<Setting<?>> settingRegistry = Registries.getSettingsRegistry(context);
        settingRegistry.register("github:accounts", Setting.builder(new TypeToken<List<GithubAccount>>() {}, "github:accounts")
                .treePath("plugins.github")
                .category(SettingCategory.simple("railroad:plugins.github"))
                .codec(codec)
                .defaultValue(new ArrayList<>())
                .addListener((oldValue, newValue) -> {
                    Map<Integer, GithubAccount> oldById = oldValue.stream()
                            .collect(Collectors.toMap(GithubAccount::getUserId, Function.identity()));
                    Map<Integer, GithubAccount> newById = newValue.stream()
                            .collect(Collectors.toMap(GithubAccount::getUserId, Function.identity()));

                    List<GithubAccount> addedAccounts = newValue.stream()
                            .filter(account -> !oldById.containsKey(account.getUserId()))
                            .toList();

                    List<GithubAccount> removedAccounts = oldValue.stream()
                            .filter(account -> !newById.containsKey(account.getUserId()))
                            .toList();

                    for (GithubAccount account : addedAccounts) {
                        GithubPlugin.TOKEN_STORAGE.saveToken(account.getAndClearAccessToken(), "railroad_github_access_token_" + account.getUserId());
                        vcsService.addProfile(account);
                        logger.info("Added Github account: userId={}, alias={}", account.getUserId(), account.getAlias());
                    }

                    for (GithubAccount account : removedAccounts) {
                        GithubPlugin.TOKEN_STORAGE.clearToken("railroad_github_access_token_" + account.getUserId());
                        vcsService.removeProfile(account);
                        logger.info("Removed Github account: userId={}, alias={}", account.getUserId(), account.getAlias());
                    }
                })
                .build());
    }

    @Override
    public void onDisable(PluginContext context) {
        Registry<Setting<?>> settingRegistry = Registries.getSettingsRegistry(context);
        settingRegistry.unregister("github:accounts");
    }
}
