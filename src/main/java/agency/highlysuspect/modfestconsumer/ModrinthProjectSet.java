package agency.highlysuspect.modfestconsumer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModrinthProjectSet {
	protected final Map<String, ModrinthAPI.Project> projectIdToProjectInfo = new HashMap<>();
	
	public @Nullable
	ModrinthAPI.Project getProjectOrNull(String projectId) {
		return projectIdToProjectInfo.get(projectId);
	}
}
