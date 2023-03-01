package io.mdt.ait.common.tiles;

import com.mdt.ait.AIT;
import com.mdt.ait.common.blocks.BasicInteriorDoorBlock;
import com.mdt.ait.core.init.AITSounds;
import com.mdt.ait.core.init.AITTiles;
import io.mdt.ait.tardis.door.TARDISDoorState;
import io.mdt.ait.tardis.door.TARDISDoorStates;
import io.mdt.ait.tardis.link.ITARDISLinkable;
import io.mdt.ait.tardis.link.TARDISLink;
import io.mdt.ait.util.TARDISUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;

public class TARDISInteriorDoorTile extends TileEntity implements ITickableTileEntity, ITARDISLinkable {

    private float rightDoorRotation = 0;
    private float leftDoorRotation = 0;

    public TARDISInteriorDoorTile() {
        super(AITTiles.BASIC_INTERIOR_DOOR_TILE_ENTITY_TYPE.get());
    }

    public float getLeftDoorRotation() {
        return this.leftDoorRotation;
    }

    public float getRightDoorRotation() {
        return this.rightDoorRotation;
    }

    public TARDISDoorState getStateManger() {
        return this.getDoor().getState();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (this.getLevel() != null && !this.getLevel().isClientSide) {
            ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
            ForgeChunkManager.forceChunk((ServerWorld) this.getLevel(), AIT.MOD_ID, this.getBlockPos(), chunkPos.x, chunkPos.z, false, false);
        }

        this.syncToClient();
    }

    TARDISDoorStates previousState = TARDISDoorStates.CLOSED;

    @Override
    public void tick() {
        // remove portal if entity removed, probably can implement that in setRemoved()
        // ...


        if (this.getStateManger() != null) {
            TARDISDoorStates state = this.getStateManger().getState();
            if (state != this.previousState) {
                this.rightDoorRotation = state == TARDISDoorStates.FIRST ? 0.0f : 87.5f;
                this.leftDoorRotation = state == TARDISDoorStates.FIRST ? 0.0f : (state == TARDISDoorStates.BOTH ? 0.0f : 87.5f);
            }
            if (state != TARDISDoorStates.CLOSED) {
                if (this.rightDoorRotation < 87.5f) {
                    this.rightDoorRotation += 5.0f;
                } else {
                    this.rightDoorRotation = 87.5f;
                }
            } else {
                if (this.leftDoorRotation > 0.0f && this.rightDoorRotation > 0.0f) {
                    this.leftDoorRotation -= 15.0f;
                    this.rightDoorRotation -= 15.0f;
                }
            }
            if (state == TARDISDoorStates.BOTH) {
                if (this.leftDoorRotation < 87.5f) {
                    this.leftDoorRotation += 5.0f;
                } else {
                    this.leftDoorRotation = 87.5f;
                }
            }
            if(state == TARDISDoorStates.CLOSED) {
                if(this.leftDoorRotation == -2.5f) {
                    this.leftDoorRotation = 0.0f;
                }
                if(this.rightDoorRotation == -2.5f) {
                    this.rightDoorRotation = 0.0f;
                }
            }

            this.previousState = state;
        }
    }

    public void useOn(World world, PlayerEntity player, BlockPos pos) {
        if (!world.isClientSide) {
            TARDISDoorStates state = this.getStateManger().getState();

            if (!this.getStateManger().isLocked()) {
                state = this.getStateManger().next();

                //TARDISUtil.getExteriorTile(this.getTARDIS()).syncToClient();
            } else {
                player.displayClientMessage(new TranslationTextComponent(
                        "Door is locked!").setStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
            }

            world.playSound(null, pos, state.getSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.syncToClient();
        }
    }

    public void onKey(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();

        if (TARDISUtil.getExteriorTile(this.getTARDIS()) == null) {
            player.displayClientMessage(new TranslationTextComponent(
                    "TARDIS is in flight!").setStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
            return;
        }

        if (!this.getTARDIS().ownsKey(context.getItemInHand())) {
            player.displayClientMessage(new TranslationTextComponent(
                    "This TARDIS is not yours!").setStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
            return;
        }

        this.getStateManger().setLocked(player.isCrouching());
        this.level.playSound(null, this.getDoor().getDoorPosition(), AITSounds.TARDIS_LOCK.get(), SoundCategory.MASTER, 1.0F, 1.0F);

        player.displayClientMessage(new TranslationTextComponent(
                this.getStateManger().isLocked() ? "Door is locked!" : "Door is unlocked!").setStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);

        this.syncToClient();
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        this.leftDoorRotation = nbt.getFloat("left");
        this.rightDoorRotation = nbt.getFloat("right");

        super.load(state, nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putFloat("left", this.leftDoorRotation);
        nbt.putFloat("right", this.rightDoorRotation);

        return super.save(nbt);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getBlockPos()).inflate(10, 10, 10);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.getBlockPos(), 0, this.save(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.load(this.getBlockState(), packet.getTag());
    }

    public void syncToClient() {
        if (this.level != null) {
            this.level.setBlocksDirty(this.worldPosition, this.level.getBlockState(this.worldPosition), this.level.getBlockState(this.worldPosition));
            this.level.sendBlockUpdated(this.worldPosition, this.level.getBlockState(this.worldPosition), this.level.getBlockState(this.worldPosition), 3);
            this.setChanged();
        }
    }

    public Direction getFacing() {
        return this.getBlockState().getValue(BasicInteriorDoorBlock.FACING);
    }

    private final TARDISLink link = new TARDISLink();

    @Override
    public TARDISLink getLink() {
        return this.link;
    }
}
