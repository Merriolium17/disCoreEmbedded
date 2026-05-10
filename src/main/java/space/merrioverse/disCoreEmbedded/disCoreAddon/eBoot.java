package space.merrioverse.disCoreEmbedded.disCoreAddon;

import io.github.mrbest2525.disCoreBot.api.DisCoreBotApi;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import space.merrioverse.disCoreEmbedded.DisCoreEmbedded;

import java.io.File;
import java.io.IOException;

public class eBoot implements Listener {
    private final DisCoreEmbedded embedded;
    private final File configFile;

    private DisCoreBotRegisterEvent register;
    private YamlConfiguration config;

    private final NamespacedKey STARTUP_AND_SHUTDOWN;

    public eBoot(DisCoreEmbedded embedded) {
        this.embedded = embedded;
        STARTUP_AND_SHUTDOWN = NamespacedKey.fromString("boot", embedded);

        this.configFile = new File(embedded.getAddonDataDir(), "embedded_boot/boot.yml");
        loadConfig();

        // アドオンが有効設定の場合のみ機能させる
        if (!config.getBoolean("enabled")) return;
        embedded.getServer().getPluginManager().registerEvents(this, embedded);

    }

    private void loadConfig() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            // デフォルト設定の生成が必要ならここで行う
            config = new YamlConfiguration();
            config.set("enabled", false);
            config.set("channel-id", "0123456789");
            config.set("server-name", "A Minecraft Server");
            config.set("icon-url", "");
            try {
                config.save(configFile);
            } catch (IOException e) {
                Bukkit.getLogger().severe(e.toString());
            }
        } else {config = YamlConfiguration.loadConfiguration(configFile);

        }
    }

    public void onRegister(DisCoreBotRegisterEvent event) {
        event.registerM2D(STARTUP_AND_SHUTDOWN, config.getString("channel-id"));
    }

    public void onEnable() {

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle("起動しました", null))
                .setFooter(new WebhookEmbed.EmbedFooter("Powered by DisCoreBot",null))
                .setColor(0x00FF00)
                .build();
        WebhookMessage message = new WebhookMessageBuilder()
                .addEmbeds(embed)
                .setAvatarUrl(config.getString("icon-url"))
                .setUsername(config.getString("server-name"))
                .build();
        DisCoreBotApi.getInstance().sendMessage(STARTUP_AND_SHUTDOWN, config.getString("channel-id"), message);
    }

    public void onDisable() {
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle("停止しました", null))
                .setFooter(new WebhookEmbed.EmbedFooter("Powered by DisCoreBot",null))
                .setColor(0xFF0000)
                .build();
        WebhookMessage message = new WebhookMessageBuilder()
                .addEmbeds(embed)
                .setAvatarUrl(config.getString("icon-url"))
                .setUsername(config.getString("server-name"))
                .build();
        DisCoreBotApi.getInstance().sendMessage(STARTUP_AND_SHUTDOWN, config.getString("channel-id"), message);
    }
}
