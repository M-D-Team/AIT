package com.mdt.ait.core.init;

import com.mdt.ait.AIT;
import com.mdt.ait.common.tileentities.BasicInteriorDoorTile;
import com.mdt.ait.common.tileentities.TSVTile;
import com.mdt.ait.common.tileentities.TardisTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class AITTiles {

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, AIT.MOD_ID);

    public static final RegistryObject<TileEntityType<TardisTileEntity>> TARDIS_TILE_ENTITY_TYPE = TILE_ENTITIES.register(
            "tardis",
            () -> TileEntityType.Builder.of(TardisTileEntity::new, AITBlocks.TARDIS_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TSVTile>> TSV_TILE_ENTITY_TYPE = TILE_ENTITIES.register(
            "tsv",
            () -> TileEntityType.Builder.of(TSVTile::new, AITBlocks.TSV_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<BasicInteriorDoorTile>> BASIC_INTERIOR_DOOR_TILE_ENTITY_TYPE = TILE_ENTITIES.register(
            "interior_door",
            () -> TileEntityType.Builder.of(BasicInteriorDoorTile::new, AITBlocks.INTERIOR_DOOR_BLOCK.get()).build(null));
}
