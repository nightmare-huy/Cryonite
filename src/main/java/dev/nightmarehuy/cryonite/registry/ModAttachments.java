package dev.nightmarehuy.cryonite.registry;

import com.mojang.serialization.Codec;
import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.attachment.VoidRescueData;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Cryonite.MODID);

    /** Last safe position + rescue cooldown of the Void armor (attached to players). */
    public static final Supplier<AttachmentType<VoidRescueData>> VOID_RESCUE = REGISTER.register("void_rescue",
            () -> AttachmentType.builder(() -> VoidRescueData.EMPTY)
                    .serialize(VoidRescueData.CODEC)
                    .copyOnDeath()
                    .build());

    /** Ticks a Molten Void Blank item entity has already spent on Crying Obsidian. */
    public static final Supplier<AttachmentType<Integer>> QUENCH_PROGRESS = REGISTER.register("quench_progress",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    private ModAttachments() {}
}
