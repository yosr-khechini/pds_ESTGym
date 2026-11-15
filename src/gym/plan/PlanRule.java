package gym.plan;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import gym.model.Member;
import gym.model.Service;

public interface PlanRule {
	Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration);

	String getDescription();

	default Optional<Duration> getPlanDuration(Member member, Service service, LocalDateTime now) {
		return Optional.empty();
	}

	default Optional<ShowerContinuation> getShowerContinuation() {
		return Optional.empty();
	}

	final class Decision {
		private static final Decision ALLOWED = new Decision(true, "");
		private final boolean allowed;
		private final String reason;

		private Decision(boolean allowed, String reason) {
			this.allowed = allowed;
			this.reason = reason;
		}

		public static Decision allow() {
			return ALLOWED;
		}

		public static Decision deny(String reason) {
			if (reason == null || reason.isBlank())
				throw new IllegalArgumentException("A denial reason is required");

			return new Decision(false, reason);
		}

		public boolean isAllowed() {
			return allowed;
		}

		public String getReason() {
			return reason;
		}
	}
}
