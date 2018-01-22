package net.qbar.common.init;

import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.Fluid;
import net.qbar.common.block.*;
import net.qbar.common.block.creative.BlockCreativeSteamGenerator;
import net.qbar.common.block.creative.BlockCreativeWaterGenerator;
import net.qbar.common.block.BlockStructure;
import net.qbar.common.multiblock.TileMultiblockGag;
import net.qbar.common.tile.TileStructure;
import net.qbar.common.tile.creative.TileCreativeSteamGenerator;
import net.qbar.common.tile.creative.TileCreativeWaterGenerator;
import net.qbar.common.tile.machine.*;

public class MachineBlocks
{
    public static void init()
    {
        QBarBlocks.registerBlock(new BlockKeypunch());
        QBarBlocks.registerBlock(new BlockTank("fluidtank_small", Fluid.BUCKET_VOLUME * 48, 0));
        QBarBlocks.registerBlock(new BlockTank("fluidtank_medium", Fluid.BUCKET_VOLUME * 128, 1));
        QBarBlocks.registerBlock(new BlockTank("fluidtank_big", Fluid.BUCKET_VOLUME * 432, 2));
        QBarBlocks.registerBlock(new BlockSolidBoiler());
        QBarBlocks.registerBlock(
                new BlockMultiblockMachine<>("assembler", Material.IRON, TileAssembler::new, TileAssembler.class));
        QBarBlocks.registerBlock(new BlockCreativeSteamGenerator());
        QBarBlocks.registerBlock(new BlockCreativeWaterGenerator());
        QBarBlocks.registerBlock(new BlockStructure());
        QBarBlocks.registerBlock(new BlockMultiblockMachine<>("steamfurnacemk1", Material.IRON, TileSteamFurnace::new,
                TileSteamFurnace.class));
        QBarBlocks.registerBlock(new BlockSolarBoiler());
        QBarBlocks.registerBlock(new BlockSolarMirror());
        QBarBlocks.registerBlock(new BlockMultiblockMachine<>("rollingmill", Material.IRON, TileRollingMill::new,
                TileRollingMill.class));
        QBarBlocks.registerBlock(new BlockLiquidBoiler());
        QBarBlocks.registerBlock(new BlockMultiblockMachine<>("steamfurnacemk2", Material.IRON,
                TileSteamFurnaceMK2::new, TileSteamFurnaceMK2.class));
        QBarBlocks.registerBlock(
                new BlockMultiblockMachine<>("orewasher", Material.IRON, TileOreWasher::new, TileOreWasher.class));
        QBarBlocks.registerBlock(new BlockMultiblockMachine<>("sortingmachine", Material.IRON, TileSortingMachine::new,
                TileSortingMachine.class));
        QBarBlocks.registerBlock(new BlockMultiblockMachine<>("smallminingdrill", Material.IRON,
                TileSmallMiningDrill::new, TileSmallMiningDrill.class));
        QBarBlocks.registerBlock(new BlockMultiblockMachine<>("tinyminingdrill", Material.IRON,
                TileTinyMiningDrill::new, TileTinyMiningDrill.class));
        QBarBlocks.registerBlock(new BlockMultiblockMachine<>("alloycauldron", Material.IRON, TileAlloyCauldron::new,
                TileAlloyCauldron.class));
        QBarBlocks.registerBlock(new BlockMultiblockMachine<>("sawmill", Material.IRON, TileSawMill::new,
                TileSawMill.class));

        QBarBlocks.registerBlock(new BlockEngineerStorage());
        QBarBlocks.registerBlock(new BlockEngineerWorkbench());
        QBarBlocks.registerBlock(new BlockCraftCardLibrary());
        QBarBlocks.registerBlock(new BlockBlueprintPrinter());

        QBarBlocks.registerTile(TileTank.class, "tank");
        QBarBlocks.registerTile(TileKeypunch.class, "keypunch");
        QBarBlocks.registerTile(TileSolidBoiler.class, "boiler");
        QBarBlocks.registerTile(TileAssembler.class, "assembler");
        QBarBlocks.registerTile(TileMultiblockGag.class, "multiblockgag");
        QBarBlocks.registerTile(TileCreativeSteamGenerator.class, "creative_steam_generator");
        QBarBlocks.registerTile(TileCreativeWaterGenerator.class, "creative_water_generator");
        QBarBlocks.registerTile(TileStructure.class, "structure");
        QBarBlocks.registerTile(TileSteamFurnace.class, "steamfurnace");
        QBarBlocks.registerTile(TileSolarBoiler.class, "solarboiler");
        QBarBlocks.registerTile(TileSolarMirror.class, "solarmirror");
        QBarBlocks.registerTile(TileRollingMill.class, "rollingmill");
        QBarBlocks.registerTile(TileLiquidBoiler.class, "liquidfuelboiler");
        QBarBlocks.registerTile(TileSteamFurnaceMK2.class, "steamfurnacemk2");
        QBarBlocks.registerTile(TileOreWasher.class, "orewasher");
        QBarBlocks.registerTile(TileSortingMachine.class, "sortingmachine");
        QBarBlocks.registerTile(TileSmallMiningDrill.class, "smallminingdrill");
        QBarBlocks.registerTile(TileTinyMiningDrill.class, "tinyminingdrill");
        QBarBlocks.registerTile(TileAlloyCauldron.class, "alloycauldron");
        QBarBlocks.registerTile(TileSawMill.class, "sawmill");
        QBarBlocks.registerTile(TileEngineerStorage.class, "engineerstorage");
        QBarBlocks.registerTile(TileEngineerWorkbench.class, "engineerworkbench");
        QBarBlocks.registerTile(TileBlueprintPrinter.class, "blueprintprinter");
        QBarBlocks.registerTile(TileCraftCardLibrary.class, "craftcardlibrary");
    }
}
