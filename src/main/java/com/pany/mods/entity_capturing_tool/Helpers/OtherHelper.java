package com.pany.mods.entity_capturing_tool.Helpers;

import com.pany.mods.entity_capturing_tool.EntityCapturingTool;
import com.pany.mods.entity_capturing_tool.blocks.endercageblock.EnderCageDiamondEntity;
import com.pany.mods.entity_capturing_tool.blocks.endercageblock.EnderCageEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.pany.mods.entity_capturing_tool.Helpers.MathStuff.GetDistance;

public class OtherHelper {

    public static boolean isEndercage(Item itemInHand){

        return itemInHand.equals(EntityCapturingTool.EnderCageBlock.asItem()) || itemInHand.equals(EntityCapturingTool.EnderCageDiamondBlock.asItem());
    }

    public static Item getEnderCageItemOfEntity(EnderCageEntity enderCageEntity){
        return enderCageEntity instanceof EnderCageDiamondEntity ? EntityCapturingTool.EnderCageDiamondItem : EntityCapturingTool.EnderCageItem;
    }
}
