package com.mdt.ait.tardis.special;

import com.mdt.ait.AIT;
import com.mdt.ait.common.blocks.TardisBlock;
import com.mdt.ait.common.tileentities.TardisTileEntity;
import com.mdt.ait.core.init.enums.EnumDoorState;
import com.mdt.ait.core.init.enums.EnumMatState;
import com.mdt.ait.tardis.Tardis;
import com.mdt.ait.tardis.TardisConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DematTransit {

    public UUID tardisID;

    public BlockPos leverPos;

    public BlockPos landingPosition;

    public BlockState newBlockState;

    public boolean readyForDemat = false;

    public DematTransit(UUID tardisID) {
        this.tardisID = tardisID;
    }

    public int getFlightTicks() {
        Tardis tardis = AIT.tardisManager.getTardis(tardisID);
        // 20 seconds
        return 400; // @TODO: LOQOR MAKE A FORMULA FOR THIS
    }

    public void finishedDematAnimation() {
        if (this.tardisID != null) {
            Tardis tardis = AIT.tardisManager.getTardis(tardisID);
            ServerWorld oldDimension = AIT.server.getLevel(tardis.exterior_dimension);
            ServerWorld newDimension = AIT.server.getLevel(tardis.target_dimension);
            assert oldDimension != null;
            assert newDimension != null;
            if (!TardisConfig.cantLandOnBlockList.contains(newDimension.getBlockState(tardis.targetPosition.below(1)).getBlock())) { // Checks if the block below the tardis's target position is a valid block to land on
                if (newDimension.getBlockState(tardis.targetPosition.above(1)).getBlock().equals(Blocks.AIR)) {
                    landTardisPart1(tardis.targetPosition);
                }
            }
        }
    }

    public void landTardisPart1(BlockPos landing_position) {
        this.landingPosition = landing_position;
        Tardis tardis = AIT.tardisManager.getTardis(tardisID);
        ServerWorld oldDimension = AIT.server.getLevel(tardis.exterior_dimension);
        ServerWorld newDimension = AIT.server.getLevel(tardis.target_dimension);
        assert oldDimension != null;
        BlockState oldBlockState = oldDimension.getBlockState(tardis.exterior_position);
        TardisTileEntity oldTardisTileEntity = (TardisTileEntity) oldDimension.getBlockEntity(tardis.exterior_position);
        assert oldTardisTileEntity != null;
        this.newBlockState = oldBlockState.setValue(TardisBlock.isExistingTardis, true).setValue(TardisBlock.FACING, tardis.target_facing_direction);
        assert newDimension != null;
        ForgeChunkManager.forceChunk(newDimension, AIT.MOD_ID, landing_position, 0, 0, true, true);
        oldDimension.removeBlock(tardis.exterior_position, false);
        readyForDemat = true;
        // pass or something idk
    }

    public void landTardisPart2() {
        Tardis tardis = AIT.tardisManager.getTardis(tardisID);
        ServerWorld newDimension = AIT.server.getLevel(tardis.target_dimension);
        assert newDimension != null;
        newDimension.setBlockAndUpdate(landingPosition, newBlockState);
        TardisTileEntity newTardisTileEntity = (TardisTileEntity) newDimension.getBlockEntity(landingPosition);
        assert newTardisTileEntity != null;
        newTardisTileEntity.setExterior(tardis.exteriorType);
        newTardisTileEntity.linked_tardis_id = tardis.tardisID;
        newTardisTileEntity.setDoorState(EnumDoorState.CLOSED);
        newTardisTileEntity.linked_tardis = tardis;
        newTardisTileEntity.setMatState(EnumMatState.REMAT);
        newDimension.setBlockEntity(landingPosition, newTardisTileEntity);
        tardis.targetPosition = landingPosition;
        tardis.__moveExterior(tardis.targetPosition, tardis.target_facing_direction, tardis.target_dimension);
    }
}