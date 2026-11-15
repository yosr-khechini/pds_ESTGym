package gym.equipment;

import java.time.Duration;

import gym.model.Member;
import gym.model.Service;

public final class ExitEquipment extends StandardEquipment {
	public ExitEquipment(int id, String name, Service service, String iconName, Duration duration) {
		this(id, name, service, iconName, duration, true);
	}

	public ExitEquipment(int id, String name, Service service, String iconName, Duration duration,
			boolean requiresMemberInside) {
		super(id, name, service, iconName, DurationPolicies.fixed(duration), requiresMemberInside);
	}

	@Override
	protected String validateMember(Member member) {
		return member.isInside() ? null : "The member is not inside the gym";
	}

	@Override
	protected void onSuccessfulUse(Member member) {
		member.setInside(false);
	}
}
