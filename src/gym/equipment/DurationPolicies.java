package gym.equipment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import gym.model.Member;
import gym.model.Service;
import gym.plan.PlanRule;

public final class DurationPolicies {
	private DurationPolicies() {
	}

	public static DurationPolicy fixed(Duration duration) {
		Objects.requireNonNull(duration);
		if (duration.isNegative() || duration.isZero())
			throw new IllegalArgumentException("Equipment duration must be positive");
		return (member, service, now) -> Optional.of(duration);
	}

	public static DurationPolicy fromPlan() {
		return (member, service, now) -> {
			PlanRule rule = member.getPlan().getRule(service);
			return rule == null ? Optional.empty() : rule.getPlanDuration(member, service, now);
		};
	}
}
