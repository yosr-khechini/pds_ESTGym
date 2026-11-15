package gym.plan;

import gym.model.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class GymPlan {
	private final String name;
	private final Map<String, PlanRule> rules = new LinkedHashMap<>();

	public GymPlan(String name) {
		this.name = Objects.requireNonNull(name);
	}

	public GymPlan addRule(Service service, PlanRule rule) {
		return addRule(Objects.requireNonNull(service).getId(), rule);
	}

	public GymPlan addRule(String serviceId, PlanRule rule) {
		rules.put(Objects.requireNonNull(serviceId), Objects.requireNonNull(rule));
		return this;
	}

	public PlanRule getRule(Service service) {
		return rules.get(service.getId());
	}

	public Map<String, PlanRule> getRules() {
		return Collections.unmodifiableMap(rules);
	}

	public String getName() {
		return name;
	}
}
