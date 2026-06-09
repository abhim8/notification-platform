package notification.domain.channel;

import notification.domain.event.NotificationEvent;

/**
 * Port interface for sending notifications through a specific channel.
 *
 * Each channel adapter (Email, SMS, Push, Webhook) implements this interface.
 * This is a hexagonal architecture port - an abstraction from the domain that
 * adapters must implement.
 */
public interface ChannelDispatcher {

    /**
     * Send a notification through this channel.
     *
     * @param event the notification event to send
     * @param recipient the recipient identifier (email, phone, device ID, webhook URL, etc.)
     * @param content the notification content/body to send
     * @return the result of the dispatch attempt
     * @throws ChannelDispatchException if the channel dispatch fails
     */
    DispatchResult dispatch(NotificationEvent event, String recipient, String content);

    /**
     * Get the channel type this dispatcher handles
     */
    Channel getChannel();
}

