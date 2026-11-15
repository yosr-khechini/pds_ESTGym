package gym.plan;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import gym.model.Member;
import gym.model.Service;
import gym.model.UsageRecord;

public final class PlanRules {
	private PlanRules() {
	}

	public static PlanRule alwaysAllowed() {
		return new SimpleRule("Allowed", null);
	}

	public static PlanRule neverAllowed(String reason) {
		return new SimpleRule("Not included", reason);
	}

	public static PlanRule minutesPerDayAndDaysPerWeek(long minutesPerDay, int daysPerWeek) {
		return new MinutesAndDaysRule(minutesPerDay, daysPerWeek);
	}

	public static PlanRule daysPerWeek(int maximumDays) {
		return new DaysPerWeekRule(maximumDays);
	}

	public static PlanRule usesPerDay(long uses) {
		return new UsesLimitRule(uses, Member.UsagePeriod.DAY);
	}

	public static PlanRule usesPerWeek(long uses) {
		return new UsesLimitRule(uses, Member.UsagePeriod.WEEK);
	}

	public static PlanRule usesPerMonth(long uses) {
		return new UsesLimitRule(uses, Member.UsagePeriod.MONTH);
	}

	public static PlanRule classUsesPerWeek(int classLevel, long maximumUses) {
		return new ClassUsesPerWeekRule(classLevel, maximumUses);
	}

	public static PlanRule showerUsesPerDay(long maximumUses, Duration initialDuration, Duration extraDuration) {
		return new ShowerUsesPerDayRule(maximumUses, initialDuration, extraDuration);
	}

	public static PlanRule minimumInterval(Duration interval) {
		return new MinimumIntervalRule(interval);
	}

	public static PlanRule onceEveryMonths(int months) {
		return new MonthsIntervalRule(months);
	}

	public static PlanRule classLevel(int maximumLevel) {
		return new ClassLevelRule(maximumLevel);
	}

	public static ShowerRule shower(Duration initialDuration, Duration extraDuration) {
		return new ShowerRule(initialDuration, extraDuration, initialDuration);
	}

	public static PlanRule allOf(PlanRule... rules) {
		return new CompositeRule(true, rules);
	}

	public static PlanRule anyOf(PlanRule... rules) {
		return new CompositeRule(false, rules);
	}

	private static final class SimpleRule implements PlanRule {
		private final String description;
		private final String denialReason;

		private SimpleRule(String description, String denialReason) {
			this.description = description;
			this.denialReason = denialReason;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			return denialReason == null ? Decision.allow() : Decision.deny(denialReason);
		}

		@Override
		public String getDescription() {
			return description;
		}
	}

	private static final class MinutesAndDaysRule implements PlanRule {
		private final long secondsPerDay;
		private final int daysPerWeek;

		private MinutesAndDaysRule(long minutesPerDay, int daysPerWeek) {
			if (minutesPerDay <= 0 || daysPerWeek <= 0 || daysPerWeek > 7)
				throw new IllegalArgumentException("Invalid daily minutes or weekly days");
			secondsPerDay = Math.multiplyExact(minutesPerDay, 60);
			this.daysPerWeek = daysPerWeek;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			LocalDate date = now.toLocalDate();
			long used = member.getUsedSecondsOn(service, date);
			if (used + requestedDuration.getSeconds() > secondsPerDay)
				return Decision.deny("Daily service time limit reached");
			if (member.getUsageCount(service, Member.UsagePeriod.DAY, now) == 0
					&& member.getUsageDaysInWeek(service, now) >= daysPerWeek)
				return Decision.deny("Weekly service day limit reached");
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return secondsPerDay / 60 + " minutes/day, " + daysPerWeek + " days/week";
		}

		@Override
		public Optional<Duration> getPlanDuration(Member member, Service service, LocalDateTime now) {
			long remaining = secondsPerDay - member.getUsedSecondsOn(service, now.toLocalDate());
			return Optional.of(Duration.ofSeconds(Math.max(0, remaining)));
		}
	}

	private static final class UsesLimitRule implements PlanRule {
		private final long maximumUses;
		private final Member.UsagePeriod period;

		private UsesLimitRule(long maximumUses, Member.UsagePeriod period) {
			if (maximumUses < 0)
				throw new IllegalArgumentException("Usage limit cannot be negative");
			this.maximumUses = maximumUses;
			this.period = period;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			long uses = member.getUsageCount(service, period, now);
			if (uses >= maximumUses)
				return Decision.deny(period.name().toLowerCase(Locale.ROOT) + " use limit reached");
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return maximumUses + " use(s) per " + period.name().toLowerCase(Locale.ROOT);
		}
	}

	private static final class DaysPerWeekRule implements PlanRule {
		private final int maximumDays;

		private DaysPerWeekRule(int maximumDays) {
			if (maximumDays <= 0 || maximumDays > 7)
				throw new IllegalArgumentException("Weekly day limit must be between 1 and 7");
			this.maximumDays = maximumDays;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			if (member.getUsageCount(service, Member.UsagePeriod.DAY, now) == 0
					&& member.getUsageDaysInWeek(service, now) >= maximumDays)
				return Decision.deny("Weekly service day limit reached");
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return maximumDays + " days/week";
		}
	}

	private static final class ClassUsesPerWeekRule implements PlanRule {
		private final int classLevel;
		private final long maximumUses;

		private ClassUsesPerWeekRule(int classLevel, long maximumUses) {
			if (classLevel < 1 || classLevel > 2)
				throw new IllegalArgumentException("Class level must be 1 or 2");
			if (maximumUses < 0)
				throw new IllegalArgumentException("Usage limit cannot be negative");
			this.classLevel = classLevel;
			this.maximumUses = maximumUses;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			if (member.getUsageCount(classLevel, Member.UsagePeriod.WEEK, now) >= maximumUses)
				return Decision.deny("Weekly class use limit reached");
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return maximumUses + " use(s) per week for class level " + classLevel;
		}
	}

	private static final class MinimumIntervalRule implements PlanRule {
		private final Duration interval;

		private MinimumIntervalRule(Duration interval) {
			if (interval.isNegative() || interval.isZero())
				throw new IllegalArgumentException("Interval must be positive");
			this.interval = interval;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			LocalDateTime lastUse = member.getLastUse(service);
			if (lastUse != null && now.isBefore(lastUse.plus(interval)))
				return Decision.deny("Minimum interval between uses has not elapsed");
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return "One use every " + interval.toMinutes() + " minutes";
		}
	}

	private static final class MonthsIntervalRule implements PlanRule {
		private final int months;

		private MonthsIntervalRule(int months) {
			if (months <= 0)
				throw new IllegalArgumentException("Months must be positive");
			this.months = months;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			LocalDateTime lastUse = member.getLastUse(service);
			if (lastUse != null && now.isBefore(lastUse.plusMonths(months)))
				return Decision.deny("This service is available once every " + months + " month(s)");
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return "Once every " + months + " month(s)";
		}
	}

	private static final class ClassLevelRule implements PlanRule {
		private final int maximumLevel;

		private ClassLevelRule(int maximumLevel) {
			if (maximumLevel < 0 || maximumLevel > 2)
				throw new IllegalArgumentException("Maximum class level must be between 0 and 2");
			this.maximumLevel = maximumLevel;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			if (service.getClassLevel() == 0 || service.getClassLevel() > maximumLevel)
				return Decision.deny("The member's plan does not include this class level");
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return maximumLevel == 0 ? "No classes" : "Classes up to level " + maximumLevel;
		}
	}

	public static final class ShowerRule implements PlanRule, ShowerContinuation {
		private final Duration initialDuration;
		private final Duration extraDuration;
		private final Duration activationWindow;

		private ShowerRule(Duration initialDuration, Duration extraDuration, Duration activationWindow) {
			if (initialDuration.isNegative() || initialDuration.isZero()
					|| extraDuration.isNegative() || activationWindow.isNegative())
				throw new IllegalArgumentException("Invalid shower duration");
			this.initialDuration = initialDuration;
			this.extraDuration = extraDuration;
			this.activationWindow = activationWindow;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return initialDuration.toMinutes() + " initial minutes + " + extraDuration.toMinutes()
					+ " extra minutes (activate within " + activationWindow.toMinutes() + " minutes after the initial phase)";
		}

		@Override
		public Optional<Duration> getPlanDuration(Member member, Service service, LocalDateTime now) {
			return Optional.of(initialDuration);
		}

		@Override
		public Optional<ShowerContinuation> getShowerContinuation() {
			return Optional.of(this);
		}

		public Duration getInitialDuration() {
			return initialDuration;
		}

		@Override
		public Duration getExtraDuration() {
			return extraDuration;
		}

		@Override
		public boolean canActivateExtra(LocalDateTime initialPhaseEnd, LocalDateTime now) {
			return !now.isBefore(initialPhaseEnd) && !now.isAfter(initialPhaseEnd.plus(activationWindow));
		}
	}

	private static final class ShowerUsesPerDayRule implements PlanRule {
		private final long maximumUses;
		private final Duration initialDuration;
		private final Duration extraDuration;

		private ShowerUsesPerDayRule(long maximumUses, Duration initialDuration, Duration extraDuration) {
			if (maximumUses < 0)
				throw new IllegalArgumentException("Usage limit cannot be negative");
			this.maximumUses = maximumUses;
			this.initialDuration = initialDuration;
			this.extraDuration = extraDuration;
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			if (requestedDuration.equals(extraDuration)) {
				Optional<UsageRecord> lastShower = member.getLastUsageRecord(service);
				if (lastShower.isPresent()) {
					LocalDateTime initialPhaseEnd = lastShower.get().getDateTime()
							.plus(lastShower.get().getDuration());
					if (lastShower.get().getDuration().equals(initialDuration)
							&& !now.isBefore(initialPhaseEnd)
							&& !now.isAfter(initialPhaseEnd.plus(initialDuration)))
						return Decision.allow();
				}
			}
			if (member.getUsageCount(service, Member.UsagePeriod.DAY, now) >= maximumUses)
				return Decision.deny("Daily shower limit reached");
			return Decision.allow();
		}

		@Override
		public String getDescription() {
			return maximumUses + " shower session(s) per day";
		}
	}

	private static final class CompositeRule implements PlanRule {
		private final boolean requireAll;
		private final List<PlanRule> rules;

		private CompositeRule(boolean requireAll, PlanRule... rules) {
			if (rules.length == 0)
				throw new IllegalArgumentException("A composite rule needs at least one rule");
			this.requireAll = requireAll;
			this.rules = new ArrayList<>(Arrays.asList(rules));
			if (this.rules.contains(null))
				throw new IllegalArgumentException("Rules cannot contain null");
		}

		@Override
		public Decision evaluate(Member member, Service service, LocalDateTime now, Duration requestedDuration) {
			List<String> reasons = new ArrayList<>();
			for (PlanRule rule : rules) {
				Decision result = rule.evaluate(member, service, now, requestedDuration);
				if (requireAll && !result.isAllowed())
					return result;
				if (!requireAll && result.isAllowed())
					return result;
				reasons.add(result.getReason());
			}
			return requireAll ? Decision.allow() : Decision.deny(String.join("; ", reasons));
		}

		@Override
		public String getDescription() {
			List<String> descriptions = new ArrayList<>();
			for (PlanRule rule : rules)
				descriptions.add(rule.getDescription());
			return String.join(requireAll ? " and " : " or ", descriptions);
		}

		@Override
		public Optional<Duration> getPlanDuration(Member member, Service service, LocalDateTime now) {
			Optional<Duration> shortest = Optional.empty();
			for (PlanRule rule : rules) {
				Optional<Duration> duration = rule.getPlanDuration(member, service, now);
				if (duration.isPresent() && (!shortest.isPresent()
						|| duration.get().compareTo(shortest.get()) < 0))
					shortest = duration;
			}
			return shortest;
		}

		@Override
		public Optional<ShowerContinuation> getShowerContinuation() {
			for (PlanRule rule : rules) {
				Optional<ShowerContinuation> continuation = rule.getShowerContinuation();
				if (continuation.isPresent())
					return continuation;
			}
			return Optional.empty();
		}
	}

}
