package gym.equipment;

import java.time.Duration;

import gym.model.Member;
import gym.model.Service;

public final class EntryEquipment extends StandardEquipment {
	public EntryEquipment(int id, String name, Service service, String iconName, Duration duration) {
		this(id, name, service, iconName, duration, false);
	}

	public EntryEquipment(int id, String name, Service service, String iconName, Duration duration,
			boolean requiresMemberInside) {
		super(id, name, service, iconName, DurationPolicies.fixed(duration), requiresMemberInside);
	}

	@Override
	protected String validateMember(Member member) {
		return member.isInside() ? "The member is already inside the gym" : null;
	}

	@Override
	protected void onSuccessfulUse(Member member) {
		member.setInside(true);
	}
}
