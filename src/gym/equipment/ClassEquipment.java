package gym.equipment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import gym.model.Member;
import gym.model.Service;

public final class ClassEquipment extends Equipment {
	private final int capacity;
	private final int level;
	private final Duration classDuration;
	private final Set<Integer> participants = new HashSet<>();
	private LocalDateTime classEnd;

	public ClassEquipment(int id, String name, Service service, String iconName, int level, int capacity,
			Duration classDuration) {
		this(id, name, service, iconName, level, capacity, classDuration, true);
	}

	public ClassEquipment(int id, String name, Service service, String iconName, int level, int capacity,
			Duration classDuration, boolean requiresMemberInside) {
		super(id, name, service, iconName, requiresMemberInside);
		if (level < 1 || level > 2 || service.getClassLevel() != level)
			throw new IllegalArgumentException("Class level must match its service level (1 or 2)");
		if (capacity <= 0)
			throw new IllegalArgumentException("Class capacity must be positive");
		if (classDuration.isNegative() || classDuration.isZero())
			throw new IllegalArgumentException("Class duration must be positive");

		this.level = level;
		this.capacity = capacity;
		this.classDuration = classDuration;
	}

	public int getLevel() {
		return level;
	}

	public int getCapacity() {
		return capacity;
	}

	public boolean isInProgress(LocalDateTime now) {
		refresh(now);
		return classEnd != null;
	}

	@Override
	public boolean isBusy(LocalDateTime now) {
		refresh(now);
		return classEnd != null && participants.size() >= capacity;
	}

	@Override
	public boolean isAvailable(Member member, LocalDateTime now) {
		refresh(now);

		if (classEnd == null)
			return true;

		return participants.size() < capacity && !participants.contains(member.getId());
	}

	@Override
	protected Optional<Duration> resolveDuration(Member member, LocalDateTime now) {
		refresh(now);

		return Optional.of(classEnd == null ? classDuration : Duration.between(now, classEnd));
	}

	@Override
	protected Duration reserve(Member member, LocalDateTime now, Duration requestedDuration) {
		refresh(now);

		if (classEnd == null)
			classEnd = now.plus(classDuration);

		participants.add(member.getId());

		return Duration.between(now, classEnd);
	}

	private void refresh(LocalDateTime now) {
		if (classEnd != null && !now.isBefore(classEnd)) {
			classEnd = null;
			participants.clear();
		}
	}
}
