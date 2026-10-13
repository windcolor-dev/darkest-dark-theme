package com.windpvp.darkestdark;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class DarkestDarkHighlightInfoFilter implements HighlightInfoFilter {
    private static final Set<String> TYPE_NAMES = Set.of(
            "CLASS_NAME_ATTRIBUTES", "INTERFACE_NAME_ATTRIBUTES", "ABSTRACT_CLASS_NAME_ATTRIBUTES",
            "ENUM_NAME_ATTRIBUTES", "RECORD_NAME_ATTRIBUTES", "ANNOTATION_NAME_ATTRIBUTES");
    private static final Set<String> IMPORT_MEMBERS = Set.of(
            "STATIC_FIELD_ATTRIBUTES", "STATIC_FINAL_FIELD_ATTRIBUTES", "STATIC_METHOD_ATTRIBUTES",
            "STATIC_FIELD_IMPORTED_ATTRIBUTES", "STATIC_FINAL_FIELD_IMPORTED_ATTRIBUTES",
            "STATIC_METHOD_IMPORTED_ATTRIBUTES");

    @Override
    public boolean accept(@NotNull HighlightInfo info, PsiFile file) {
        if (file == null || !file.getLanguage().isKindOf(JavaLanguage.INSTANCE)
                || !HighlightInfoType.SYMBOL_TYPE_SEVERITY.equals(info.getSeverity())) {
            return true;
        }
        TextAttributesKey key = info.type.getAttributesKey();
        if (key == null) {
            return true;
        }
        String name = key.getExternalName();
        if ((!TYPE_NAMES.contains(name) && !IMPORT_MEMBERS.contains(name)) || !DarkestDarkColors.isEnabled()) {
            return true;
        }
        PsiElement element = file.findElementAt(info.getStartOffset());
        JavaHighlightContext context = JavaHighlightContext.of(element);
        if (context == JavaHighlightContext.IMPORT || context == JavaHighlightContext.PACKAGE_DECLARATION) {
            return false;
        }
        return context != JavaHighlightContext.TYPE_ARGUMENT || !TYPE_NAMES.contains(name)
                || !new TextRange(info.getStartOffset(), info.getEndOffset()).equals(element.getTextRange());
    }
}
