module amallama.core {
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    exports cz.cuni.mff.core;
    exports cz.cuni.mff.core.chat;
    exports cz.cuni.mff.core.runtime;

    opens cz.cuni.mff.core.chat to com.fasterxml.jackson.databind;
}