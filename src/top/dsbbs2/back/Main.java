package top.dsbbs2.back;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements Listener {
    public static Main instance;
    {
        instance=this;
    }
    public ConcurrentHashMap<UUID, Location> deathLocation=new ConcurrentHashMap<>();
    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this,this);
    }
    @EventHandler(priority= EventPriority.MONITOR,ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        deathLocation.put(e.getEntity().getUniqueId(),e.getEntity().getLocation());
    }
    public static Player proxiedCommandSenderToPlayer(ProxiedCommandSender s)
    {
        if(s.getCallee() instanceof Player)
            return (Player)s.getCallee();
        if(s.getCallee() instanceof ProxiedCommandSender)
            return proxiedCommandSenderToPlayer((ProxiedCommandSender) s.getCallee());
        return null;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("back"))
        {
            Player p=null;
            if(sender instanceof Player)
                p=(Player) sender;
            else if(sender instanceof ProxiedCommandSender)
                p=proxiedCommandSenderToPlayer((ProxiedCommandSender)sender);
            if(p==null) {
                sender.sendMessage("此命令必须由玩家执行!");
                return true;
            }
            if(args.length>=1)
            {
                Optional<UUID> tmp=Arrays.stream(Bukkit.getOfflinePlayers()).filter(i-> Objects.equals(i.getName(),args[0])).map(OfflinePlayer::getUniqueId).findFirst();
                if(tmp.isPresent())
                {
                    UUID uuid=tmp.get();
                    Location loc=deathLocation.get(uuid);
                    if(loc==null)
                    {
                        sender.sendMessage("此玩家不存在上次死亡地点!");
                        return true;
                    }else p.teleport(loc);
                }
            }else{
                UUID uuid=p.getUniqueId();
                Location loc=deathLocation.get(uuid);
                if(loc==null)
                {
                    sender.sendMessage("此玩家不存在上次死亡地点!");
                    return true;
                }else p.teleport(loc);
            }
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name="";
        if(args.length>=1)
            name=args[0];
        String name2=name;
        if(command.getName().equalsIgnoreCase("back"))
            if(args.length<=1)
                return Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(i->i.startsWith(name2)).collect(Collectors.toList());
        return super.onTabComplete(sender, command, alias, args);
    }
}
