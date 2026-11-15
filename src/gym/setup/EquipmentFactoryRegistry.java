package gym.setup;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import gym.equipment.ClassEquipment;
import gym.equipment.DurationPolicies;
import gym.equipment.DurationPolicy;
import gym.equipment.Equipment;
import gym.equipment.EntryEquipment;
import gym.equipment.ExitEquipment;
import gym.equipment.StandardEquipment;
import gym.model.Service;

final class EquipmentFactoryRegistry {
	@FunctionalInterface
	private interface EquipmentFactory {
		Equipment create(GymData.EquipmentDto dto, Service service, String context);
	}

	@FunctionalInterface
	private interface DurationFactory {
		DurationPolicy create(GymData.EquipmentDto dto, String context);
	}

	private static final Map<String, EquipmentFactory> FACTORIES = createFactories();
	private static final Map<String, DurationFactory> DURATION_FACTORIES = createDurationFactories();

	private EquipmentFactoryRegistry() {
	}

	static Equipment create(GymData.EquipmentDto dto, Service service, String context) {
		String type = GymSetup.requireText(dto.type, context, "type");
		EquipmentFactory factory = FACTORIES.get(type);
		if (factory == null)
			throw GymSetup.configurationError(context + " has unknown equipment type '" + type + "'");
		try {
			GymSetup.requireText(dto.iconName, context, "iconName");
			GymSetup.require(dto.requiresInside, context, "requiresInside");
			return factory.create(dto, service, context);
		} catch (IllegalArgumentException e) {
			if (e.getMessage() != null && e.getMessage().startsWith("Gym configuration error:"))
				throw e;
			throw GymSetup.configurationError(context + ": " + e.getMessage(), e);
		} catch (ArithmeticException e) {
			throw GymSetup.configurationError(context + ": " + e.getMessage(), e);
		}
	}

	private static Map<String, EquipmentFactory> createFactories() {
		Map<String, EquipmentFactory> factories = new LinkedHashMap<>();
		factories.put("entry", (dto, service, context) ->
				new EntryEquipment(required(dto.id, context, "id"), dto.name, service,
						dto.iconName, fixedDuration(dto, context), dto.requiresInside));
		factories.put("exit", (dto, service, context) ->
				new ExitEquipment(required(dto.id, context, "id"), dto.name, service,
						dto.iconName, fixedDuration(dto, context), dto.requiresInside));
		factories.put("standard", (dto, service, context) ->
				new StandardEquipment(required(dto.id, context, "id"), dto.name, service,
						dto.iconName, durationPolicy(dto, context), dto.requiresInside));
		factories.put("class", (dto, service, context) ->
				new ClassEquipment(required(dto.id, context, "id"), dto.name, service,
						dto.iconName, required(dto.level, context, "level"),
						required(dto.capacity, context, "capacity"), fixedDuration(dto, context),
						dto.requiresInside));
		return Map.copyOf(factories);
	}

	private static Map<String, DurationFactory> createDurationFactories() {
		Map<String, DurationFactory> factories = new LinkedHashMap<>();
		factories.put("fixed", (dto, context) ->
				DurationPolicies.fixed(fixedDuration(dto, context)));
		factories.put("fromPlan", (dto, context) -> DurationPolicies.fromPlan());
		return Map.copyOf(factories);
	}

	private static DurationPolicy durationPolicy(GymData.EquipmentDto dto, String context) {
		GymData.DurationDto duration = GymSetup.require(dto.duration, context, "duration");
		String type = GymSetup.requireText(duration.type, context + " duration", "type");
		DurationFactory factory = DURATION_FACTORIES.get(type);
		if (factory == null)
			throw GymSetup.configurationError(context + " has unknown duration type '" + type + "'");
		return factory.create(dto, context + " duration");
	}

	private static Duration fixedDuration(GymData.EquipmentDto dto, String context) {
		GymData.DurationDto duration = GymSetup.require(dto.duration, context, "duration");
		String type = GymSetup.requireText(duration.type, context + " duration", "type");
		if (!"fixed".equals(type))
			throw GymSetup.configurationError(context + " requires a fixed duration, not '" + type + "'");
		Long seconds = GymSetup.require(duration.seconds, context + " duration", "seconds");
		return Duration.ofSeconds(seconds);
	}

	private static <T> T required(T value, String context, String field) {
		return GymSetup.require(value, context, field);
	}
}
