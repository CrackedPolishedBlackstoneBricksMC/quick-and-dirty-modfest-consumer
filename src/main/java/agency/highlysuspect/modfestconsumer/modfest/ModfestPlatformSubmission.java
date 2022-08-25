package agency.highlysuspect.modfestconsumer.modfest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.stream.Collectors;

public class ModfestPlatformSubmission {
	public String slug, name;
	@JsonProperty("id") public String modrinthProjectId;
	@JsonProperty("version_id") public String modrinthVersionId;
	
	public static String bigConcat(Collection<ModfestPlatformSubmission> subs) {
		return subs.stream().map(s -> s.name).collect(Collectors.joining(", "));
	}
	
	@Override
	public String toString() {
		return "ModfestPlatformSubmission{slug='%s', name='%s', modrinthProjectId='%s', modrinthVersionId='%s'}".formatted(slug, name, modrinthProjectId, modrinthVersionId);
	}
}
