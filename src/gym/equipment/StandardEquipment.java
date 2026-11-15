package gym.equipment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import gym.model.Member;
import gym.model.Service;

public class StandardEquipment extends Equipment {
	private final DurationPolicy durationPolicy;

	public StandardEquipment(int id, String name, Service service, String iconName, DurationPolicy durationPolicy,
			boolean requiresMemberInside) {
		super(id, name, service, iconName, requiresMemberInside);

		this.durationPolicy = Objects.requireNonNull(durationPolicy);
	}

	@Override
	protected Optional<Duration> resolveDuration(Member member, LocalDateTime now) {
		return durationPolicy.resolve(member, getService(), now);
	}
}
