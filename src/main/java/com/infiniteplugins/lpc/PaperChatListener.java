package com.infiniteplugins.lpc;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PaperChatListener implements Listener {

	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
	private static final Pattern SPRITE = Pattern.compile("<sprite(?::(?:\"([^\"]*)\"|'([^']*)'|([^:>]*?))):(?:\"([^\"]*)\"|'([^']*)'|([^>]*?))>", Pattern.CASE_INSENSITIVE);

	private final LPC plugin;

	PaperChatListener(final LPC plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(final AsyncChatEvent event) {
		final Player player = event.getPlayer();
		final String format = plugin.buildFormat(player);
		final Component message = deserialize(plugin.processMessage(player, LEGACY.serialize(event.message())));

		event.message(message);
		event.renderer(new ChatRenderer() {
			@Override
			public Component render(final Player source, final Component sourceDisplayName, final Component message, final Audience viewer) {
				return formatMessage(format, message);
			}
		});
	}

	private Component formatMessage(final String format, final Component message) {
		final String[] parts = format.split("\\{message}", -1);
		Component component = deserialize(parts[0]);

		for (int i = 1; i < parts.length; i++) {
			component = component.append(message).append(deserialize(parts[i]));
		}

		return component;
	}

	private Component deserialize(final String input) {
		if (input == null || input.isEmpty()) {
			return Component.empty();
		}

		final Matcher matcher = SPRITE.matcher(input);
		int cursor = 0;
		Component component = Component.empty();
		while (matcher.find()) {
			if (matcher.start() > cursor) {
				component = component.append(LEGACY.deserialize(input.substring(cursor, matcher.start())));
			}
			component = component.append(sprite(first(matcher, 1, 3), first(matcher, 4, 6)));
			cursor = matcher.end();
		}
		if (cursor < input.length()) {
			component = component.append(LEGACY.deserialize(input.substring(cursor)));
		}
		return component;
	}

	private static String first(final Matcher matcher, final int from, final int to) {
		for (int i = from; i <= to; i++) {
			final String group = matcher.group(i);
			if (group != null) {
				return group;
			}
		}
		return "";
	}

	private static Component sprite(final String atlas, final String sprite) {
		try {
			final Key spriteKey = Key.key(sprite.trim());
			if (atlas == null || atlas.trim().isEmpty()) {
				return Component.object(ObjectContents.sprite(spriteKey));
			}
			return Component.object(ObjectContents.sprite(Key.key(atlas.trim()), spriteKey));
		} catch (final IllegalArgumentException ignored) {
			return Component.text("<sprite" + (atlas == null || atlas.isEmpty() ? "" : ":" + atlas) + ":" + sprite + ">");
		}
	}
}
