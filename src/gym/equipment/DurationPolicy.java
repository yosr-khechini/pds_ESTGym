package gym.equipment;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Optional;

import gym.model.Member;
import gym.model.Service;

public interface DurationPolicy {
	Optional<Duration> resolve(Member member, Service service, LocalDateTime now);
}
