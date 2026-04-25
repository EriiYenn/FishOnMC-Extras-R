package dannypx.foe.config;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.type.keybind.KeyBindMode;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup;
import me.fzzyhmstrs.fzzy_config.screen.context.ContextInput;
import me.fzzyhmstrs.fzzy_config.util.Translatable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedKeybind;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Version(version = 4)
@Translatable.Name("Controls")
public class KeyBindConfig extends Config {
    public KeyBindConfig() {
        super(Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "keybinding_config"));
    }

    @Name("Open FOER Menu")
    public ValidatedKeybind openMainKeybind = new ValidatedKeybind(GLFW.GLFW_KEY_O, ContextInput.KEYBOARD, false, false, false);

    @Name("Inspect")
    public ConfigGroup inspectGroup = new ConfigGroup("inspect_group");

    @Name("Inspect Key")
    public ValidatedKeybind inspectKeybind = new ValidatedKeybind(GLFW.GLFW_KEY_LEFT_SHIFT, ContextInput.KEYBOARD, false, false, false);

    @ConfigGroup.Pop
    @Name("Button mode")
    public ValidatedEnum<KeyBindMode> inspectMode = new ValidatedEnum<>(KeyBindMode.HOLD, ValidatedEnum.WidgetType.CYCLING);

    @Override
    public @NotNull FileType fileType() {
        return FileType.JSON;
    }
}
