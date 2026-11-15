package gym.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public final class UsageRecord {
	private final Service service;
	private final String equipmentName;
	private final LocalDateTime dateTime;
	private final Duration duration;

	public UsageRecord(Service service, String equipmentName, LocalDateTime dateTime, Duration duration) {
		this.service = Objects.requireNonNull(service);
		this.equipmentName = Objects.requireNonNull(equipmentName);
		this.dateTime = Objects.requireNonNull(dateTime);
		this.duration = Objects.requireNonNull(duration);
	}

	public Service getService() {
		return service;
	}

	public String getServiceName() {
		return service.getName();
	}

	public String getEquipmentName() {
		return equipmentName;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public Duration getDuration() {
		return duration;
	}
}
