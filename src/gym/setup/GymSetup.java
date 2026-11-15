package gym.setup;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import gym.equipment.Equipment;
import gym.model.Gym;
import gym.model.Member;
import gym.model.Service;
import gym.plan.GymPlan;
import gym.plan.PlanRule;

public final class GymSetup {
	private static final Path DATA_FILE = Path.of("gym-data.json");
	private static final Gson GSON = new Gson();

	private GymSetup() {
	}

	public static Gym createTestGym() {
		GymData data = readData();
		validateTopLevel(data);

		Gym gym = new Gym("ESTGym");
		Map<String, Service> services = new HashMap<>();
		for (int index = 0; index < data.services.size(); index++) {
			String context = "services[" + index + "]";
			GymData.ServiceDto dto = require(data.services.get(index), context, "service");
			requireText(dto.id, context, "id");
			requireText(dto.name, context, "name");
			Service service;
			try {
				service = new Service(dto.id, dto.name, dto.classLevel == null ? 0 : dto.classLevel);
			} catch (IllegalArgumentException e) {
				throw configurationError(context + " service '" + dto.id + "': " + e.getMessage(), e);
			}
			if (services.putIfAbsent(dto.id, service) != null)
				throw configurationError(context + " duplicates service '" + dto.id + "'");
			gym.addService(service);
		}

		Map<Integer, GymData.MemberDto> memberData = new HashMap<>();
		for (int index = 0; index < data.members.size(); index++) {
			String context = "members[" + index + "]";
			GymData.MemberDto dto = require(data.members.get(index), context, "member");
			require(dto.id, context, "id");
			requireText(dto.name, context, "name");
			if (dto.id <= 0)
				throw configurationError(context + " member '" + dto.name + "' must have a positive ID");
			if (memberData.putIfAbsent(dto.id, dto) != null)
				throw configurationError(context + " duplicates member ID " + dto.id);
		}

		Map<Integer, GymData.PlanDto> plans = new HashMap<>();
		for (int index = 0; index < data.plans.size(); index++) {
			String context = "plans[" + index + "]";
			GymData.PlanDto dto = require(data.plans.get(index), context, "plan");
			require(dto.memberId, context, "memberId");
			GymData.MemberDto member = memberData.get(dto.memberId);
			if (member == null)
				throw configurationError(context + " references unknown member ID " + dto.memberId);
			if (plans.putIfAbsent(dto.memberId, dto) != null)
				throw configurationError(context + " duplicates the plan for member '" + member.name + "'");
			require(dto.rules, context + " for member '" + member.name + "'", "rules");
		}

		for (GymData.MemberDto memberDto : data.members) {
			GymData.PlanDto planDto = plans.get(memberDto.id);
			if (planDto == null)
				throw configurationError("Member '" + memberDto.name + "' (ID " + memberDto.id
						+ ") has no plan in plans");
			GymPlan plan = new GymPlan(memberDto.name);
			Set<String> ruleServices = new HashSet<>();
			for (int ruleIndex = 0; ruleIndex < planDto.rules.size(); ruleIndex++) {
				String context = "member '" + memberDto.name + "' (ID " + memberDto.id
						+ "), plans rule[" + ruleIndex + "]";
				GymData.RuleDto ruleDto = require(planDto.rules.get(ruleIndex), context, "rule");
				requireText(ruleDto.service, context, "service");
				Service service = services.get(ruleDto.service);
				if (service == null)
					throw configurationError(context + " references unknown service '" + ruleDto.service + "'");
				if (!ruleServices.add(ruleDto.service))
					throw configurationError(context + " duplicates service rule '" + ruleDto.service + "'");
				PlanRule rule = RuleFactoryRegistry.create(ruleDto,
						context + ", service '" + ruleDto.service + "'");
				plan.addRule(service, rule);
			}
			gym.addPlan(plan);
			gym.addMember(new Member(memberDto.id, memberDto.name, "CARD-" + memberDto.id, plan));
		}

		Set<Integer> equipmentIds = new HashSet<>();
		for (int index = 0; index < data.equipment.size(); index++) {
			String context = "equipment[" + index + "]";
			GymData.EquipmentDto dto = require(data.equipment.get(index), context, "equipment");
			require(dto.id, context, "id");
			requireText(dto.name, context, "name");
			requireText(dto.service, context, "service");
			if (!equipmentIds.add(dto.id))
				throw configurationError(context + " equipment '" + dto.name
						+ "' duplicates equipment ID " + dto.id);
			Service service = services.get(dto.service);
			if (service == null)
				throw configurationError(context + " equipment '" + dto.name
						+ "' references unknown service '" + dto.service + "'");
			Equipment equipment = EquipmentFactoryRegistry.create(dto, service,
					context + " equipment '" + dto.name + "'");
			gym.addEquipment(equipment);
		}

		return gym;
	}

	private static GymData readData() {
		if (!Files.isRegularFile(DATA_FILE))
			throw configurationError("Gym data file not found: " + DATA_FILE.toAbsolutePath()
					+ ". Run the application with the project root as its working directory.");
		try (Reader reader = Files.newBufferedReader(DATA_FILE)) {
			GymData data = GSON.fromJson(reader, GymData.class);
			if (data == null)
				throw configurationError("Gym data file is empty: " + DATA_FILE.toAbsolutePath());
			return data;
		} catch (JsonParseException e) {
			throw configurationError("Invalid JSON in " + DATA_FILE + ": " + e.getMessage(), e);
		} catch (IOException e) {
			throw configurationError("Could not read gym data file " + DATA_FILE + ": " + e.getMessage(), e);
		}
	}

	private static void validateTopLevel(GymData data) {
		require(data.members, "root", "members");
		require(data.services, "root", "services");
		require(data.equipment, "root", "equipment");
		require(data.plans, "root", "plans");
	}

	static <T> T require(T value, String context, String field) {
		if (value == null)
			throw configurationError(context + " is missing required field '" + field + "'");
		return value;
	}

	static String requireText(String value, String context, String field) {
		require(value, context, field);
		if (value.isBlank())
			throw configurationError(context + " has an empty required field '" + field + "'");
		return value;
	}

	static IllegalArgumentException configurationError(String message) {
		return new IllegalArgumentException("Gym configuration error: " + message);
	}

	static IllegalArgumentException configurationError(String message, Throwable cause) {
		return new IllegalArgumentException("Gym configuration error: " + message, cause);
	}
}
