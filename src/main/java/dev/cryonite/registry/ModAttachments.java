package dev.cryonite.registry;

import dev.cryonite.Cryonite;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.attachment.AttachmentType;

/** Player-local state for the Void armor rescue; stored as attachments, never stack NBT. */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Cryonite.MOD_ID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BlockPos>> LAST_SAFE_POSITION = TYPES.register("last_safe_position", () -> AttachmentType.builder(() -> BlockPos.ZERO).serialize(BlockPos.CODEC).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> VOID_RESCUE_COOLDOWN = TYPES.register("void_rescue_cooldown", () -> AttachmentType.builder(() -> 0).serialize(com.mojang.serialization.Codec.INT).copyOnDeath().build());
    private ModAttachments() {}
}
