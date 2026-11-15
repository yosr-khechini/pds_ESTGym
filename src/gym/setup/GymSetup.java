package gym.setup;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gym.equipment.Equipment;
import gym.model.Gym;
import gym.model.Member;
import gym.model.Service;
import gym.plan.GymPlan;
import gym.plan.PlanRule;

public final class GymSetup {
	private static final Path DATA_FILE = Path.of("gym-data.json");

	private GymSetup() {
	}

	public static Gym createTestGym() {
		GymData data = GymDataReader.read(DATA_FILE);
		validateTopLevel(data);

		Gym gym = new Gym("ESTGym");
		Map<String, Service> services = loadServices(data, gym);
		Map<Integer, GymData.MemberDto> members = indexMembers(data);
		Map<Integer, GymData.PlanDto> plans = indexPlans(data, members);

		loadMembersWithPlans(gym, data, plans, services);
		loadEquipment(gym, data, services);

		return gym;
	}

	private static Map<String, Service> loadServices(GymData data, Gym gym) {
		Map<String, Service> services = new HashMap<>();
		for (int index = 0; index < data.services.size(); index++) {
			String context = "services[" + index + "]";
			GymData.ServiceDto dto = ConfigValidation.require(data.services.get(index), context, "service");

			Service service = createService(dto, context);

			if (services.putIfAbsent(dto.id, service) != null)
				throw ConfigValidation.configurationError(context + " duplicates service '" + dto.id + "'");

			gym.addService(service);
		}

		return services;
	}

	private static Service createService(GymData.ServiceDto dto, String context) {
		ConfigValidation.requireText(dto.id, context, "id");
		ConfigValidation.requireText(dto.name, context, "name");

		try {
			return new Service(dto.id, dto.name, dto.classLevel == null ? 0 : dto.classLevel);
		} catch (IllegalArgumentException e) {
			throw ConfigValidation.configurationError(context + " service '" + dto.id + "': " + e.getMessage(), e);
		}
	}

	private static Map<Integer, GymData.MemberDto> indexMembers(GymData data) {
		Map<Integer, GymData.MemberDto> members = new HashMap<>();

		for (int index = 0; index < data.members.size(); index++)
			indexMember(data.members.get(index), index, members);

		return members;
	}

	private static void indexMember(GymData.MemberDto dto, int index, Map<Integer, GymData.MemberDto> members) {
		String context = "members[" + index + "]";
		ConfigValidation.require(dto, context, "member");
		ConfigValidation.require(dto.id, context, "id");
		ConfigValidation.requireText(dto.name, context, "name");

		if (dto.id <= 0)
			throw ConfigValidation.configurationError(context + " member '" + dto.name + "' must have a positive ID");

		if (members.putIfAbsent(dto.id, dto) != null)
			throw ConfigValidation.configurationError(context + " duplicates member ID " + dto.id);
	}

	private static Map<Integer, GymData.PlanDto> indexPlans(GymData data,
			Map<Integer, GymData.MemberDto> members) {
		Map<Integer, GymData.PlanDto> plans = new HashMap<>();

		for (int index = 0; index < data.plans.size(); index++)
			indexPlan(data.plans.get(index), index, members, plans);

		return plans;
	}

	private static void indexPlan(GymData.PlanDto dto, int index, Map<Integer, GymData.MemberDto> members,
			Map<Integer, GymData.PlanDto> plans) {
		String context = "plans[" + index + "]";
		ConfigValidation.require(dto, context, "plan");
		ConfigValidation.require(dto.memberId, context, "memberId");
		GymData.MemberDto member = members.get(dto.memberId);
		if (member == null)
			throw ConfigValidation.configurationError(context + " references unknown member ID " + dto.memberId);

		if (plans.putIfAbsent(dto.memberId, dto) != null)
			throw ConfigValidation.configurationError(context + " duplicates the plan for member '" + member.name + "'");

		ConfigValidation.require(dto.rules, context + " for member '" + member.name + "'", "rules");
	}

	private static void loadMembersWithPlans(Gym gym, GymData data, Map<Integer, GymData.PlanDto> plans,
			Map<String, Service> services) {
		for (GymData.MemberDto member : data.members)
			loadMemberWithPlan(gym, member, plans.get(member.id), services);
	}

	private static void loadMemberWithPlan(Gym gym, GymData.MemberDto memberDto, GymData.PlanDto planDto,
			Map<String, Service> services) {
		if (planDto == null)
			throw ConfigValidation.configurationError("Member '" + memberDto.name + "' (ID " + memberDto.id
					+ ") has no plan in plans");

		GymPlan plan = buildPlan(memberDto, planDto, services);

		gym.addPlan(plan);
		gym.addMember(new Member(memberDto.id, memberDto.name, "CARD-" + memberDto.id, plan));
	}

	private static GymPlan buildPlan(GymData.MemberDto member, GymData.PlanDto planDto,
			Map<String, Service> services) {
		GymPlan plan = new GymPlan(member.name);
		Set<String> ruleServices = new HashSet<>();

		for (int index = 0; index < planDto.rules.size(); index++)
			addPlanRule(plan, member, planDto.rules.get(index), index, services, ruleServices);

		return plan;
	}

	private static void addPlanRule(GymPlan plan, GymData.MemberDto member, GymData.RuleDto ruleDto, int ruleIndex,
			Map<String, Service> services, Set<String> ruleServices) {
		String context = "member '" + member.name + "' (ID " + member.id + "), plans rule[" + ruleIndex + "]";
		ConfigValidation.require(ruleDto, context, "rule");
		ConfigValidation.requireText(ruleDto.service, context, "service");

		Service service = services.get(ruleDto.service);
		if (service == null)
			throw ConfigValidation.configurationError(context + " references unknown service '" + ruleDto.service + "'");

		if (!ruleServices.add(ruleDto.service))
			throw ConfigValidation.configurationError(context + " duplicates service rule '" + ruleDto.service + "'");

		PlanRule rule = RuleFactoryRegistry.create(ruleDto, context + ", service '" + ruleDto.service + "'");

		plan.addRule(service, rule);
	}

	private static void loadEquipment(Gym gym, GymData data, Map<String, Service> services) {
		Set<Integer> equipmentIds = new HashSet<>();

		for (int index = 0; index < data.equipment.size(); index++)
			loadEquipmentItem(gym, data.equipment.get(index), index, services, equipmentIds);
	}

	private static void loadEquipmentItem(Gym gym, GymData.EquipmentDto dto, int index,
			Map<String, Service> services, Set<Integer> equipmentIds) {
		String context = "equipment[" + index + "]";
		ConfigValidation.require(dto, context, "equipment");
		ConfigValidation.require(dto.id, context, "id");
		ConfigValidation.requireText(dto.name, context, "name");
		ConfigValidation.requireText(dto.service, context, "service");

		if (!equipmentIds.add(dto.id))
			throw ConfigValidation.configurationError(context + " equipment '" + dto.name
					+ "' duplicates equipment ID " + dto.id);

		Service service = services.get(dto.service);
		if (service == null)
			throw ConfigValidation.configurationError(context + " equipment '" + dto.name
					+ "' references unknown service '" + dto.service + "'");

		Equipment equipment = EquipmentFactoryRegistry.create(dto, service, context + " equipment '" + dto.name + "'");

		gym.addEquipment(equipment);
	}

	private static void validateTopLevel(GymData data) {
		ConfigValidation.require(data.members, "root", "members");
		ConfigValidation.require(data.services, "root", "services");
		ConfigValidation.require(data.equipment, "root", "equipment");
		ConfigValidation.require(data.plans, "root", "plans");
	}
}
