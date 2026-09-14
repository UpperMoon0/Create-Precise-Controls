package com.nstut.createprecisecontrols;

/**
 * Decides whether Create's normal ValueSettings save-on-Use-release should be replaced by the
 * exact target editor. The release itself is owned by Create; Precise Controls only diverts the
 * Ctrl-modified Factory Gauge case.
 */
public final class ExactTargetRelease {
    private ExactTargetRelease() {}

    public enum Action {
        CREATE_SAVE,
        OPEN_EXACT
    }

    public static Action decide(boolean controlDown, boolean factoryGauge, boolean supportsExactTarget) {
        return controlDown && factoryGauge && supportsExactTarget ? Action.OPEN_EXACT : Action.CREATE_SAVE;
    }
}
