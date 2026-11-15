package gym.model;

import java.util.Objects;

public final class Service {
	private final String id;
	private final String name;
	private final int classLevel;

	public Service(String id, String name) {
		this(id, name, 0);
	}

	public Service(String id, String name, int classLevel) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name);

		if (classLevel < 0 || classLevel > 2)
			throw new IllegalArgumentException("Class level must be between 0 and 2");

		this.classLevel = classLevel;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getClassLevel() {
		return classLevel;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Service service && id.equals(service.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
