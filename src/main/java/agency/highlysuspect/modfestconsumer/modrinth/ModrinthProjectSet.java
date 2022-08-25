package agency.highlysuspect.modfestconsumer.modrinth;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModrinthProjectSet {
	protected final Map<String, ModrinthProject> projectIdToProjectInfo = new HashMap<>();
	
	public @Nullable ModrinthProject getProjectOrNull(String projectId) {
		return projectIdToProjectInfo.get(projectId);
	}
}
