package dev.nightmarehuy.cryonite.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Клиентское состояние Пространственного Терминала: список измерений, полученный с сервера,
 * и текущая выбранная карточка карусели.
 */
public final class DimensionTerminalClient {

    public record DimensionEntry(ResourceLocation key, String name) {}

    /** Все известные измерения, в порядке, в котором прислал сервер. */
    public static final List<DimensionEntry> dimensions = new ArrayList<>();

    /** Индекс текущего измерения игрока (те, в котором он сейчас находится), или -1. */
    public static int currentDimensionIndex = -1;

    /** Заменить весь список измерений ответом сервера. */
    public static void setDimensions(List<DimensionEntry> entries) {
        dimensions.clear();
        dimensions.addAll(entries);
    }

    public static DimensionEntry get(int index) {
        return dimensions.get(index);
    }

    public static int count() {
        return dimensions.size();
    }

    public static Component formattedName(String raw) {
        // Уже приходит от сервера в человечном виде; заглушка на случай пустого значения.
        return Component.literal(raw == null || raw.isEmpty() ? "Unknown dimension" : raw);
    }

    private DimensionTerminalClient() {}
}
