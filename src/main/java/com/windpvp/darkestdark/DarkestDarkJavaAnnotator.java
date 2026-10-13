package com.windpvp.darkestdark;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import org.jetbrains.annotations.NotNull;

public final class DarkestDarkJavaAnnotator implements Annotator, DumbAware {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiIdentifier) || !DarkestDarkColors.isEnabled()) {
            return;
        }
        JavaHighlightContext context = JavaHighlightContext.of(element);
        if (context != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .textAttributes(context.attributes)
                    .create();
        }
    }
}
