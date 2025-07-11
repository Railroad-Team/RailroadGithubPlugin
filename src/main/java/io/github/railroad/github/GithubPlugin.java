package io.github.railroad.github;

import com.google.gson.reflect.TypeToken;
import io.github.railroad.core.registry.Registry;
import io.github.railroad.core.settings.Setting;
import io.github.railroad.core.settings.SettingCategory;
import io.github.railroad.core.settings.SettingCodec;
import io.github.railroad.github.ui.GithubAccountsPane;
import io.github.railroad.railroadpluginapi.Plugin;
import io.github.railroad.railroadpluginapi.PluginContext;
import io.github.railroad.railroadpluginapi.Registries;

import java.util.ArrayList;
import java.util.List;

public class GithubPlugin implements Plugin {
    private static final SettingCodec<List<GithubAccount>, GithubAccountsPane> CODEC =
            SettingCodec.<List<GithubAccount>, GithubAccountsPane>builder()
                    .id("github:accounts")
                    .nodeToValue(GithubAccountsPane::getAccounts)
                    .valueToNode((accounts, pane) -> pane.setAccounts(accounts))
                    .jsonDecoder(GithubAccount::listFromJson)
                    .jsonEncoder(GithubAccount::listToJson)
                    .createNode(GithubAccountsPane::new)
                    .build();

    @Override
    public void onEnable(PluginContext context) {
        if (context == null)
            throw new IllegalArgumentException("PluginContext cannot be null");

        Registry<Setting<?>> settingRegistry = Registries.getSettingsRegistry(context);
        settingRegistry.register("github:accounts", Setting.builder((Class<List<GithubAccount>>) new TypeToken<List<GithubAccount>>() {}.getRawType(), "github:accounts")
                .treePath("plugins.github")
                .category(SettingCategory.simple("railroad:plugins.github"))
                .codec(CODEC)
                .defaultValue(new ArrayList<>())
                .build());
    }

    @Override
    public void onDisable(PluginContext context) {
        Registry<Setting<?>> settingRegistry = Registries.getSettingsRegistry(context);
        settingRegistry.unregister("github:accounts");
    }
}
