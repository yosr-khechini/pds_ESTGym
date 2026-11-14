package pds.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Class that represent a clock simulator. This clock is set to the start
 * (00:00) of the current day. <br>
 * In the system there must be only one clock, so access to it must be made
 * by using the <code><b>getSimulatedClock</b></code>, or, to access the current
 * time, by calling the <code><b>getCurrentTime</b></code>.<br>
 * So set the clock speed use the <code><b>setTicksPerSecond</b></code> method.
 * To set the current time, use the method <code><b>setCurrentTime</b></code>.
 */
public class SimulatedClock {

	private static SimulatedClock relogio;
	private LocalDateTime ultimoTempoLido;
	private long ultimoTempoReal;
	private int ticksPorSegundo = 1;

	/**
	 * Private constructor. To acccess the clock use the getSimulatedClock method
	 */
	private SimulatedClock() {
		ultimoTempoReal = System.currentTimeMillis();
		ultimoTempoLido = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
	}

	/**
	 * Returns the simulated clock
	 * 
	 * @return the simulated clock
	 */
	public static SimulatedClock getSimulatedClock() {
		if (relogio == null) {
			relogio = new SimulatedClock();
		}
		return relogio;
	}

	/**
	 * Convenience method to return the current time without having to access the
	 * clock
	 * 
	 * @return the current time in the simulated clock
	 */
	public static LocalDateTime getCurrentTime() {
		return getSimulatedClock().currentTime();
	}

	/**
	 * Returns the current time in the simulated clock
	 * 
	 * @return the current time in the simulated clock
	 */
	public synchronized LocalDateTime currentTime() {
		long agora = System.currentTimeMillis();
		long difSecs = (agora - ultimoTempoReal) / 1000;
		if (difSecs == 0)
			return ultimoTempoLido;
		long segundosSimulados = difSecs * ticksPorSegundo;
		ultimoTempoReal = agora;
		ultimoTempoLido = ultimoTempoLido.plusSeconds(segundosSimulados);
		return ultimoTempoLido;
	}

	/**
	 * Sets the "speed" of the clock, i. e., each real second counts as
	 * <b>ticksPerSecond</b> seconds in the simulated clock.
	 * Initial speed is 1 tick per second, i.e., 1 real second equals to 1
	 * simulated second.
	 * 
	 * @param ticksPerSecond how many simulated seconds pass for each real second
	 */
	public void setTicksPorSegundo(int ticksPerSecond) {
		if (ticksPerSecond < 1)
			throw new IllegalArgumentException("The minimum tick is 1: " + ticksPerSecond);
		this.ticksPorSegundo = ticksPerSecond;
	}

	/**
	 * Defines the current time in the simulated clock. The new time can never be
	 * before the current simulated time.
	 * 
	 * @param time the new time.
	 * @throws IllegalArgumentException if the time is before the current clock time
	 */
	public void setTempoAtual(LocalDateTime time) {
		ultimoTempoReal = System.currentTimeMillis();
		ultimoTempoLido = getCurrentTime();
		if (time.isBefore(ultimoTempoLido))
			throw new IllegalArgumentException(
					"time (" + time + " cannot be before the current time (" + ultimoTempoLido + ")");
		ultimoTempoLido = time;
	}
}
