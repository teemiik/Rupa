package com.circlesandholes.game;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("LevelLoader")
class LevelLoaderTest {

    private Files files;
    private Files previousFiles;

    @BeforeEach
    void setUp() {
        previousFiles = Gdx.files;
        files = mock(Files.class);
        Gdx.files = files;
    }

    @AfterEach
    void tearDown() {
        Gdx.files = previousFiles;
    }

    private FileHandle resource(String name) throws Exception {
        return new FileHandle(new File(
                getClass().getClassLoader().getResource("levels/" + name).toURI()));
    }

    @Test
    void count_walksUntilFirstMissingFile() throws Exception {
        when(files.internal("levels/level1.json")).thenReturn(resource("sample.json"));
        when(files.internal("levels/level2.json")).thenReturn(new FileHandle(new File("does-not-exist.json")));
        assertEquals(1, LevelLoader.count());
    }

    @Test
    void count_isZeroWhenNoLevelFiles() {
        when(files.internal("levels/level1.json")).thenReturn(new FileHandle(new File("does-not-exist.json")));
        assertEquals(0, LevelLoader.count());
    }

    @Test
    void load_parsesAllFields() throws Exception {
        when(files.internal("levels/level1.json")).thenReturn(resource("sample.json"));
        LevelData data = LevelLoader.load(1);

        assertEquals("board_2", data.board);
        assertEquals("background_2", data.background);
        assertTrue(data.showHints);

        assertEquals(2, data.holes.size());
        assertArrayEquals(new float[]{0.5f, 0.3f}, data.holes.get(0), 0.0001f);
        assertArrayEquals(new float[]{0.2f, 0.6f}, data.holes.get(1), 0.0001f);

        assertEquals(1, data.dynamicHoles.size());
        assertEquals(0.4f, data.dynamicHoles.get(0).x, 0.0001f);
        assertEquals(1, data.dynamicHoles.get(0).xDin);
        assertEquals(-1, data.dynamicHoles.get(0).yDin);

        assertEquals(0.1f, data.ctrlLowerX, 0.0001f);
        assertEquals(0.9f, data.ctrlUpperX, 0.0001f);
        assertTrue(data.ctrlInverted);

        assertEquals(1, data.barriers.size());
        assertEquals(0.5f, data.barriers.get(0).x, 0.0001f);
        assertEquals(30f, data.barriers.get(0).rotation, 0.0001f);

        assertEquals(1, data.rotatingPlatforms.size());
        assertEquals(90f, data.rotatingPlatforms.get(0).rotationSpeed, 0.0001f);
        assertEquals("red", data.rotatingPlatforms.get(0).color);
    }

    @Test
    void load_appliesDefaultsForMissingOptionalFields() throws Exception {
        when(files.internal("levels/level1.json")).thenReturn(resource("defaults.json"));
        LevelData data = LevelLoader.load(1);

        assertEquals("board", data.board);
        assertEquals("background", data.background);
        assertFalse(data.showHints);
        assertTrue(data.holes.isEmpty());
        assertEquals(0f, data.ctrlLowerX, 0.0001f);
        assertFalse(data.ctrlInverted);

        assertEquals(1, data.rotatingPlatforms.size());
        assertEquals(45f, data.rotatingPlatforms.get(0).rotationSpeed, 0.0001f);
        assertEquals("default", data.rotatingPlatforms.get(0).color);
    }
}
