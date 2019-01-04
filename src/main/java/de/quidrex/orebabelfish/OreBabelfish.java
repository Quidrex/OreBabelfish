package de.quidrex.orebabelfish;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

@Mod(modid = OreBabelfish.MODID, name = OreBabelfish.NAME, version = OreBabelfish.VERSION,
        updateJSON = OreBabelfish.UPDATE_JSON)
public class OreBabelfish {
    public static final String MODID = "orebabelfish";
    public static final String NAME = "OreBabelfish";
    public static final String VERSION = "1.12.2-1.0";
    public static final String UPDATE_JSON =
            "https://raw.githubusercontent.com/quidrex/updateJSON/master/OreBabelfish.json";

    private File config;
    private Map<Integer, ItemStack> dictionary = new HashMap<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);
        config = new File(e.getModConfigurationDirectory(), MODID + ".cfg");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        try {
            if (!config.exists() && config.createNewFile()) {
                FileWriter writer = new FileWriter(config);
                writer.write("oreGold     minecraft:gold_ore     0\n");
                writer.write("oreIron     minecraft:iron_ore     0\n");
                writer.write("oreLapis    minecraft:lapis_ore    0\n");
                writer.write("oreDiamond  minecraft:diamond_ore  0\n");
                writer.write("oreRedstone minecraft:redstone_ore 0\n");
                writer.write("oreEmerald  minecraft:emerald_ore  0\n");
                writer.write("oreQuartz   minecraft:quartz_ore   0\n");
                writer.write("oreCoal     minecraft:coal_ore     0\n");
                writer.close();
            }

            try (Scanner scanner = new Scanner(config)) {
                while (scanner.hasNextLine()) {
                    String[] line = scanner.nextLine().split("\\s+");
                    dictionary.put(OreDictionary.getOreID(line[0]),
                            GameRegistry.makeItemStack(line[1], Integer.parseInt(line[2]), 1, null));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing OreBabelfish config", e);
        }
    }

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        ListIterator<ItemStack> drops = event.getDrops().listIterator();
        while (drops.hasNext()) {
            drops.set(translate(drops.next()));
        }
    }

    @SubscribeEvent
    public void onEntityItemPickupEvent(EntityItemPickupEvent event) {
        ItemStack src = event.getItem().getItem();
        ItemStack dst = translate(src);
        if (!dst.isItemEqual(src)) {
            event.setCanceled(true);

            EntityItem entity = event.getItem();
            entity.setItem(dst);
            entity.onCollideWithPlayer(event.getEntityPlayer());
        }
    }

    private ItemStack translate(ItemStack src) {
        for (int oreId : OreDictionary.getOreIDs(src)) {
            ItemStack dst = dictionary.get(oreId);
            if (dst != null) {
                return new ItemStack(dst.getItem(), dst.getCount(), dst.getMetadata());
            }
        }

        return src;
    }
}