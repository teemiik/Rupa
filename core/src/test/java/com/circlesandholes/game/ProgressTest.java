package com.circlesandholes.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Progress")
class ProgressTest {

    private Preferences prefs;
    private Application previousApp;

    @BeforeEach
    void setUp() {
        previousApp = Gdx.app;
        prefs = mock(Preferences.class);
        Application app = mock(Application.class);
        when(app.getPreferences("rupa.progress")).thenReturn(prefs);
        Gdx.app = app;
    }

    @AfterEach
    void tearDown() {
        Gdx.app = previousApp;
    }

    @ParameterizedTest(name = "{0}s -> {1}")
    @CsvSource({
            "0, 00:00",
            "5, 00:05",
            "59, 00:59",
            "60, 01:00",
            "65, 01:05",
            "125, 02:05",
            "600, 10:00",
            "3599, 59:59"
    })
    void formatTime_formatsAsMinutesColonSeconds(int seconds, String expected) {
        assertEquals(expected, Progress.formatTime(seconds));
    }

    @Test
    void bestTime_returnsMinusOneWhenNeverStored() {
        when(prefs.getInteger("best_3", -1)).thenReturn(-1);
        assertEquals(-1, Progress.bestTime(3));
    }

    @Test
    void isCompleted_isFalseWhenNeverFinished() {
        when(prefs.getInteger("best_3", -1)).thenReturn(-1);
        assertFalse(Progress.isCompleted(3));
    }

    @Test
    void isCompleted_isTrueWhenHasBestTime() {
        when(prefs.getInteger("best_3", -1)).thenReturn(42);
        assertTrue(Progress.isCompleted(3));
    }

    @Test
    void recordTime_storesFirstResult() {
        when(prefs.getInteger("best_2", -1)).thenReturn(-1);
        Progress.recordTime(2, 30);
        verify(prefs).putInteger("best_2", 30);
        verify(prefs).flush();
    }

    @Test
    void recordTime_storesWhenResultIsBetter() {
        when(prefs.getInteger("best_2", -1)).thenReturn(50);
        Progress.recordTime(2, 30);
        verify(prefs).putInteger("best_2", 30);
        verify(prefs).flush();
    }

    @Test
    void recordTime_keepsPreviousWhenResultIsWorse() {
        when(prefs.getInteger("best_2", -1)).thenReturn(10);
        Progress.recordTime(2, 30);
        verify(prefs, never()).putInteger(eq("best_2"), anyInt());
        verify(prefs, never()).flush();
    }
}
