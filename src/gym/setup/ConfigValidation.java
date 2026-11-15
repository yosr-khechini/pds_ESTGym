package gym.setup;

final class ConfigValidation {
	private ConfigValidation() {
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
