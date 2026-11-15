package gym.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import gym.equipment.Equipment;
import gym.plan.GymPlan;
import gym.plan.PlanRule;
import gym.plan.ShowerContinuation;
import pds.util.SimulatedClock;

public final class Gym {
	private final String name;
	private final Map<String, Service> services = new LinkedHashMap<>();
	private final Map<String, GymPlan> plans = new LinkedHashMap<>();
	private final Map<Integer, Member> members = new LinkedHashMap<>();
	private final Map<Integer, Equipment> equipment = new LinkedHashMap<>();

	public Gym(String name) {
		this.name = Objects.requireNonNull(name);
	}

	public String getName() {
		return name;
	}

	public void addService(Service service) {
		services.put(Objects.requireNonNull(service).getId(), service);
	}

	public void addPlan(GymPlan plan) {
		plans.put(Objects.requireNonNull(plan).getName(), plan);
	}

	public void addMember(Member member) {
		Objects.requireNonNull(member);
		if (plans.get(member.getPlan().getName()) != member.getPlan())
			throw new IllegalArgumentException("Member plan must belong to this gym");
		members.put(member.getId(), member);
	}

	public void addEquipment(Equipment item) {
		Objects.requireNonNull(item);
		Service registered = services.get(item.getService().getId());
		if (registered == null || !registered.equals(item.getService()))
			throw new IllegalArgumentException("Equipment service must belong to this gym");
		equipment.put(item.getId(), item);
	}

	public Member getMember(int memberId) {
		return members.get(memberId);
	}

	public Equipment getEquipment(int equipmentId) {
		return equipment.get(equipmentId);
	}

	public Collection<Member> getMembers() {
		return Collections.unmodifiableCollection(members.values());
	}

	public Collection<Service> getServices() {
		return Collections.unmodifiableCollection(services.values());
	}

	public Collection<GymPlan> getPlans() {
		return Collections.unmodifiableCollection(plans.values());
	}

	public Collection<Equipment> getEquipment() {
		return Collections.unmodifiableCollection(equipment.values());
	}

	public AccessResult accessEquipment(int equipmentId, int memberId) {
		return accessEquipment(equipmentId, memberId, SimulatedClock.getCurrentTime());
	}

	public synchronized AccessResult accessEquipment(int equipmentId, int memberId, LocalDateTime now) {
		Objects.requireNonNull(now);
		Equipment item = equipment.get(equipmentId);
		if (item == null)
			return AccessResult.denied("Unknown equipment");
		Member member = members.get(memberId);
		if (member == null)
			return AccessResult.denied("Unknown member");
		if (!item.isAvailable(member, now))
			return AccessResult.denied("Equipment is busy or unavailable");
		if (item.requiresMemberInside() && !member.isInside())
			return AccessResult.denied("The member must be inside the gym");
		if (member.isUsing(now))
			return AccessResult.denied("The member is already using another service");
		String memberFailure = item.memberValidationFailure(member);
		if (memberFailure != null)
			return AccessResult.denied(memberFailure);
		PlanRule rule = member.getPlan().getRule(item.getService());
		if (rule == null)
			return AccessResult.denied("The service is not included in the member's plan");
		ShowerContinuation showerContinuation = rule.getShowerContinuation().orElse(null);
		boolean activatingShowerExtra = showerContinuation != null
				&& member.canActivateShowerExtra(equipmentId, now);
		Duration duration;
		if (activatingShowerExtra) {
			duration = member.getShowerExtraDuration(equipmentId, now);
		} else {
			java.util.Optional<Duration> availableDuration = item.getDuration(member, now);
			if (!availableDuration.isPresent())
				return AccessResult.denied("No valid duration is defined for this service");
			duration = availableDuration.get();
		}
		if (duration.isZero() || duration.isNegative())
			return AccessResult.denied("No valid duration is defined for this service");
		PlanRule.Decision decision = member.checkPlan(item.getService(), now, duration);
		if (!decision.isAllowed())
			return AccessResult.denied(decision.getReason());
		Duration actualDuration = item.occupy(member, now, duration);
		if (activatingShowerExtra) {
			member.activateShowerExtra(item.getName(), now);
		} else {
			member.beginUse(item.getService(), item.getName(), now, actualDuration);
			if (showerContinuation != null)
				member.scheduleShowerExtra(equipmentId, item.getService(), now.plus(actualDuration),
						showerContinuation);
		}
		item.completeUse(member);
		return AccessResult.allowed(actualDuration);
	}
}
