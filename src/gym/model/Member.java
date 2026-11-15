package gym.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import gym.plan.GymPlan;
import gym.plan.PlanRule;
import gym.plan.ShowerContinuation;

public final class Member {
	private final int id;
	private final String name;
	private final String cardId;
	private final GymPlan plan;
	private final List<UsageRecord> usageRecords = new ArrayList<>();
	private final Map<String, ServiceUsage> usageByService = new HashMap<>();
	private boolean inside;
	private Service activeService;
	private LocalDateTime activeUntil;
	private ShowerSession pendingShower;

	public Member(int id, String name, String cardId, GymPlan plan) {
		if (id <= 0)
			throw new IllegalArgumentException("Member id must be positive");
		this.id = id;
		this.name = Objects.requireNonNull(name);
		this.cardId = Objects.requireNonNull(cardId);
		this.plan = Objects.requireNonNull(plan);
	}

	public Member(int id, String name, GymPlan plan) {
		this(id, name, "CARD-" + id, plan);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCardId() {
		return cardId;
	}

	public GymPlan getPlan() {
		return plan;
	}

	public boolean isInside() {
		return inside;
	}

	public List<UsageRecord> getUsageRecords() {
		return Collections.unmodifiableList(usageRecords);
	}

	public Map<String, PlanRule> getContractedServices() {
		return plan.getRules();
	}

	public boolean isUsing(LocalDateTime now) {
		return activeUntil != null && now.isBefore(activeUntil);
	}

	public Service getActiveService(LocalDateTime now) {
		return isUsing(now) ? activeService : null;
	}

	public PlanRule.Decision checkPlan(Service service, LocalDateTime now, Duration duration) {
		PlanRule rule = plan.getRule(service);
		if (rule == null)
			return PlanRule.Decision.deny("The service is not included in the member's plan");
		return rule.evaluate(this, service, now, duration);
	}

	public long getUsageCount(Service service, UsagePeriod period, LocalDateTime now) {
		Objects.requireNonNull(service);
		Objects.requireNonNull(period);
		Objects.requireNonNull(now);
		ServiceUsage usage = existingUsage(service);
		return switch (period) {
			case DAY -> usage.usesByDay.getOrDefault(now.toLocalDate(), 0L);
			case WEEK -> usage.usesByWeek.getOrDefault(periodKey(period, now), 0L);
			case MONTH -> usage.usesByMonth.getOrDefault(periodKey(period, now), 0L);
		};
	}

	public long getUsageCount(int classLevel, UsagePeriod period, LocalDateTime now) {
		if (classLevel < 1 || classLevel > 2)
			throw new IllegalArgumentException("Class level must be 1 or 2");
		Objects.requireNonNull(period);
		Objects.requireNonNull(now);
		return usageRecords.stream()
				.filter(record -> record.getService().getClassLevel() == classLevel)
				.filter(record -> periodKey(period, record.getDateTime()).equals(periodKey(period, now)))
				.count();
	}

	public int getUsageDaysInWeek(Service service, LocalDateTime now) {
		Objects.requireNonNull(service);
		Objects.requireNonNull(now);
		return existingUsage(service).daysByWeek
				.getOrDefault(periodKey(UsagePeriod.WEEK, now), Collections.emptySet()).size();
	}

	public long getUsesOnDay(Service service, LocalDate date) {
		return existingUsage(service).usesByDay.getOrDefault(date, 0L);
	}

	public long getUsesInWeek(Service service, String weekKey) {
		return existingUsage(service).usesByWeek.getOrDefault(weekKey, 0L);
	}

	public long getUsesInMonth(Service service, String monthKey) {
		return existingUsage(service).usesByMonth.getOrDefault(monthKey, 0L);
	}

	public long getUsedSecondsOn(Service service, LocalDate date) {
		return existingUsage(service).secondsByDay.getOrDefault(date, 0L);
	}

	public int getUseDaysInWeek(Service service, String weekKey) {
		return existingUsage(service).daysByWeek.getOrDefault(weekKey, Collections.emptySet()).size();
	}

	public LocalDateTime getLastUse(Service service) {
		return existingUsage(service).lastUse;
	}

	public Optional<UsageRecord> getLastUsageRecord(Service service) {
		Objects.requireNonNull(service);
		for (int index = usageRecords.size() - 1; index >= 0; index--) {
			UsageRecord record = usageRecords.get(index);
			if (record.getService().equals(service))
				return Optional.of(record);
		}
		return Optional.empty();
	}

	public void setInside(boolean inside) {
		this.inside = inside;
	}

	public void beginUse(Service service, String equipmentName, LocalDateTime now, Duration duration) {
		pendingShower = null;
		activeService = service;
		activeUntil = now.plus(duration);
		usageRecords.add(new UsageRecord(service, equipmentName, now, duration));
		usage(service).record(now, duration);
	}

	public void scheduleShowerExtra(int equipmentId, Service service, LocalDateTime initialPhaseEnd,
			ShowerContinuation continuation) {
		Duration extraDuration = continuation.getExtraDuration();
		pendingShower = extraDuration.isZero() ? null
				: new ShowerSession(equipmentId, service, initialPhaseEnd, continuation, extraDuration);
	}

	public boolean canActivateShowerExtra(int equipmentId, LocalDateTime now) {
		return pendingShower != null && pendingShower.equipmentId == equipmentId
				&& pendingShower.continuation.canActivateExtra(pendingShower.initialPhaseEnd, now);
	}

	public Duration getShowerExtraDuration(int equipmentId, LocalDateTime now) {
		return canActivateShowerExtra(equipmentId, now) ? pendingShower.extraDuration : Duration.ZERO;
	}

	public void activateShowerExtra(String equipmentName, LocalDateTime now) {
		ShowerSession session = pendingShower;
		pendingShower = null;
		activeService = session.service;
		activeUntil = now.plus(session.extraDuration);
		usageRecords.add(new UsageRecord(session.service, equipmentName, now, session.extraDuration));
		usage(session.service).recordExtra(now, session.extraDuration);
	}

	private ServiceUsage usage(Service service) {
		return usageByService.computeIfAbsent(service.getId(), ignored -> new ServiceUsage());
	}

	private ServiceUsage existingUsage(Service service) {
		return usageByService.getOrDefault(service.getId(), ServiceUsage.EMPTY);
	}

	private static final class ServiceUsage {
		private static final ServiceUsage EMPTY = new ServiceUsage();
		private final Map<LocalDate, Long> secondsByDay = new HashMap<>();
		private final Map<LocalDate, Long> usesByDay = new HashMap<>();
		private final Map<String, Long> usesByWeek = new HashMap<>();
		private final Map<String, Long> usesByMonth = new HashMap<>();
		private final Map<String, Set<LocalDate>> daysByWeek = new HashMap<>();
		private LocalDateTime lastUse;

		private void record(LocalDateTime now, Duration duration) {
			LocalDate date = now.toLocalDate();
			String week = periodKey(UsagePeriod.WEEK, now);
			secondsByDay.merge(date, duration.getSeconds(), Long::sum);
			usesByDay.merge(date, 1L, Long::sum);
			usesByWeek.merge(week, 1L, Long::sum);
			usesByMonth.merge(periodKey(UsagePeriod.MONTH, now), 1L, Long::sum);
			daysByWeek.computeIfAbsent(week, ignored -> new HashSet<>()).add(date);
			lastUse = now;
		}

		private void recordExtra(LocalDateTime now, Duration duration) {
			LocalDate date = now.toLocalDate();
			String week = periodKey(UsagePeriod.WEEK, now);
			secondsByDay.merge(date, duration.getSeconds(), Long::sum);
			daysByWeek.computeIfAbsent(week, ignored -> new HashSet<>()).add(date);
		}

	}

	public enum UsagePeriod {
		DAY, WEEK, MONTH
	}

	private static String periodKey(UsagePeriod period, LocalDateTime now) {
		return switch (period) {
			case DAY -> now.toLocalDate().toString();
			case WEEK -> {
				WeekFields fields = WeekFields.ISO;
				yield now.get(fields.weekBasedYear()) + "-W" + now.get(fields.weekOfWeekBasedYear());
			}
			case MONTH -> now.getYear() + "-" + now.getMonthValue();
		};
	}

	private static final class ShowerSession {
		private final int equipmentId;
		private final Service service;
		private final LocalDateTime initialPhaseEnd;
		private final ShowerContinuation continuation;
		private final Duration extraDuration;

		private ShowerSession(int equipmentId, Service service, LocalDateTime initialPhaseEnd,
				ShowerContinuation continuation, Duration extraDuration) {
			this.equipmentId = equipmentId;
			this.service = service;
			this.initialPhaseEnd = initialPhaseEnd;
			this.continuation = continuation;
			this.extraDuration = extraDuration;
		}
	}
}
