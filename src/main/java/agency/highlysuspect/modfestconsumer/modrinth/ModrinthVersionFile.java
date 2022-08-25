package agency.highlysuspect.modfestconsumer.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Map;

public class ModrinthVersionFile {
	public Map<String, String> hashes;
	public @JsonProperty("url") URI uri;
	public String filename;
	public boolean primary;
	
	@Override
	public String toString() {
		return "ModrinthVersionFile{hashes=%s, uri=%s, filename='%s', primary=%s}".formatted(hashes, uri, filename, primary);
	}
}
