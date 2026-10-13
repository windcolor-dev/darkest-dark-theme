package com.windpvp.darkestdark;

import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.core.JavaCoreApplicationEnvironment;
import com.intellij.core.JavaCoreProjectEnvironment;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.colors.impl.EditorColorsSchemeImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.util.PsiTreeUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JavaHighlightingTest {
    private static Disposable disposable;
    private static Project project;
    private static TestColorsManager colors;

    @BeforeClass
    public static void setUp() throws Exception {
        Path bin = Files.createDirectories(Path.of(System.getProperty("idea.home.path"), "bin"));
        Files.writeString(bin.resolve("idea.properties"), "");
        disposable = Disposer.newDisposable();
        JavaCoreApplicationEnvironment application = new JavaCoreApplicationEnvironment(disposable);
        colors = new TestColorsManager();
        application.registerApplicationService(EditorColorsManager.class, colors);
        project = new JavaCoreProjectEnvironment(disposable, application).getProject();
    }

    @AfterClass
    public static void tearDown() {
        Disposer.dispose(disposable);
    }

    @Before
    public void selectTheme() {
        colors.scheme = new EditorColorsSchemeImpl(null);
        colors.scheme.getMetaProperties().setProperty("darkestDarkHighlighting", "true");
    }

    @Test
    public void packageAndImports() {
        PsiFile file = file("""
                package com.windpvp;
                import java.util.List;
                import java.util.*;
                import static java.lang.Math.PI;
                class Example {}
                """);

        assertEquals(List.of("com", "windpvp"), text(file, DarkestDarkColors.PACKAGE_NAME));
        assertEquals(List.of("java", "util", "List", "java", "util", "java", "lang", "Math", "PI"),
                text(file, DarkestDarkColors.IMPORT_NAME));
    }

    @Test
    public void typeArguments() {
        PsiFile file = file("""
                class Example<T> {
                    Map<String, List<? extends Number[]>> values;
                    List<T> parameters;
                    String plain;
                    <U> Map<T, U> method() { return null; }
                }
                """);

        assertEquals(List.of("String", "List", "Number"), text(file, DarkestDarkColors.TYPE_ARGUMENT));
    }

    @Test
    public void otherSchemes() {
        colors.scheme = new EditorColorsSchemeImpl(null);
        PsiFile file = file("class Example { List<String> values; }");

        assertTrue(annotations(file).isEmpty());
        assertTrue(new DarkestDarkHighlightInfoFilter().accept(symbol(file, "String", "CLASS_NAME_ATTRIBUTES"), file));
    }

    @Test
    public void schemeCopies() {
        colors.scheme.getMetaProperties().remove("darkestDarkHighlighting");
        colors.scheme.getMetaProperties().setProperty("originalScheme", "Darkest Dark");
        PsiFile file = file("class Example { List<String> values; }");

        assertEquals(List.of("String"), text(file, DarkestDarkColors.TYPE_ARGUMENT));
        colors.scheme.getMetaProperties().setProperty("originalScheme", "My copy");
        colors.scheme.getMetaProperties().setProperty("pluginId", "local.darkest-dark-theme");
        assertEquals(List.of("String"), text(file, DarkestDarkColors.TYPE_ARGUMENT));
    }

    @Test
    public void nameOverrides() {
        PsiFile file = file("""
                package com.windpvp;
                import static java.lang.Math.PI;
                class Example { List<String> values; }
                """);
        DarkestDarkHighlightInfoFilter filter = new DarkestDarkHighlightInfoFilter();

        assertFalse(filter.accept(symbol(file, "com", "CLASS_NAME_ATTRIBUTES"), file));
        assertFalse(filter.accept(symbol(file, "Math", "CLASS_NAME_ATTRIBUTES"), file));
        assertFalse(filter.accept(symbol(file, "PI", "STATIC_FINAL_FIELD_ATTRIBUTES"), file));
        assertFalse(filter.accept(symbol(file, "String", "CLASS_NAME_ATTRIBUTES"), file));
        assertTrue(filter.accept(symbol(file, "List", "INTERFACE_NAME_ATTRIBUTES"), file));
        assertTrue(filter.accept(symbol(file, "String", "TYPE_PARAMETER_NAME_ATTRIBUTES"), file));
    }

    @Test
    public void diagnostics() {
        PsiFile file = file("class Example { List<Missing> values; }");
        DarkestDarkHighlightInfoFilter filter = new DarkestDarkHighlightInfoFilter();

        for (HighlightInfoType type : List.of(HighlightInfoType.ERROR, HighlightInfoType.WRONG_REF,
                HighlightInfoType.WARNING, HighlightInfoType.WEAK_WARNING)) {
            assertTrue(filter.accept(highlight(file, "Missing", type), file));
        }
        for (TextAttributesKey key : List.of(CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES,
                CodeInsightColors.DEPRECATED_ATTRIBUTES)) {
            HighlightInfoType type = new HighlightInfoType.HighlightInfoTypeImpl(HighlightSeverity.INFORMATION, key);
            assertTrue(filter.accept(highlight(file, "Missing", type), file));
        }
        for (Annotation annotation : annotations(file)) {
            assertNull(annotation.getMessage());
            assertTrue(filter.accept(HighlightInfo.fromAnnotation(annotation), file));
        }
    }

    private PsiFile file(String source) {
        return PsiFileFactory.getInstance(project).createFileFromText("Example.java", JavaFileType.INSTANCE, source);
    }

    private List<Annotation> annotations(PsiFile file) {
        AnnotationHolderImpl holder = new AnnotationHolderImpl(new AnnotationSession(file), false);
        DarkestDarkJavaAnnotator annotator = new DarkestDarkJavaAnnotator();
        for (PsiIdentifier identifier : PsiTreeUtil.findChildrenOfType(file, PsiIdentifier.class)) {
            holder.runAnnotatorWithContext(identifier, annotator);
        }
        holder.assertAllAnnotationsCreated();
        return holder;
    }

    private List<String> text(PsiFile file, TextAttributesKey key) {
        return annotations(file).stream()
                .filter(annotation -> key.equals(annotation.getTextAttributes()))
                .map(annotation -> file.getText().substring(annotation.getStartOffset(), annotation.getEndOffset()))
                .toList();
    }

    private HighlightInfo symbol(PsiFile file, String name, String key) {
        HighlightInfoType type = new HighlightInfoType.HighlightInfoTypeImpl(
                HighlightInfoType.SYMBOL_TYPE_SEVERITY, TextAttributesKey.createTextAttributesKey(key));
        return highlight(file, name, type);
    }

    private HighlightInfo highlight(PsiFile file, String name, HighlightInfoType type) {
        return HighlightInfo.newHighlightInfo(type)
                .range(file.findElementAt(file.getText().indexOf(name)))
                .createUnconditionally();
    }

    private static final class TestColorsManager extends EditorColorsManager {
        private EditorColorsScheme scheme;

        @Override
        public void addColorScheme(EditorColorsScheme scheme) {
        }

        @Override
        public EditorColorsScheme[] getAllSchemes() {
            return new EditorColorsScheme[]{scheme};
        }

        @Override
        public void setGlobalScheme(EditorColorsScheme scheme) {
            this.scheme = scheme;
        }

        @Override
        public void setCurrentSchemeOnLafChange(EditorColorsScheme scheme) {
            setGlobalScheme(scheme);
        }

        @Override
        public EditorColorsScheme getGlobalScheme() {
            return scheme;
        }

        @Override
        public EditorColorsScheme getScheme(String name) {
            return scheme;
        }

        @Override
        public boolean isDefaultScheme(EditorColorsScheme scheme) {
            return false;
        }

        @Override
        public boolean isUseOnlyMonospacedFonts() {
            return true;
        }

        @Override
        public void setUseOnlyMonospacedFonts(boolean value) {
        }

        @Override
        public long getSchemeModificationCounter() {
            return 0;
        }
    }
}
