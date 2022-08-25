package agency.highlysuspect.modfestconsumer.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ModrinthProject {
	public String slug, title;
	@JsonProperty("versions") public List<String> versionIds;
}
