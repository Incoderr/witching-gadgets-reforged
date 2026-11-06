package witchinggadgets.client.travellersgear;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.gear.GearMenu;

public final class TgLayout {
    public static final int SLOT_ELEMENTS = GearMenu.HOTBAR_END;
    public static final String[] TEXTURES = {"book", "digital", "epic"};
    private static final Logger LOGGER = LoggerFactory.getLogger(TgLayout.class);
    private static final Identifier SOURCE = WitchingGadgets.id("travellersgear/layouts.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final class Element {
        public final String name;
        public final int width;
        public final int height;
        public int x;
        public int y;
        public boolean hidden;

        Element(String name, int width, int height, int x, int y, boolean hidden) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
            this.hidden = hidden;
        }

        Element copy() {
            return new Element(name, width, height, x, y, hidden);
        }
    }

    public record Preset(String texture, List<Element> elements) {
    }

    private static List<Element> elements = new ArrayList<>();
    private static final Map<String, Preset> PRESETS = new LinkedHashMap<>();
    private static String texture = TEXTURES[0];
    private static boolean loaded;

    private TgLayout() {
    }

    public static void install() {
        GearMenu.layout = new GearMenu.Layout() {
            @Override
            public int x(int slot) {
                return element(slot).x + 1;
            }

            @Override
            public int y(int slot) {
                return element(slot).y + 1;
            }
        };
    }

    public static List<Element> elements() {
        load();
        return elements;
    }

    public static Element element(int index) {
        return elements().get(index);
    }

    public static Element element(String name) {
        for (Element element : elements()) {
            if (element.name.equals(name)) {
                return element;
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static Map<String, Preset> presets() {
        load();
        return PRESETS;
    }

    public static String texture() {
        load();
        return texture;
    }

    public static Identifier textureId() {
        return WitchingGadgets.id("textures/gui/travellersgear/inventory_" + texture() + ".png");
    }

    public static void setTexture(String value) {
        texture = value;
    }

    public static long hiddenMask() {
        long mask = 0L;
        List<Element> list = elements();
        for (int index = 0; index < SLOT_ELEMENTS && index < list.size(); index++) {
            if (list.get(index).hidden) {
                mask |= GearMenu.hiddenBit(index);
            }
        }
        return mask;
    }

    public static void apply(Preset preset) {
        texture = preset.texture();
        elements = copy(preset.elements());
        save();
    }

    private static List<Element> copy(List<Element> source) {
        List<Element> copy = new ArrayList<>();
        for (Element element : source) {
            copy.add(element.copy());
        }
        return copy;
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve(WitchingGadgets.MODID + "-travellersgear-inventory.json");
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        List<Element> defaults = List.of();
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(SOURCE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject base = root.getAsJsonObject("default");
            defaults = read(base.getAsJsonArray("elements"));
            texture = base.get("texture").getAsString();
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("presets").entrySet()) {
                JsonObject preset = entry.getValue().getAsJsonObject();
                PRESETS.put(entry.getKey(), new Preset(preset.get("texture").getAsString(), read(preset.getAsJsonArray("elements"))));
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Failed to read Travellers' Gear layouts", exception);
        }
        elements = copy(defaults);
        Path file = file();
        if (!Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject saved = JsonParser.parseReader(reader).getAsJsonObject();
            if (saved.has("texture")) {
                texture = saved.get("texture").getAsString();
            }
            JsonObject positions = saved.getAsJsonObject("elements");
            for (Element element : elements) {
                if (positions != null && positions.has(element.name)) {
                    JsonObject value = positions.getAsJsonObject(element.name);
                    element.x = value.get("x").getAsInt();
                    element.y = value.get("y").getAsInt();
                    element.hidden = value.get("hidden").getAsBoolean();
                }
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Failed to read {}", file, exception);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("texture", texture);
        JsonObject positions = new JsonObject();
        for (Element element : elements()) {
            JsonObject value = new JsonObject();
            value.addProperty("x", element.x);
            value.addProperty("y", element.y);
            value.addProperty("hidden", element.hidden);
            positions.add(element.name, value);
        }
        root.add("elements", positions);
        try {
            Files.writeString(file(), GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            LOGGER.error("Failed to save {}", file(), exception);
        }
    }

    private static List<Element> read(JsonArray array) {
        List<Element> list = new ArrayList<>();
        for (JsonElement value : array) {
            JsonObject object = value.getAsJsonObject();
            JsonArray size = object.getAsJsonArray("size");
            JsonArray pos = object.getAsJsonArray("pos");
            list.add(new Element(object.get("name").getAsString(), size.get(0).getAsInt(), size.get(1).getAsInt(), pos.get(0).getAsInt(), pos.get(1).getAsInt(), object.get("hidden").getAsBoolean()));
        }
        return list;
    }
}
