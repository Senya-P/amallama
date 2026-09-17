package cz.cuni.mff.core.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A chat client that communicates with the OpenAI API.
 */
public final class OpenAIChatClient implements ChatClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http;
    private final URI endpoint;

    /**
     * Creates a client for the given server.
     * @param baseUrl the base URL of the OpenAI-compatible server, e.g. {@code http://127.0.0.1:8080}
     */
    public OpenAIChatClient(String baseUrl) {
        this.http = HttpClient.newHttpClient();
        this.endpoint = URI.create(baseUrl + "/v1/chat/completions");
    }

    @Override
    public CompletableFuture<ChatResponse> send(ChatRequest request) {
        HttpRequest httpRequest = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(writeBody(request)))
                .build();

        return http.sendAsync(httpRequest, BodyHandlers.ofString())
                .thenApply(this::handleResponse);
    }

    private ChatResponse handleResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new ChatException(
                "Chat completion failed: HTTP " + response.statusCode() + " — " + response.body()
            );
        }
        return toChatResponse(readResponse(response.body()));
    }

    private CompletionResponse readResponse(String body) {
        try {
            return MAPPER.readValue(body, CompletionResponse.class);
        } catch (JsonProcessingException e) {
            throw new ChatException("Failed to parse chat response" + ": " + e.getMessage());
        }
    }

    private String writeBody(ChatRequest request) {
        try {
            return MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new ChatException("Failed to serialize chat request" + ": " + e.getMessage());
        }
    }

    /**
     * Maps a parsed completion response DTO to the {@link ChatResponse}.
     */
    private static ChatResponse toChatResponse(CompletionResponse response) {
        CompletionResponse.Choice choice = response.choices().get(0);
        return new ChatResponse(
            choice.message().content(),
            choice.finishReason(),
            response.usage().promptTokens(),
            response.usage().completionTokens()
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CompletionResponse(List<Choice> choices, Usage usage) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Choice(Message message, @JsonProperty("finish_reason") String finishReason) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Message(String content) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Usage(
            @JsonProperty("prompt_tokens") int promptTokens, 
            @JsonProperty("completion_tokens") int completionTokens
        ) {}
    }

    
}
