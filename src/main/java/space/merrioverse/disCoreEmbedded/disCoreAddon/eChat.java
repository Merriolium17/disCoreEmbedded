package space.merrioverse.disCoreEmbedded.disCoreAddon;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import io.github.mrbest2525.disCoreBot.api.DisCoreBotApi;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import space.merrioverse.disCoreEmbedded.DisCoreEmbedded;

import java.io.File;
import java.io.IOException;


public class eChat implements Listener{
    private final DisCoreEmbedded embedded;
    private final File configFile;
    private YamlConfiguration config;

    private final NamespacedKey EMBEDDED_CHAT;

    public eChat(DisCoreEmbedded embedded) {
        this.embedded = embedded;
        EMBEDDED_CHAT = NamespacedKey.fromString("chat", embedded);

        this.configFile = new File(embedded.getAddonDataDir(), "embedded_chat/chat.yml");
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

    @EventHandler
    public void onRegister(DisCoreBotRegisterEvent event) {
        event.registerM2D(EMBEDDED_CHAT, config.getString("channel-id"));
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String playerName = event.getPlayer().getName();
        String avatarUrl = String.format("https://mc-heads.net/avatar/%s", event.getPlayer().getUniqueId());

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setAuthor(new WebhookEmbed.EmbedAuthor("<" + playerName + ">", avatarUrl, null))
                .setTitle(new WebhookEmbed.EmbedTitle(event.message().toString(), null))
                .setColor(0xFFFF00)
                .build();
        WebhookMessage message = new WebhookMessageBuilder()
                .addEmbeds(embed)
                .setAvatarUrl(config.getString("icon-url"))
                .setUsername(config.getString("server-name"))
                .build();
        DisCoreBotApi.getInstance().sendMessage(EMBEDDED_CHAT, config.getString("channel-id"), message);
    }
}
