package agency.highlysuspect.modfestconsumer.modrinth;

import com.google.gson.annotations.SerializedName;

public class ModrinthDependency {
	@SerializedName("version_id") public String versionId;
	@SerializedName("project_id") public String projectId;
	@SerializedName("dependency_type") public Type dependencyType;
	
	@Override
	public String toString() {
		return "ModrinthDependency{versionId='%s', projectId='%s', dependencyType='%s'}".formatted(versionId, projectId, dependencyType);
	}
	
	public enum Type {
		@SerializedName("required") Required,
		@SerializedName("optional") Optional,
		@SerializedName("incompatible") Incompatible,
		@SerializedName("embedded") Embedded,
		;
	}
}
