package ftn.kts.transport.services;

import java.util.List;

import ftn.kts.transport.dtos.LineDTO;
import ftn.kts.transport.exception.DAOException;
import ftn.kts.transport.model.Line;

public interface LineService {

	Line findById(Long id);
	Line findByName(String name);
	Line addLineMethod(Line line, LineDTO lineDTO);
	Line addLine(Line line) throws DAOException;
	Line addStationsToLine(long id, LineDTO lineDTO) throws DAOException;
	Line updateLine(LineDTO line, long id) throws DAOException;
	Line deleteLine(long id) throws DAOException;
	Line updateLineStations(long id, LineDTO lineDTO);
	List<Line> getAllLines();
	
}
