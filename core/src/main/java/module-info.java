/**
 * Core backend module of amallama. Contains no JavaFX dependencies so the
 * backend stays usable and testable on its own.
 */
module amallama.core {
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires java.logging;
    requires transitive com.github.oshi.ffm;
    requires java.management;

    exports cz.cuni.mff.core;
    exports cz.cuni.mff.core.chat;
    exports cz.cuni.mff.core.runtime;
    exports cz.cuni.mff.core.model;
    exports cz.cuni.mff.core.download;
    exports cz.cuni.mff.core.hw;
    exports cz.cuni.mff.core.monitor;

    opens cz.cuni.mff.core.chat to com.fasterxml.jackson.databind;
    opens cz.cuni.mff.core to com.fasterxml.jackson.databind;
}