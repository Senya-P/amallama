module amallama.core {
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    exports cz.cuni.mff.core;
    exports cz.cuni.mff.core.chat;
    exports cz.cuni.mff.core.runtime;

    // Jackson reflects into the chat DTOs (including the private nested
    // CompletionResponse in OpenAIChatClient) — exports alone is not enough.
    opens cz.cuni.mff.core.chat to com.fasterxml.jackson.databind;
}