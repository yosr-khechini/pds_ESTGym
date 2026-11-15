package gym.setup;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonElement;

import gym.plan.PlanRule;
import gym.plan.PlanRules;

final class RuleFactoryRegistry {
	@FunctionalInterface
	private interface RuleFactory {
		PlanRule create(GymData.RuleDto dto, String context);
	}

	private static final Map<String, RuleFactory> FACTORIES = createFactories();

	private RuleFactoryRegistry() {
	}

	static PlanRule create(GymData.RuleDto dto, String context) {
		String type = GymSetup.requireText(dto.type, context, "type");
		RuleFactory factory = FACTORIES.get(type);
		if (factory == null)
			throw GymSetup.configurationError(context + " has unknown rule type '" + type + "'");
		try {
			return factory.create(dto, context + " rule '" + type + "'");
		} catch (IllegalArgumentException e) {
			if (e.getMessage() != null && e.getMessage().startsWith("Gym configuration error:"))
				throw e;
			throw GymSetup.configurationError(context + " rule '" + type + "': " + e.getMessage(), e);
		} catch (ArithmeticException e) {
			throw GymSetup.configurationError(context + " rule '" + type + "': " + e.getMessage(), e);
		}
	}

	private static Map<String, RuleFactory> createFactories() {
		Map<String, RuleFactory> factories = new LinkedHashMap<>();
		factories.put("alwaysAllowed", (dto, context) -> PlanRules.alwaysAllowed());
		factories.put("neverAllowed", (dto, context) ->
				PlanRules.neverAllowed(GymSetup.requireText(
						stringParameter(dto, "reason", context), context, "reason")));
		factories.put("minutesPerDayAndDaysPerWeek", (dto, context) ->
				PlanRules.minutesPerDayAndDaysPerWeek(
						longParameter(dto, "minutesPerDay", context),
						intParameter(dto, "daysPerWeek", context)));
		factories.put("daysPerWeek", (dto, context) ->
				PlanRules.daysPerWeek(intParameter(dto, "maximumDays", context)));
		factories.put("usesPerDay", (dto, context) ->
				PlanRules.usesPerDay(longParameter(dto, "uses", context)));
		factories.put("usesPerWeek", (dto, context) ->
				PlanRules.usesPerWeek(longParameter(dto, "uses", context)));
		factories.put("usesPerMonth", (dto, context) ->
				PlanRules.usesPerMonth(longParameter(dto, "uses", context)));
		factories.put("classUsesPerWeek", (dto, context) ->
				PlanRules.classUsesPerWeek(intParameter(dto, "level", context),
						longParameter(dto, "maximumUses", context)));
		factories.put("showerUsesPerDay", (dto, context) ->
				PlanRules.showerUsesPerDay(longParameter(dto, "usesPerDay", context),
						minutes(dto, "initialMinutes", context),
						minutes(dto, "extraMinutes", context)));
		factories.put("minimumInterval", (dto, context) ->
				PlanRules.minimumInterval(minutes(dto, "intervalMinutes", context)));
		factories.put("onceEveryMonths", (dto, context) ->
				PlanRules.onceEveryMonths(intParameter(dto, "months", context)));
		factories.put("classLevel", (dto, context) ->
				PlanRules.classLevel(intParameter(dto, "maximumLevel", context)));
		factories.put("shower", (dto, context) ->
				PlanRules.shower(minutes(dto, "initialMinutes", context),
						minutes(dto, "extraMinutes", context)));
		factories.put("allOf", (dto, context) -> composite(dto, context, true));
		factories.put("anyOf", (dto, context) -> composite(dto, context, false));
		return Map.copyOf(factories);
	}

	private static PlanRule composite(GymData.RuleDto dto, String context, boolean all) {
		GymSetup.require(dto.rules, context, "rules");
		if (dto.rules.isEmpty())
			throw GymSetup.configurationError(context + " requires at least one nested rule");
		PlanRule[] rules = new PlanRule[dto.rules.size()];
		for (int index = 0; index < dto.rules.size(); index++) {
			String nestedContext = context + " rules[" + index + "]";
			rules[index] = create(GymSetup.require(dto.rules.get(index), nestedContext, "rule"), nestedContext);
		}
		return all ? PlanRules.allOf(rules) : PlanRules.anyOf(rules);
	}

	private static Duration minutes(GymData.RuleDto dto, String field, String context) {
		return Duration.ofMinutes(longParameter(dto, field, context));
	}

	private static String stringParameter(GymData.RuleDto dto, String field, String context) {
		JsonElement value = parameter(dto, field, context);
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
			throw GymSetup.configurationError(context + " parameter '" + field + "' must be a string");
		return value.getAsString();
	}

	private static long longParameter(GymData.RuleDto dto, String field, String context) {
		JsonElement value = parameter(dto, field, context);
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
			throw GymSetup.configurationError(context + " parameter '" + field + "' must be an integer");
		try {
			return new BigDecimal(value.getAsString()).longValueExact();
		} catch (NumberFormatException | ArithmeticException e) {
			throw GymSetup.configurationError(context + " parameter '" + field + "' must be an integer", e);
		}
	}

	private static int intParameter(GymData.RuleDto dto, String field, String context) {
		try {
			return Math.toIntExact(longParameter(dto, field, context));
		} catch (ArithmeticException e) {
			throw GymSetup.configurationError(context + " parameter '" + field + "' is outside the integer range", e);
		}
	}

	private static JsonElement parameter(GymData.RuleDto dto, String field, String context) {
		JsonElement value = dto.parameters == null ? null : dto.parameters.get(field);
		if (value == null || value.isJsonNull())
			throw GymSetup.configurationError(context + " is missing required parameter '" + field + "'");
		return value;
	}
}
