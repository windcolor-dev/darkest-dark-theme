package com.windpvp.darkestdark;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import java.util.Map;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DarkestDarkColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] ATTRIBUTES = {
            new AttributesDescriptor("Java package declaration names", DarkestDarkColors.PACKAGE_NAME),
            new AttributesDescriptor("Java import names", DarkestDarkColors.IMPORT_NAME),
            new AttributesDescriptor("Java class type arguments", DarkestDarkColors.TYPE_ARGUMENT)
    };

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(JavaLanguage.INSTANCE, null, null);
    }

    @Override
    public @NotNull String getDemoText() {
        return """
                package <packageName>com.windpvp.example</packageName>;

                import <importName>java.util.List</importName>;
                import <importName>java.util.Map</importName>;

                class Example<T> {
                    Map<<typeArgument>String</typeArgument>, List<<typeArgument>Integer</typeArgument>>> values;
                    List<T> parameters;
                }
                """;
    }

    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of("packageName", DarkestDarkColors.PACKAGE_NAME,
                "importName", DarkestDarkColors.IMPORT_NAME,
                "typeArgument", DarkestDarkColors.TYPE_ARGUMENT);
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return ATTRIBUTES.clone();
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Darkest Dark";
    }
}
