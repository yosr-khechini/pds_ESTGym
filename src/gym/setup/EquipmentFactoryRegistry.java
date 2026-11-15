package gym.setup;

import java.time.Duration;
import java.util.Map;

import static gym.setup.ConfigValidation.configurationError;
import static gym.setup.ConfigValidation.require;
import static gym.setup.ConfigValidation.requireText;

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
		Equipment create(GymData.EquipmentDto equipmentData, Service service, String errorContext);
	}

	@FunctionalInterface
	private interface DurationFactory {
		DurationPolicy create(GymData.EquipmentDto equipmentData, String errorContext);
	}

	private static final Map<String, EquipmentFactory> FACTORIES = Map.of(
			"entry", EquipmentFactoryRegistry::createEntry,
			"exit", EquipmentFactoryRegistry::createExit,
			"standard", EquipmentFactoryRegistry::createStandard,
			"class", EquipmentFactoryRegistry::createClass);
	private static final Map<String, DurationFactory> DURATION_FACTORIES = Map.of(
			"fixed", EquipmentFactoryRegistry::createFixedDurationPolicy,
			"fromPlan", EquipmentFactoryRegistry::createPlanDurationPolicy);

	private EquipmentFactoryRegistry() {
	}

	static Equipment create(GymData.EquipmentDto equipmentData, Service service, String errorContext) {
		String type = requireText(equipmentData.type, errorContext, "type");
		EquipmentFactory factory = FACTORIES.get(type);
		if (factory == null)
			throw configurationError(errorContext + " has unknown equipment type '" + type + "'");
		return createWithErrorHandling(factory, equipmentData, service, errorContext);
	}

	private static Equipment createWithErrorHandling(EquipmentFactory factory,
			GymData.EquipmentDto equipmentData, Service service, String errorContext) {
		try {
			requireText(equipmentData.iconName, errorContext, "iconName");
			require(equipmentData.requiresInside, errorContext, "requiresInside");
			return factory.create(equipmentData, service, errorContext);
		} catch (IllegalArgumentException e) {
			throw wrapFactoryError(errorContext, e);
		} catch (ArithmeticException e) {
			throw wrapFactoryError(errorContext, e);
		}
	}

	private static IllegalArgumentException wrapFactoryError(String errorContext, RuntimeException error) {
		if (error instanceof IllegalArgumentException configurationError
				&& configurationError.getMessage() != null
				&& configurationError.getMessage().startsWith("Gym configuration error:"))
			return configurationError;
		return configurationError(errorContext + ": " + error.getMessage(), error);
	}

	private static Equipment createEntry(GymData.EquipmentDto equipmentData, Service service, String errorContext) {
		return new EntryEquipment(requireId(equipmentData, errorContext), equipmentData.name, service,
				equipmentData.iconName, fixedDuration(equipmentData, errorContext), equipmentData.requiresInside);
	}

	private static Equipment createExit(GymData.EquipmentDto equipmentData, Service service, String errorContext) {
		return new ExitEquipment(requireId(equipmentData, errorContext), equipmentData.name, service,
				equipmentData.iconName, fixedDuration(equipmentData, errorContext), equipmentData.requiresInside);
	}

	private static Equipment createStandard(GymData.EquipmentDto equipmentData, Service service,
			String errorContext) {
		return new StandardEquipment(requireId(equipmentData, errorContext), equipmentData.name, service,
				equipmentData.iconName, durationPolicy(equipmentData, errorContext), equipmentData.requiresInside);
	}

	private static Equipment createClass(GymData.EquipmentDto equipmentData, Service service, String errorContext) {
		return new ClassEquipment(requireId(equipmentData, errorContext), equipmentData.name, service,
				equipmentData.iconName, requireLevel(equipmentData, errorContext),
				requireCapacity(equipmentData, errorContext), fixedDuration(equipmentData, errorContext),
				equipmentData.requiresInside);
	}

	private static int requireId(GymData.EquipmentDto equipmentData, String errorContext) {
		return require(equipmentData.id, errorContext, "id");
	}

	private static int requireLevel(GymData.EquipmentDto equipmentData, String errorContext) {
		return require(equipmentData.level, errorContext, "level");
	}

	private static int requireCapacity(GymData.EquipmentDto equipmentData, String errorContext) {
		return require(equipmentData.capacity, errorContext, "capacity");
	}

	private static DurationPolicy durationPolicy(GymData.EquipmentDto equipmentData, String errorContext) {
		GymData.DurationDto duration = require(equipmentData.duration, errorContext, "duration");
		String type = requireText(duration.type, errorContext + " duration", "type");
		DurationFactory factory = DURATION_FACTORIES.get(type);
		if (factory == null)
			throw configurationError(errorContext + " has unknown duration type '" + type + "'");
		return factory.create(equipmentData, errorContext + " duration");
	}

	private static DurationPolicy createFixedDurationPolicy(GymData.EquipmentDto equipmentData,
			String errorContext) {
		return DurationPolicies.fixed(fixedDuration(equipmentData, errorContext));
	}

	private static DurationPolicy createPlanDurationPolicy(GymData.EquipmentDto equipmentData, String errorContext) {
		return DurationPolicies.fromPlan();
	}

	private static Duration fixedDuration(GymData.EquipmentDto equipmentData, String errorContext) {
		GymData.DurationDto duration = require(equipmentData.duration, errorContext, "duration");
		String type = requireText(duration.type, errorContext + " duration", "type");
		if (!"fixed".equals(type))
			throw configurationError(errorContext + " requires a fixed duration, not '" + type + "'");
		Long seconds = require(duration.seconds, errorContext + " duration", "seconds");
		return Duration.ofSeconds(seconds);
	}
}
