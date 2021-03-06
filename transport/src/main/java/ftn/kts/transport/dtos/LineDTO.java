package ftn.kts.transport.dtos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LineDTO {

	private String name;
	private HashMap<Integer, Long> stations;
	private int vehicleType;
	private Set<String> streetPath;
	private int duration;
	private String description;

	public LineDTO(String name, HashMap<Integer, Long> stations, int vehicleType, Set<String> streetPath, int duration, String description) {
		this.name = name;
		this.stations = stations;
		this.vehicleType = vehicleType;
		this.streetPath = streetPath;
		this.duration = duration;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LineDTO() {
		this.stations = new HashMap<Integer, Long>();
		this.streetPath = new HashSet<String>();
	}

	public LineDTO(String name) {
		super();
		this.name = name;
	}
	
	public LineDTO(String name, HashMap<Integer, Long> stations) {
		super();
		this.name = name;
		this.stations = stations;
	}
	

	public LineDTO(String name, HashMap<Integer, Long> stations, int vehicleType) {
		super();
		this.name = name;
		this.stations = stations;
		this.vehicleType = vehicleType;
	}
	

	public LineDTO(String name, HashMap<Integer, Long> stations, int vehicleType, Set<String> streetPath) {
		super();
		this.name = name;
		this.stations = stations;
		this.vehicleType = vehicleType;
		this.streetPath = streetPath;
	}

	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<Integer, Long> getStations() {
		return stations;
	}

	public void setStations(HashMap<Integer, Long> stations) {
		this.stations = stations;
	}

	public int getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}

	public Set<String> getStreetPath() {
		return streetPath;
	}

	public void setStreetPath(Set<String> streetPath) {
		this.streetPath = streetPath;
	}
	
	
	
	
}
