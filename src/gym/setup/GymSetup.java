package gym.setup;

import java.time.Duration;

import gym.equipment.ClassEquipment;
import gym.equipment.DurationPolicies;
import gym.equipment.EntryEquipment;
import gym.equipment.ExitEquipment;
import gym.equipment.StandardEquipment;
import gym.model.Gym;
import gym.model.Member;
import gym.model.Service;
import gym.plan.GymPlan;
import gym.plan.PlanRule;
import gym.plan.PlanRules;

public final class GymSetup {
	private GymSetup() {
	}

	public static Gym createTestGym() {
		Gym gym = new Gym("ESTGym");

		Service entry = service(gym, "entry", "Entry");
		Service exit = service(gym, "exit", "Exit");
		Service step = service(gym, "step", "Step class", 1);
		Service cycling = service(gym, "cycling", "Cycling class", 1);
		Service bodyCombat = service(gym, "body-combat", "Body combat class", 2);
		Service yoga = service(gym, "yoga", "Ioga class", 2);
		Service pool = service(gym, "pool", "Swimming pool");
		Service shower = service(gym, "shower", "Shower");
		Service drink = service(gym, "vitamin-drink", "Vitamin drink");
		Service trainer = service(gym, "personal-trainer", "Personal trainer");
		Service nutrition = service(gym, "nutrition", "Nutrition consultation");
		Service evaluation = service(gym, "evaluation", "Physical evaluation");
		Service massage = service(gym, "massage-chair", "Massage chair");

		GymPlan vitorPlan = createPlan("Vítor Virtual");
		addEntryRule(vitorPlan, PlanRules.minutesPerDayAndDaysPerWeek(60, 3));
		addExitRule(vitorPlan);
		addClassRules(vitorPlan, classLimit(1, 1), classLimit(1, 1), noAccess(), noAccess());
		addPoolRule(vitorPlan, noAccess());
		addShowerRule(vitorPlan, 1, 3, 2);
		addDrinkRule(vitorPlan, 1, null);
		addTrainerRule(vitorPlan, noAccess());
		addPeriodicRules(vitorPlan, 6, 3);
		addMassageRule(vitorPlan, noAccess());

		GymPlan irisPlan = createPlan("Iris Irreal");
		addEntryRule(irisPlan, PlanRules.daysPerWeek(3));
		addExitRule(irisPlan);
		addClassRules(irisPlan, noAccess(), noAccess(), noAccess(), noAccess());
		addPoolRule(irisPlan, PlanRules.minutesPerDayAndDaysPerWeek(45, 2));
		addShowerRule(irisPlan, 1, 5, 2);
		addDrinkRule(irisPlan, 2, Duration.ofMinutes(30));
		addTrainerRule(irisPlan, PlanRules.minutesPerDayAndDaysPerWeek(45, 3));
		addPeriodicRules(irisPlan, 6, 3);
		addMassageRule(irisPlan, noAccess());

		GymPlan inesPlan = createPlan("Inês Inexistente");
		addEntryRule(inesPlan, alwaysAllowed());
		addExitRule(inesPlan);
		addClassRules(inesPlan, classLimit(1, 1), classLimit(1, 1), noAccess(), noAccess());
		addPoolRule(inesPlan, noAccess());
		addShowerRule(inesPlan, 1, 5, 2);
		addDrinkRule(inesPlan, 2, Duration.ofMinutes(30));
		addTrainerRule(inesPlan, noAccess());
		addPeriodicRules(inesPlan, 1, 3);
		addMassageRule(inesPlan, noAccess());

		GymPlan filipePlan = createPlan("Filipe Fictício");
		addEntryRule(filipePlan, PlanRules.daysPerWeek(3));
		addExitRule(filipePlan);
		addClassRules(filipePlan, classLimit(1, 2), classLimit(1, 2),
				classLimit(2, 1), classLimit(2, 1));
		addPoolRule(filipePlan, PlanRules.minutesPerDayAndDaysPerWeek(45, 2));
		addShowerRule(filipePlan, 1, 5, 2);
		addDrinkRule(filipePlan, 2, Duration.ofMinutes(30));
		addTrainerRule(filipePlan, PlanRules.minutesPerDayAndDaysPerWeek(45, 3));
		addPeriodicRules(filipePlan, 6, 3);
		addMassageRule(filipePlan, limitedMassage(3));

		GymPlan diogoPlan = createPlan("Diogo Digital");
		addEntryRule(diogoPlan, alwaysAllowed());
		addExitRule(diogoPlan);
		addClassRules(diogoPlan, classLevel(1), classLevel(1), classLimit(2, 3), classLimit(2, 3));
		addPoolRule(diogoPlan, PlanRules.minutesPerDayAndDaysPerWeek(60, 5));
		addShowerRule(diogoPlan, 2, 6, 3);
		addDrinkRule(diogoPlan, 4, Duration.ofMinutes(20));
		addTrainerRule(diogoPlan, noAccess());
		addPeriodicRules(diogoPlan, 3, 3);
		addMassageRule(diogoPlan, limitedMassage(7));

		GymPlan beatrizPlan = createPlan("Beatriz Bytes");
		addEntryRule(beatrizPlan, alwaysAllowed());
		addExitRule(beatrizPlan);
		addClassRules(beatrizPlan, classLevel(2), classLevel(2), classLevel(2), classLevel(2));
		addPoolRule(beatrizPlan, noAccess());
		addShowerRule(beatrizPlan, 2, 6, 3);
		addDrinkRule(beatrizPlan, 4, Duration.ofMinutes(20));
		addTrainerRule(beatrizPlan, PlanRules.minutesPerDayAndDaysPerWeek(60, 5));
		addPeriodicRules(beatrizPlan, 1, 1);
		addMassageRule(beatrizPlan, limitedMassage(3));

		addMember(gym, 1, "Vítor Virtual", vitorPlan);
		addMember(gym, 2, "Iris Irreal", irisPlan);
		addMember(gym, 3, "Inês Inexistente", inesPlan);
		addMember(gym, 4, "Filipe Fictício", filipePlan);
		addMember(gym, 5, "Diogo Digital", diogoPlan);
		addMember(gym, 6, "Beatriz Bytes", beatrizPlan);

		gym.addEquipment(new EntryEquipment(1, "Entry door", entry, "porta", Duration.ofSeconds(3)));
		gym.addEquipment(new ExitEquipment(2, "Exit door", exit, "porta", Duration.ofSeconds(3)));
		gym.addEquipment(new ClassEquipment(3, "Step class", step, "aula", 1, 30, Duration.ofMinutes(30)));
		gym.addEquipment(new ClassEquipment(4, "Cycling class", cycling, "aula", 1, 25, Duration.ofMinutes(50)));
		gym.addEquipment(new ClassEquipment(5, "Body combat class", bodyCombat, "aula", 2, 20, Duration.ofMinutes(30)));
		gym.addEquipment(new ClassEquipment(6, "Ioga class", yoga, "aula", 2, 15, Duration.ofMinutes(60)));
		gym.addEquipment(new StandardEquipment(7, "Beverage dispenser", drink, "bebida",
				DurationPolicies.fixed(Duration.ofSeconds(20)), true));
		gym.addEquipment(new StandardEquipment(8, "Pool entry", pool, "porta_piscina",
				DurationPolicies.fixed(Duration.ofSeconds(3)), true));
		addPlanDurationEquipment(gym, shower, 9, "Shower 1", "duche");
		addPlanDurationEquipment(gym, shower, 10, "Shower 2", "duche");
		addPlanDurationEquipment(gym, shower, 11, "Shower 3", "duche");
		addPlanDurationEquipment(gym, trainer, 12, "Manel Mandão", "pt");
		addPlanDurationEquipment(gym, trainer, 13, "Rita Rígida", "pt");
		gym.addEquipment(new StandardEquipment(14, "Carlos Comilão", nutrition, "nutricionista",
				DurationPolicies.fixed(Duration.ofMinutes(30)), true));
		gym.addEquipment(new StandardEquipment(15, "Marta Magra", nutrition, "nutricionista",
				DurationPolicies.fixed(Duration.ofMinutes(30)), true));
		gym.addEquipment(new StandardEquipment(16, "Evaluation room", evaluation, "fisica",
				DurationPolicies.fixed(Duration.ofMinutes(20)), true));
		gym.addEquipment(new StandardEquipment(17, "Massage chair 1", massage, "poltrona",
				DurationPolicies.fixed(Duration.ofMinutes(10)), true));
		gym.addEquipment(new StandardEquipment(18, "Massage chair 2", massage, "poltrona",
				DurationPolicies.fixed(Duration.ofMinutes(10)), true));

		return gym;
	}

	private static GymPlan createPlan(String name) {
		return new GymPlan(name);
	}

	private static void addMember(Gym gym, int id, String name, GymPlan plan) {
		gym.addPlan(plan);
		gym.addMember(new Member(id, name, "CARD-" + id, plan));
	}

	private static void addEntryRule(GymPlan plan, PlanRule rule) {
		plan.addRule("entry", rule);
	}

	private static void addExitRule(GymPlan plan) {
		plan.addRule("exit", alwaysAllowed());
	}

	private static void addClassRules(GymPlan plan, PlanRule step, PlanRule cycling, PlanRule bodyCombat,
			PlanRule yoga) {
		plan.addRule("step", step);
		plan.addRule("cycling", cycling);
		plan.addRule("body-combat", bodyCombat);
		plan.addRule("yoga", yoga);
	}

	private static void addPoolRule(GymPlan plan, PlanRule rule) {
		plan.addRule("pool", rule);
	}

	private static void addShowerRule(GymPlan plan, long usesPerDay, long initialMinutes, long extraMinutes) {
		Duration initialDuration = Duration.ofMinutes(initialMinutes);
		Duration extraDuration = Duration.ofMinutes(extraMinutes);
		plan.addRule("shower", PlanRules.allOf(
				PlanRules.showerUsesPerDay(usesPerDay, initialDuration, extraDuration),
				PlanRules.shower(initialDuration, extraDuration)));
	}

	private static void addDrinkRule(GymPlan plan, long usesPerDay, Duration minimumInterval) {
		PlanRule rule = PlanRules.usesPerDay(usesPerDay);
		if (minimumInterval != null)
			rule = PlanRules.allOf(rule, PlanRules.minimumInterval(minimumInterval));
		plan.addRule("vitamin-drink", rule);
	}

	private static void addTrainerRule(GymPlan plan, PlanRule rule) {
		plan.addRule("personal-trainer", rule);
	}

	private static void addPeriodicRules(GymPlan plan, int nutritionMonths, int evaluationMonths) {
		plan.addRule("nutrition", PlanRules.onceEveryMonths(nutritionMonths));
		plan.addRule("evaluation", PlanRules.onceEveryMonths(evaluationMonths));
	}

	private static void addMassageRule(GymPlan plan, PlanRule rule) {
		plan.addRule("massage-chair", rule);
	}

	private static PlanRule limitedMassage(long usesPerWeek) {
		return PlanRules.allOf(PlanRules.usesPerDay(1), PlanRules.usesPerWeek(usesPerWeek));
	}

	private static PlanRule classLimit(int level, long usesPerWeek) {
		return PlanRules.allOf(PlanRules.classLevel(level), PlanRules.classUsesPerWeek(level, usesPerWeek));
	}

	private static PlanRule classLevel(int maximumLevel) {
		return PlanRules.classLevel(maximumLevel);
	}

	private static PlanRule alwaysAllowed() {
		return PlanRules.alwaysAllowed();
	}

	private static PlanRule noAccess() {
		return PlanRules.neverAllowed("This service is not included in the member's plan");
	}

	private static void addPlanDurationEquipment(Gym gym, Service service, int id, String name, String iconName) {
		gym.addEquipment(new StandardEquipment(id, name, service, iconName, DurationPolicies.fromPlan(), true));
	}

	private static Service service(Gym gym, String id, String name) {
		return service(gym, id, name, 0);
	}

	private static Service service(Gym gym, String id, String name, int classLevel) {
		Service service = new Service(id, name, classLevel);
		gym.addService(service);
		return service;
	}
}
