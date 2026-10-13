package com.windpvp.darkestdark;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiPackageStatement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiReferenceParameterList;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiTypeParameterListOwner;

enum JavaHighlightContext {
    PACKAGE_DECLARATION(DarkestDarkColors.PACKAGE_NAME),
    IMPORT(DarkestDarkColors.IMPORT_NAME),
    TYPE_ARGUMENT(DarkestDarkColors.TYPE_ARGUMENT);

    final TextAttributesKey attributes;

    JavaHighlightContext(TextAttributesKey attributes) {
        this.attributes = attributes;
    }

    static JavaHighlightContext of(PsiElement element) {
        if (!(element instanceof PsiIdentifier)
                || !(element.getParent() instanceof PsiJavaCodeReferenceElement reference)
                || reference instanceof PsiReferenceExpression) {
            return null;
        }

        PsiElement parent = reference.getParent();
        while (parent instanceof PsiJavaCodeReferenceElement || parent instanceof PsiTypeElement) {
            parent = parent.getParent();
        }
        if (parent instanceof PsiPackageStatement) {
            return PACKAGE_DECLARATION;
        }
        if (parent instanceof PsiImportStatementBase) {
            return IMPORT;
        }
        if (parent instanceof PsiReferenceParameterList && !isTypeParameter(reference)) {
            return TYPE_ARGUMENT;
        }
        return null;
    }

    private static boolean isTypeParameter(PsiJavaCodeReferenceElement reference) {
        if (reference.getQualifier() != null) {
            return false;
        }
        String name = reference.getReferenceName();
        for (PsiElement scope = reference.getParent(); scope != null && !(scope instanceof PsiFile);
             scope = scope.getParent()) {
            if (scope instanceof PsiTypeParameterListOwner owner) {
                for (PsiTypeParameter parameter : owner.getTypeParameters()) {
                    if (name != null && name.equals(parameter.getName())) {
                        return true;
                    }
                }
            }
            if (scope instanceof PsiModifierListOwner owner) {
                PsiModifierList modifiers = owner.getModifierList();
                if (modifiers != null && modifiers.hasExplicitModifier(PsiModifier.STATIC)) {
                    break;
                }
            }
            if (scope instanceof PsiClass type
                    && (type.isInterface() || type.isEnum() || type.isRecord()
                    || type.getParent() instanceof PsiClass containingType && containingType.isInterface())) {
                break;
            }
        }
        return false;
    }
}
