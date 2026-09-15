module amallama.core {
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    exports cz.cuni.mff.core;
    exports cz.cuni.mff.core.chat;
    exports cz.cuni.mff.core.runtime;
    exports cz.cuni.mff.core.model;
    exports cz.cuni.mff.core.download;

    opens cz.cuni.mff.core.chat to com.fasterxml.jackson.databind;
    opens cz.cuni.mff.core to com.fasterxml.jackson.databind;
}