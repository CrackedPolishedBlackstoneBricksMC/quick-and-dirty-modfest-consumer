package agency.highlysuspect.modfestconsumer.modrinth;

import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.util.Map;

public class ModrinthVersionFile {
	public Map<String, String> hashes;
	public @SerializedName("url") URI uri;
	public String filename;
	public boolean primary;
	
	@Override
	public String toString() {
		return "ModrinthVersionFile{hashes=%s, uri=%s, filename='%s', primary=%s}".formatted(hashes, uri, filename, primary);
	}
}
