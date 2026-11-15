package gym.model;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public final class AccessResult {
	private final boolean allowed;
	private final String message;
	private final Duration duration;

	private AccessResult(boolean allowed, String message, Duration duration) {
		this.allowed = allowed;
		this.message = Objects.requireNonNull(message);
		this.duration = duration;
	}

	public static AccessResult allowed(Duration duration) {
		return new AccessResult(true, "Access granted", Objects.requireNonNull(duration));
	}

	public static AccessResult denied(String reason) {
		return new AccessResult(false, Objects.requireNonNull(reason), null);
	}

	public boolean isAllowed() {
		return allowed;
	}

	public String getMessage() {
		return message;
	}

	public Optional<Duration> getDuration() {
		return Optional.ofNullable(duration);
	}
}
