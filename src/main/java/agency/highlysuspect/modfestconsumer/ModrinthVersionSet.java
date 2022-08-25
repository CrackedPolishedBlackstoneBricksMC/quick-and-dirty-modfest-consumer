package agency.highlysuspect.modfestconsumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ModrinthVersionSet {
	@JsonProperty("versionIdToVersionInfo")
	public Map<String, ModrinthAPI.Version> versionIdToVersionInfo = new HashMap<>();
	
	public @Nullable
	ModrinthAPI.Version getVersionOrNull(String versionId) {
		return versionIdToVersionInfo.get(versionId);
	}
	
	public boolean hasVersion(String versionId) {
		return versionIdToVersionInfo.containsKey(versionId);
	}
	
	public ModrinthAPI.Version getExistingVersionForProject(String projectId) {
		for(ModrinthAPI.Version v : allVersions()) {
			if(Objects.equals(v.projectId(), projectId)) return v;
		}
		return null;
	}
	
	public void put(ModrinthAPI.Version ver) {
		versionIdToVersionInfo.put(ver.id(), ver);
	}
	
	public void putAll(ModrinthVersionSet others) {
		others.allVersions().forEach(this::put);
	}
	
	public void putAll(Collection<ModrinthAPI.Version> others) {
		others.forEach(this::put);
	}
	
	@Override
	public String toString() {
		return versionIdToVersionInfo.toString();
	}
	
	public Set<ModrinthAPI.Version> allVersions() {
		return ImmutableSet.copyOf(versionIdToVersionInfo.values());
	}
	
	public int size() {
		return versionIdToVersionInfo.size();
	}
	
	public ModrinthVersionSet copy() {
		ModrinthVersionSet copy = new ModrinthVersionSet();
		copy.putAll(this);
		return copy;
	}
	
	public void clear() {
		versionIdToVersionInfo.clear();
	}
	
	public static class Cache extends ModrinthVersionSet {
		public Cache(Path path) {
			this.path = path;
		}
		
		private transient final Path path;
		private transient boolean dirty = false;
		
		public static ModrinthVersionSet.Cache load(Path path) {
			if(!Files.exists(path)) return new ModrinthVersionSet.Cache(path);
			else try {
				//Kinda crusty yea
				ModrinthVersionSet list = ModfestConsumer.JSON.readValue(Files.newBufferedReader(path), ModrinthVersionSet.class);
				ModrinthVersionSet.Cache cache = new ModrinthVersionSet.Cache(path);
				cache.putAll(list);
				return cache;
			} catch (Exception e) {
				e.printStackTrace();
				return new ModrinthVersionSet.Cache(path);
			}
		}
		
		public void save() throws Exception {
			if(dirty) {
				dirty = false;
				Files.createDirectories(path.getParent());
				Files.writeString(path, ModfestConsumer.JSON.writeValueAsString(this));
			}
		}
		
		public @Nullable
		ModrinthAPI.Version getVersionOrDownload(ModrinthAPI api, String versionId) {
			ModrinthAPI.Version get = versionIdToVersionInfo.get(versionId);
			if(get != null) return get;
			
			try {
				ModrinthAPI.Version ver = api.requestVersion(versionId);
				dirty = true;
				put(ver);
				return ver;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void fetchManyVersions(ModrinthAPI api, Collection<String> versionIds) {
			try {
				List<ModrinthAPI.Version> vers = api.requestManyVersions(versionIds);
				dirty = true;
				putAll(vers);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
