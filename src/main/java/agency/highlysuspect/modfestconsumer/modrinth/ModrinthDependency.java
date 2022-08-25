package agency.highlysuspect.modfestconsumer.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModrinthDependency {
	@JsonProperty("version_id") public String versionId;
	@JsonProperty("project_id") public String projectId;
	@JsonProperty("dependency_type") public Type dependencyType;
	
	@Override
	public String toString() {
		return "ModrinthDependency{versionId='%s', projectId='%s', dependencyType='%s'}".formatted(versionId, projectId, dependencyType);
	}
	
	public enum Type {
		@JsonProperty("required") Required,
		@JsonProperty("optional") Optional,
		@JsonProperty("incompatible") Incompatible,
		@JsonProperty("embedded") Embedded,
		;
	}
}
