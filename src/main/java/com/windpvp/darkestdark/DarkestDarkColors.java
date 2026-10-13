package com.windpvp.darkestdark;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;

public final class DarkestDarkColors {
    public static final TextAttributesKey PACKAGE_NAME = TextAttributesKey.createTextAttributesKey(
            "DARKEST_DARK_PACKAGE_NAME", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey IMPORT_NAME = TextAttributesKey.createTextAttributesKey(
            "DARKEST_DARK_IMPORT_NAME", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey TYPE_ARGUMENT = TextAttributesKey.createTextAttributesKey(
            "DARKEST_DARK_TYPE_ARGUMENT", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

    private DarkestDarkColors() {
    }

    static boolean isEnabled() {
        return isEnabled(EditorColorsManager.getInstance().getGlobalScheme());
    }

    static boolean isEnabled(EditorColorsScheme scheme) {
        var properties = scheme.getMetaProperties();
        return "true".equals(properties.getProperty("darkestDarkHighlighting"))
                || "Darkest Dark".equals(properties.getProperty("originalScheme"))
                || "local.darkest-dark-theme".equals(properties.getProperty("pluginId"));
    }
}
