package cz.cuni.mff.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class LocalModelTest {

    @Test
    void ggufModelIsNotSelfContained() {
        LocalModel model = LocalModel.of(Path.of("/models/tiny.gguf"));

        assertFalse(model.selfContained());
    }

    @Test
    void llamafileModelIsSelfContained() {
        LocalModel model = LocalModel.of(Path.of("/models/tiny.llamafile"));

        assertTrue(model.selfContained());
    }

    @Test
    void windowsLlamafileExecutableIsSelfContained() {
        LocalModel model = LocalModel.of(Path.of("foo.llamafile.exe"));

        assertTrue(model.selfContained());
        assertEquals("foo.llamafile.exe", model.name());
    }

    @Test
    void bareLlamafileRuntimeIsNotASelfContainedModel() {
        LocalModel model = LocalModel.of(Path.of("llamafile.exe"));

        assertFalse(model.selfContained());
    }

    @Test
    void detectionIsCaseInsensitiveButNameIsPreserved() {
        LocalModel model = LocalModel.of(Path.of("Foo.LLAMAFILE.EXE"));

        assertTrue(model.selfContained());
        assertEquals("Foo.LLAMAFILE.EXE", model.name());
    }

    @Test
    void nameIsTheFileName() {
        LocalModel model = LocalModel.of(Path.of("/models/tiny.gguf"));

        assertEquals("tiny.gguf", model.name());
        assertEquals(Path.of("/models/tiny.gguf"), model.path());
    }
}
