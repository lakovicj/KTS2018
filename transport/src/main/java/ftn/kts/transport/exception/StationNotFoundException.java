package ftn.kts.transport.exception;

public class StationNotFoundException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long stationId;
	
	public StationNotFoundException(Long id) {
		this.stationId = id;
	}

	public Long getStationId() {
		return stationId;
	}

}
