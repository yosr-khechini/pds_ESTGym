package gym.plan;

import java.time.Duration;
import java.time.LocalDateTime;

public interface ShowerContinuation {
	Duration getExtraDuration();

	boolean canActivateExtra(LocalDateTime initialPhaseEnd, LocalDateTime now);
}
