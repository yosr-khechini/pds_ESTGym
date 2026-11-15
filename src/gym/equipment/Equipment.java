package gym.equipment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import gym.model.Member;
import gym.model.Service;

public abstract class Equipment {
	private final int id;
	private final String name;
	private final Service service;
	private final String iconName;
	private final boolean requiresMemberInside;
	private LocalDateTime busyUntil;

	protected Equipment(int id, String name, Service service, String iconName, boolean requiresMemberInside) {
		if (id <= 0)
			throw new IllegalArgumentException("Equipment id must be positive");
		this.id = id;
		this.name = Objects.requireNonNull(name);
		this.service = Objects.requireNonNull(service);
		this.iconName = iconName;
		this.requiresMemberInside = requiresMemberInside;
	}

	public final int getId() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public final Service getService() {
		return service;
	}

	public final String getIconName() {
		return iconName;
	}

	public final boolean requiresMemberInside() {
		return requiresMemberInside;
	}

	public boolean isBusy(LocalDateTime now) {
		return busyUntil != null && now.isBefore(busyUntil);
	}

	public boolean isAvailable(Member member, LocalDateTime now) {
		return !isBusy(now);
	}

	public final boolean isBusy() {
		return isBusy(pds.util.SimulatedClock.getCurrentTime());
	}

	public final Optional<Duration> getDuration(Member member, LocalDateTime now) {
		return resolveDuration(member, now);
	}

	protected abstract Optional<Duration> resolveDuration(Member member, LocalDateTime now);

	protected String validateMember(Member member) {
		return null;
	}

	protected void onSuccessfulUse(Member member) {
	}

	protected Duration reserve(Member member, LocalDateTime now, Duration requestedDuration) {
		busyUntil = now.plus(requestedDuration);
		return requestedDuration;
	}

	public final String memberValidationFailure(Member member) {
		return validateMember(member);
	}

	public final void completeUse(Member member) {
		onSuccessfulUse(member);
	}

	public final Duration occupy(Member member, LocalDateTime now, Duration duration) {
		return reserve(member, now, duration);
	}
}
