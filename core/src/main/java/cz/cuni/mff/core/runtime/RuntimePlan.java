package cz.cuni.mff.core.runtime;

/**
 * The runtime configuration together with the reason it was chosen. 
 *
 * @param config the configuration to launch the backend with
 * @param reason why this processing unit was chosen
 */
public record RuntimePlan(RuntimeConfig config, Reason reason) {
}
