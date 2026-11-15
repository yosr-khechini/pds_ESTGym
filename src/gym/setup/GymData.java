package gym.setup;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

public final class GymData {
	List<MemberDto> members;
	List<ServiceDto> services;
	List<EquipmentDto> equipment;
	List<PlanDto> plans;

	static final class MemberDto {
		Integer id;
		String name;
	}

	static final class ServiceDto {
		String id;
		String name;
		Integer classLevel;
	}

	static final class PlanDto {
		Integer memberId;
		List<RuleDto> rules;
	}

	static final class RuleDto {
		String service;
		String type;
		Map<String, JsonElement> parameters;
		List<RuleDto> rules;
	}

	static final class EquipmentDto {
		Integer id;
		String name;
		String type;
		String service;
		String iconName;
		Boolean requiresInside;
		DurationDto duration;
		Integer capacity;
		Integer level;
	}

	static final class DurationDto {
		String type;
		Long seconds;
	}
}
