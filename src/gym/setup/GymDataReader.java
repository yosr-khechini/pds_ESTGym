package gym.setup;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

final class GymDataReader {
	private static final Gson GSON = new Gson();

	private GymDataReader() {
	}

	static GymData read(Path path) {
		if (!Files.isRegularFile(path))
			throw ConfigValidation.configurationError("Gym data file not found: " + path.toAbsolutePath()
					+ ". Run the application with the project root as its working directory.");
		try (Reader reader = Files.newBufferedReader(path)) {
			return readData(reader, path);
		} catch (JsonParseException e) {
			throw ConfigValidation.configurationError("Invalid JSON in " + path + ": " + e.getMessage(), e);
		} catch (IOException e) {
			throw ConfigValidation.configurationError("Could not read gym data file " + path + ": " + e.getMessage(),
					e);
		}
	}

	private static GymData readData(Reader reader, Path path) {
		GymData data = GSON.fromJson(reader, GymData.class);
		if (data == null)
			throw ConfigValidation.configurationError("Gym data file is empty: " + path.toAbsolutePath());
		return data;
	}
}
