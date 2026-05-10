package space.merrioverse.disCoreEmbedded.disCoreAddon;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import io.github.mrbest2525.disCoreBot.api.DisCoreBotApi;
import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.util.DeviceOs;
import space.merrioverse.disCoreEmbedded.DisCoreEmbedded;

import java.io.File;
import java.io.IOException;

public class eJoinAndLeave implements Listener{
    private final DisCoreEmbedded embedded;
    private final File configFile;
    private YamlConfiguration config;
    private long[] timeArray;
    private String[] nameArray;
    private FloodgatePlayer onBedrock;
    private boolean enabled;

    private final NamespacedKey EMBEDDED_JOIN_AND_LEAVE;

    public eJoinAndLeave(DisCoreEmbedded embedded) {
        this.embedded = embedded;
        EMBEDDED_JOIN_AND_LEAVE = NamespacedKey.fromString("join_and_leave", embedded);

        this.configFile = new File(embedded.getAddonDataDir(), "embedded_join_and_leave/join_and_leave.yml");
        loadConfig();

        // アドオンが有効設定の場合のみ機能させる
        enabled = config.getBoolean("enabled");
        if (!enabled) return;
        embedded.getServer().getPluginManager().registerEvents(this, embedded);

    }

    private void loadConfig() {
        timeArray = new long[50];
        nameArray = new String[50];
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            // デフォルト設定の生成が必要ならここで行う
            config = new YamlConfiguration();
            config.set("enabled", false);
            config.set("channel-id", "0123456789");
            config.set("server-name", "A Minecraft Server");
            config.set("icon-url", "");
            config.set("join-comment", "接続しました");
            config.set("leave-comment", "切断されました");
            try {
                config.save(configFile);
            } catch (IOException e) {
                Bukkit.getLogger().severe(e.toString());
            }
        } else {config = YamlConfiguration.loadConfiguration(configFile);

        }
    }

    public void onRegister(DisCoreBotRegisterEvent event) {
        event.registerM2D(EMBEDDED_JOIN_AND_LEAVE, config.getString("channel-id"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        String avatarUrl = String.format("https://mc-heads.net/avatar/%s", event.getPlayer().getUniqueId());
        String onDevice;
        String comment = config.getString("join-comment");
        WebhookEmbed embed;
        int i = 0;
        do {
            if (nameArray[i] == null) {
                nameArray[i] = playerName;
                timeArray[i] = System.currentTimeMillis();
                break;
            } else {
                i++;
            }
        } while ( i < 50);
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            onBedrock = FloodgateApi.getInstance().getPlayer(event.getPlayer().getUniqueId());
        }
            if (onBedrock != null) {
                DeviceOs device = onBedrock.getDeviceOs();
                if (device == DeviceOs.NX) {
                    onDevice = "Nintendo Switch";
                } else if (device == DeviceOs.PS4) {
                    onDevice = "PlayStation";
                } else if (device == DeviceOs.XBOX) {
                    onDevice = "Microsoft Xbox";
                } else if (device == DeviceOs.GEARVR || device == DeviceOs.HOLOLENS) {
                    onDevice = "VR Device";
                } else if (device == DeviceOs.WIN32 || device == DeviceOs.UWP || device == DeviceOs.WINDOWS_PHONE) {
                    onDevice = "Windows Device";
                } else if (device == DeviceOs.IOS) {
                    onDevice = "Apple Device";
                } else if (device == DeviceOs.GOOGLE) {
                    onDevice = "Android Device";
                } else if (device == DeviceOs.AMAZON) {
                    onDevice = "Amazon Fire Device";
                } else {
                    onDevice = device.toString();
                }
                embed = new WebhookEmbedBuilder()
                        .setAuthor(new WebhookEmbed.EmbedAuthor("<" + playerName + "> " + comment, avatarUrl, null))
                        .setFooter(new WebhookEmbed.EmbedFooter("Bedrock Edition - " + onDevice, null))
                        .setColor(0x00FF00)
                        .build();
            } else {
                embed = new WebhookEmbedBuilder()
                        .setAuthor(new WebhookEmbed.EmbedAuthor("<" + playerName + "> " + comment, avatarUrl, null))
                        .setFooter(new WebhookEmbed.EmbedFooter("Java Edition",null))
                        .setColor(0x00FF00)
                        .build();
            }
        WebhookMessage message = new WebhookMessageBuilder()
                .addEmbeds(embed)
                .setAvatarUrl(config.getString("icon-url"))
                .setUsername(config.getString("server-name"))
                .build();
        if (!enabled) return;
        DisCoreBotApi.getInstance().sendMessage(EMBEDDED_JOIN_AND_LEAVE, config.getString("channel-id"), message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        String avatarUrl = String.format("https://mc-heads.net/avatar/%s", event.getPlayer().getUniqueId());
        String comment = config.getString("leave-comment");
        WebhookEmbed embed;
        int i = 0;
        long online = System.currentTimeMillis();
        long days = 0;
        long hours = 0;
        long minutes = 0;
        do {
            if (nameArray[i] == playerName) {
                online -= timeArray[i];
                days = online / (1000 * 60 * 60 * 24);
                hours = (online / (1000 * 60 * 60)) % 24;
                minutes = (online / (1000 * 60)) % 60;
                break;
            } else {
                i++;
            }
        } while (i < 50);
        nameArray[i] = null;
        String timeString;
        if (days != 0) {
            timeString = String.format("接続日数: %d日\n接続時間: %d時間 %d分", days,hours,minutes);
        } else if ( hours != 0) {
            timeString = String.format("接続時間: %d時間 %d分", hours,minutes);
        } else if (minutes != 0) {
            timeString = String.format("接続時間: %d分", minutes);
        } else timeString = "";
        embed = new WebhookEmbedBuilder()
                .setAuthor(new WebhookEmbed.EmbedAuthor("<" + playerName + "> " + comment, avatarUrl, null))
                .setFooter(new WebhookEmbed.EmbedFooter(timeString,null))
                .setColor(0xFF0000)
                .build();
        WebhookMessage message = new WebhookMessageBuilder()
                .addEmbeds(embed)
                .setAvatarUrl(config.getString("icon-url"))
                .setUsername(config.getString("server-name"))
                .build();
        if (!enabled) return;
        DisCoreBotApi.getInstance().sendMessage(EMBEDDED_JOIN_AND_LEAVE, config.getString("channel-id"), message);
    }
}
