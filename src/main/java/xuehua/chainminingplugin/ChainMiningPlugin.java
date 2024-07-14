package xuehua.chainminingplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChainMiningPlugin extends JavaPlugin implements Listener {

    private final Map<Player, Boolean> chainMiningEnabled = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("lswk").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                boolean enabled = chainMiningEnabled.getOrDefault(player, false);
                chainMiningEnabled.put(player, !enabled);
                player.sendMessage(ChatColor.GREEN + "连锁挖矿模式 " + (enabled ? "已关闭" : "已开启"));
            } else {
                sender.sendMessage(ChatColor.RED + "没有权限使用此命令！");
            }
            return true;
        });
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!chainMiningEnabled.getOrDefault(player, false)) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!isValidTool(itemInHand)) {
            chainMiningEnabled.put(player, false);
            player.sendMessage(ChatColor.RED + "你必须手持镐子、铲子或斧子才能进行连锁挖矿！ 连锁挖矿模式已关闭");
            return;
        }

        Block block = event.getBlock();
        Material material = block.getType();

        Set<Block> countedBlocks = new HashSet<>();
        countBlocks(block, material, countedBlocks, 0);

        player.sendMessage(ChatColor.YELLOW + "连锁挖矿完成，您采集了 " + countedBlocks.size() + " 方块");

        // 挖掘相同类型的所有方块
        for (Block b : countedBlocks) {
            b.breakNaturally(itemInHand);
        }

        // 扣除耐久度
        Damageable toolMeta = (Damageable) itemInHand.getItemMeta();
        toolMeta.setDamage(toolMeta.getDamage() + countedBlocks.size());
        itemInHand.setItemMeta(toolMeta);

        if (toolMeta.getDamage() >= itemInHand.getType().getMaxDurability()) {
            chainMiningEnabled.put(player, false);
            player.sendMessage(ChatColor.RED + "你的工具已经损坏，连锁挖矿模式已关闭");
        }
    }

    private void countBlocks(Block block, Material material, Set<Block> countedBlocks, int depth) {
        if (block.getType() != material || countedBlocks.contains(block) || depth >= 7) {
            return;
        }

        countedBlocks.add(block);

        for (Block adjacent : getAdjacentBlocks(block)) {
            countBlocks(adjacent, material, countedBlocks, depth + 1);
        }
    }

    private Set<Block> getAdjacentBlocks(Block block) {
        Set<Block> adjacentBlocks = new HashSet<>();
        adjacentBlocks.add(block.getRelative(1, 0, 0));
        adjacentBlocks.add(block.getRelative(-1, 0, 0));
        adjacentBlocks.add(block.getRelative(0, 1, 0));
        adjacentBlocks.add(block.getRelative(0, -1, 0));
        adjacentBlocks.add(block.getRelative(0, 0, 1));
        adjacentBlocks.add(block.getRelative(0, 0, -1));
        return adjacentBlocks;
    }

    private boolean isValidTool(ItemStack item) {
        return item.getType() == Material.WOODEN_PICKAXE ||
                item.getType() == Material.STONE_PICKAXE ||
                item.getType() == Material.IRON_PICKAXE ||
                item.getType() == Material.GOLDEN_PICKAXE ||
                item.getType() == Material.DIAMOND_PICKAXE ||
                item.getType() == Material.NETHERITE_PICKAXE ||
                item.getType() == Material.WOODEN_SHOVEL ||
                item.getType() == Material.STONE_SHOVEL ||
                item.getType() == Material.IRON_SHOVEL ||
                item.getType() == Material.GOLDEN_SHOVEL ||
                item.getType() == Material.DIAMOND_SHOVEL ||
                item.getType() == Material.NETHERITE_SHOVEL ||
                item.getType() == Material.WOODEN_AXE ||
                item.getType() == Material.STONE_AXE ||
                item.getType() == Material.IRON_AXE ||
                item.getType() == Material.GOLDEN_AXE ||
                item.getType() == Material.DIAMOND_AXE ||
                item.getType() == Material.NETHERITE_AXE;
    }
}
