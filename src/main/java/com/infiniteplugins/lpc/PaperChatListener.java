package com.infiniteplugins.lpc;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

final class PaperChatListener implements Listener {

	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

	private final LPC plugin;

	PaperChatListener(final LPC plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(final AsyncChatEvent event) {
		final Player player = event.getPlayer();
		final String format = plugin.buildFormat(player);
		final Component message = LEGACY.deserialize(plugin.processMessage(player, LEGACY.serialize(event.message())));

		event.message(message);
		event.renderer(new ChatRenderer() {
			@Override
			public Component render(final Player source, final Component sourceDisplayName, final Component message, final Audience viewer) {
				return formatMessage(format, message);
			}
		});
	}

	private Component formatMessage(final String format, final Component message) {
		// Parse the complete legacy string so the formatting before {message} carries into the message.
		return LEGACY.deserialize(format.replace("{message}", LEGACY.serialize(message)));
	}
}
