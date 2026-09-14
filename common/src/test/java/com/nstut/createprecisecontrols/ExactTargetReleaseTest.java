package com.nstut.createprecisecontrols;

import org.junit.jupiter.api.Test;

import static com.nstut.createprecisecontrols.ExactTargetRelease.Action.CREATE_SAVE;
import static com.nstut.createprecisecontrols.ExactTargetRelease.Action.OPEN_EXACT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExactTargetReleaseTest {
    @Test
    void ctrlModifiedFactoryGaugeUseReleaseOpensExactEditor() {
        assertEquals(OPEN_EXACT, ExactTargetRelease.decide(true, true, true));
    }

    @Test
    void ordinaryUseReleaseKeepsCreateSaveLifecycle() {
        assertEquals(CREATE_SAVE, ExactTargetRelease.decide(false, true, true));
    }

    @Test
    void ctrlReleaseOnOtherValueSettingsKeepsCreateSaveLifecycle() {
        assertEquals(CREATE_SAVE, ExactTargetRelease.decide(true, false, true));
    }

    @Test
    void unsupportedResourceTargetKeepsAddonOwnedLifecycle() {
        assertEquals(CREATE_SAVE, ExactTargetRelease.decide(true, true, false));
    }
}
