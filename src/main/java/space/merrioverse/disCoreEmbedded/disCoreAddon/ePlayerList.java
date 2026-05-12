package space.merrioverse.disCoreEmbedded.disCoreAddon;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import io.github.mrbest2525.disCoreBot.api.DisCoreBotApi;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotDiscordMsgEvent;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import space.merrioverse.disCoreEmbedded.DisCoreEmbedded;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ePlayerList implements Listener {
    private final DisCoreEmbedded embedded;
    private final File configFile;
    private final NamespacedKey PLAYER_LIST;
    private boolean enabled;
    private boolean thread;

    private YamlConfiguration config;
    public ePlayerList(DisCoreEmbedded embedded) {
        this.embedded = embedded;
        this.configFile = new File(embedded.getAddonDataDir(),"embedded_playerlist");
        PLAYER_LIST = NamespacedKey.fromString("list", embedded);
        enabled = config.getBoolean("enabled");
        if (!enabled) return;
        // チャンネルかスレッドかを設定する
        thread = config.getBoolean("thread-mode");
        embedded.getServer().getPluginManager().registerEvents(this, embedded);
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            // デフォルト設定の生成が必要ならここで行う
            config = new YamlConfiguration();
            config.set("enabled", false);
            config.set("thread-mode", false);
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
        event.registerM2D(PLAYER_LIST, config.getString("channel-id"));
    }

    @EventHandler
    public void onDiscordMsg(DisCoreBotDiscordMsgEvent event) {
        if (event.getMessage().equals("playerlist") && event.getChannelID().equals(config.getString("channel-id"))) {
            playerList();
        }
    }
    public void playerList() {
        List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());

        String playerListString = playerNames.isEmpty() ? "現在参加者はいません" : String.join("\n- ", playerNames);
        String players = String.format("- %s",playerListString);
        String title = String.format("オンライン人数: %d人", playerNames.size());
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle(title,null))
                .setDescription(players)
                .setFooter(new WebhookEmbed.EmbedFooter("プレイヤーリスト",null))
                .build();
        WebhookMessage message = new WebhookMessageBuilder()
                .addEmbeds(embed)
                .setAvatarUrl(config.getString("icon-url"))
                .setUsername(config.getString("server-name"))
                .build();
        if (thread) {
            DisCoreBotApi.getInstance().sendMessage(PLAYER_LIST, null, config.getString("channel-id"), message);
        } else {
            DisCoreBotApi.getInstance().sendMessage(PLAYER_LIST, config.getString("channel-id"), null, message);
        }
    }

}
