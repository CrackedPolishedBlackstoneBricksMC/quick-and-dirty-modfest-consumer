package agency.highlysuspect.modfestconsumer.modrinth;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModrinthVersion {
	@SerializedName("id") public String id;
	@SerializedName("project_id") public String projectId;
	@SerializedName("version_number") public String number;
	@SerializedName("date_published") public String datePublished; //TODO use a real java time datatype (probably Instant?), labrinth says this is an ISO-8601 date
	public List<ModrinthDependency> dependencies;
	public List<ModrinthVersionFile> files;
	
	@Override
	public String toString() {
		return "ModrinthVersion{id='%s', projectId='%s', number='%s', datePublished='%s', dependencies=%s, files=%s}".formatted(id, projectId, number, datePublished, dependencies, files);
	}
	
	public ModrinthVersionFile findPrimaryFile() {
		if(files == null || files.isEmpty()) throw new IllegalStateException("No files");
		
		for(ModrinthVersionFile file : files) {
			if(file.primary) return file;
		}
		
		//"If there isn't a primary, assume the zeroth one" ~ Emmaffle on Discord, your home for critical api docs ;)
		return files.get(0);
	}
}
