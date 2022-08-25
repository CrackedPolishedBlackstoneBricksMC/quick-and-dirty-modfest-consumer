package agency.highlysuspect.modfestconsumer.modrinth;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModrinthProject {
	public String slug, title;
	@SerializedName("versions") public List<String> versionIds;
}
