package space.merrioverse.disCoreEmbedded;

import io.github.mrbest2525.disCoreBot.api.event.DisCoreBotRegisterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import space.merrioverse.disCoreEmbedded.disCoreAddon.eBoot;
import space.merrioverse.disCoreEmbedded.disCoreAddon.eChat;
import space.merrioverse.disCoreEmbedded.disCoreAddon.eJoinAndLeave;

import java.io.File;

public final class DisCoreEmbedded extends JavaPlugin implements Listener {
    private space.merrioverse.disCoreEmbedded.disCoreAddon.eJoinAndLeave addonEmbedJoinAndLeave;
    private space.merrioverse.disCoreEmbedded.disCoreAddon.eChat addonEmbedChat;
    private space.merrioverse.disCoreEmbedded.disCoreAddon.eBoot addonEmbedBoot;
    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        addonEmbedBoot.onDisable();
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event){
        getLogger().info("Loading DisCoreEmbedded Addons...");
        addonEmbedJoinAndLeave = new eJoinAndLeave(this);
        addonEmbedChat = new eChat(this);
        addonEmbedBoot = new eBoot(this);
    }

    public File getAddonDataDir() {
        return new File(getDataFolder(), "addons");
    }
}
