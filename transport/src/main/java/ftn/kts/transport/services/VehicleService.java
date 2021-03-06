package ftn.kts.transport.services;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import ftn.kts.transport.dtos.VehicleDTO;
import ftn.kts.transport.model.Vehicle;

@Service
public interface VehicleService {

	List<Vehicle> findAll();
	Vehicle addVehicle(VehicleDTO vehicle);
	Vehicle updateVehicle(VehicleDTO vehicle, long id);
	boolean deleteVehicle(long id);
	Vehicle getFreeVehicle(Date time, int duration);
	Vehicle findById(Long id);
}
