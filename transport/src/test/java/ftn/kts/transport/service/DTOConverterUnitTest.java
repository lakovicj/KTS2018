package ftn.kts.transport.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import ftn.kts.transport.DTOconverter.DTOConverter;
import ftn.kts.transport.dtos.LineDTO;
import ftn.kts.transport.dtos.PriceListDTO;
import ftn.kts.transport.dtos.RouteScheduleDTO;
import ftn.kts.transport.dtos.TicketDTO;
import ftn.kts.transport.enums.TicketTypeTemporal;
import ftn.kts.transport.enums.VehicleType;
import ftn.kts.transport.exception.DAOException;
import ftn.kts.transport.exception.InvalidInputDataException;
import ftn.kts.transport.model.Line;
import ftn.kts.transport.model.LineTicket;
import ftn.kts.transport.model.PriceList;
import ftn.kts.transport.model.Route;
import ftn.kts.transport.model.RouteSchedule;
import ftn.kts.transport.model.RouteTicket;
import ftn.kts.transport.model.Zone;
import ftn.kts.transport.model.ZoneTicket;
import ftn.kts.transport.services.LineService;
import ftn.kts.transport.services.RouteService;
import ftn.kts.transport.services.ZoneService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:test.properties")
public class DTOConverterUnitTest {

	@Autowired
	private DTOConverter converter;
	@MockBean
	private LineService lineServiceMocked;
	@MockBean
	private ZoneService zoneServiceMocked;
	@MockBean
	private RouteService routeServiceMocked;
	
	private Line line = new Line();
	private Zone zone = new Zone();
	private Route route = new Route();
	private LineDTO lineDTO = new LineDTO();
	private RouteScheduleDTO rsDTO = new RouteScheduleDTO();
	private PriceListDTO plDTO = new PriceListDTO();
	private TicketDTO ticketDTO = new TicketDTO();
	private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy. HH:mm");
	
	@Before
	public void setUp() {
		line.setId(1L);
		line.setActive(true);
		line.setName("7A");
		line.setTransportType(VehicleType.BUS);
		line.setDuration(10000);
		Mockito.when(lineServiceMocked.findById(1L)).thenReturn(line);
		Mockito.when(lineServiceMocked.findById(-1L)).thenThrow(new DAOException("Line [id=-1] cannot be found!"));
		Mockito.when(lineServiceMocked.findByName("7A")).thenReturn(line);
		Mockito.when(lineServiceMocked.findByName("8A")).thenThrow(new DAOException("Line [name=755B] cannot be found!"));
			
		zone.setId(1L);
		zone.setName("Zona I");
		zone.setSubZone(null);
		zone.setActive(true);
		Mockito.when(zoneServiceMocked.findById(1L)).thenReturn(zone);
		Mockito.when(zoneServiceMocked.findById(-1L)).thenThrow(new DAOException("Station [id=-1] cannot be found!"));
		
		route.setId(1L);
		route.setLine(line);
		Mockito.when(routeServiceMocked.getRoute(1L)).thenReturn(route);
		Mockito.when(routeServiceMocked.getRoute(-1L)).thenThrow(new DAOException("Route [id=-1] cannot be found!"));
		
		lineDTO.setName("8A");
		lineDTO.setVehicleType(0);
		lineDTO.setStreetPath(new HashSet<String>());
		lineDTO.getStreetPath().add("path1");
		lineDTO.setDuration(10000);
		
		rsDTO.setSaturday(new HashSet<Date>());
		rsDTO.setSunday(new HashSet<Date>());
		rsDTO.setWeekday(new HashSet<Date>());
		
		try {
			sdf.applyPattern("dd.MM.yyyy.");
			rsDTO.setActiveFrom(sdf.parse("01.03.2019."));
			
			sdf.applyPattern("HH:mm");
			rsDTO.getSaturday().add(sdf.parse("12:00"));
			rsDTO.getSunday().add(sdf.parse("15:00"));
			rsDTO.getWeekday().add(sdf.parse("20:00"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		plDTO.setLineDiscount(0.5);
		plDTO.setStudentDiscount(0.8);
		plDTO.setSeniorDiscount(0.7);
		plDTO.setMonthlyCoeff(17);
		plDTO.setYearlyCoeff(170);
		plDTO.setZonePrices(new HashMap<Long, Double>());
		plDTO.getZonePrices().put(1L, 100.00);
		
		
		//ticketDTO.setTicketTemporal(TicketTypeTemporal.ONE_HOUR_PASS);
		//ticketDTO.setTransportType(VehicleType.BUS);
		ticketDTO.setTicketTemporal(0);
		ticketDTO.setTransportType(0);
		
	}
	
	@Test
	public void convertToLine_PASS_Test() {
		Line ret = converter.convertDTOtoLine(lineDTO);
		assertNotNull(ret);
		assertEquals(lineDTO.getName(), ret.getName());
		assertEquals(lineDTO.getVehicleType(), ret.getTransportType().ordinal());
		assertEquals(lineDTO.getStreetPath().size(), ret.getStreetPath().size());
		assertEquals(lineDTO.getStreetPath().iterator().next(), ret.getStreetPath().iterator().next());
		assertEquals(lineDTO.getDuration(), ret.getDuration());
	}
	
	@Test(expected = DAOException.class)
	public void convertToLine_LineAlreadyExists_Test() {
		lineDTO.setName("7A");
		converter.convertDTOtoLine(lineDTO);
		lineDTO.setName("8A");
	}
	
	@Test(expected = InvalidInputDataException.class)
	public void convertToLine_InvalidTransportTypeIndex_Test() {
		// transport type = {0, 1, 2}
		lineDTO.setVehicleType(5);
		converter.convertDTOtoLine(lineDTO);
		lineDTO.setVehicleType(0);
	}
	
	@Test(expected = InvalidInputDataException.class)
	public void convertToLine_InvalidDuration_Test() {
		lineDTO.setDuration(-5000);
		converter.convertDTOtoLine(lineDTO);
		lineDTO.setDuration(10000);	// vrati na staro
	}
	
	@Test
	public void convertToRouteSchedule_PASS_Test() {
		RouteSchedule created = converter.convertDTOtoRouteSchedule(rsDTO);
		assertNotNull(created);
		assertEquals(1, created.getSaturday().size());
		assertEquals(1, created.getSunday().size());
		assertEquals(1, created.getWeekday().size());
		sdf.applyPattern("dd.MM.yyyy.");
		assertEquals("01.03.2019.", sdf.format(created.getactiveFrom()));

	}
	
	
	@Transactional
	@Test(expected = InvalidInputDataException.class)
	public void convertToTicket_InvalidTemporal_Test() {
		ticketDTO.setTicketTemporal(-1);
		converter.convertDTOtoTicket(ticketDTO);
	}
	
	@Test(expected = InvalidInputDataException.class)
	public void convertToTicket_InvalidTransport_Test() {
		ticketDTO.setTransportType(-1);
		converter.convertDTOtoTicket(ticketDTO);
	}
	
	@Test(expected = DAOException.class)
	public void convertToTicket_ZoneNotFound_Test() {
		ticketDTO.setZoneId(-1L);
		converter.convertDTOtoTicket(ticketDTO);
	}
	
	@Test(expected = DAOException.class)
	public void convertToTicket_LineNotFound_Test() {
		ticketDTO.setLineId(-1L);
		converter.convertDTOtoTicket(ticketDTO);
	}
	
	@Test(expected = DAOException.class)
	public void convertToTicket_RouteNotFound_Test() {
		ticketDTO.setRouteId(-1L);
		converter.convertDTOtoTicket(ticketDTO);
	}
	
	@Transactional
	@Test
	public void convertToTicket_ZoneTicket_PASS_Test() {
		ticketDTO.setZoneId(1L);
		ZoneTicket ret = (ZoneTicket) converter.convertDTOtoTicket(ticketDTO);
		assertNotNull(ret);
		assertEquals(ticketDTO.getZoneId(), ret.getZone().getId());
		//assertFalse(ret.isActive());
		assertEquals(ticketDTO.getTicketTemporal(), ret.getTicketTemporal().ordinal());
		assertEquals(ticketDTO.getTransportType(), ret.getTransportType().ordinal());
	}
	
	@Transactional
	@Test
	public void convertToTicket_LineTicket_PASS_Test() {
		ticketDTO.setLineId(1L);
		LineTicket ret = (LineTicket) converter.convertDTOtoTicket(ticketDTO);
		assertNotNull(ret);
		assertEquals(ticketDTO.getLineId(), ret.getLine().getId());
		//assertFalse(ret.isActive());
		assertEquals(ticketDTO.getTicketTemporal(), ret.getTicketTemporal().ordinal());
		assertEquals(ticketDTO.getTransportType(), ret.getTransportType().ordinal());
	}
	
	@Transactional
	@Test
	public void convertToTicket_LineTicket_Monthly_PASS_Test() {
		ticketDTO.setLineId(1L);
		ticketDTO.setTicketTemporal(1);
		LineTicket ret = (LineTicket) converter.convertDTOtoTicket(ticketDTO);
		assertNotNull(ret);
		assertEquals(ticketDTO.getLineId(), ret.getLine().getId());
		//assertTrue(ret.isActive());
		assertEquals(ticketDTO.getTicketTemporal(), ret.getTicketTemporal().ordinal());
		assertEquals(ticketDTO.getTransportType(), ret.getTransportType().ordinal());
	}
	
	
	@Transactional
	@Test
	public void convertToTicket_RouteTicket_PASS_Test() {
		ticketDTO.setRouteId(1L);
		ticketDTO.setTicketTemporal(3);
		RouteTicket ret = (RouteTicket) converter.convertDTOtoTicket(ticketDTO);
		assertNotNull(ret);
		assertEquals(ticketDTO.getRouteId(), ret.getRoute().getId());
		//assertTrue(ret.isActive());
		assertEquals(TicketTypeTemporal.ONE_TIME_PASS, ret.getTicketTemporal());
		assertEquals(ticketDTO.getTransportType(), ret.getTransportType().ordinal());
	}
	
	@Transactional
	@Test
	public void convertToTicket_ZoneTicket_Monthly_PASS_Test() {
		ticketDTO.setZoneId(1L);
		ticketDTO.setTicketTemporal(1);
		ZoneTicket ret = (ZoneTicket) converter.convertDTOtoTicket(ticketDTO);
		assertNotNull(ret);
		assertEquals(ticketDTO.getZoneId(), ret.getZone().getId());
		//assertTrue(ret.isActive());
		assertEquals(ticketDTO.getTicketTemporal(), ret.getTicketTemporal().ordinal());
		assertEquals(ticketDTO.getTransportType(), ret.getTransportType().ordinal());
		assertNotNull(ret.getStartTime());
		assertNotNull(ret.getEndTime());
	}
	

	@Transactional
	@Test
	public void converToTicket_ZoneTicket_Yearly_PASS_Test() {
		ticketDTO.setZoneId(1L);
		ticketDTO.setTicketTemporal(2);
		ZoneTicket ret = (ZoneTicket) converter.convertDTOtoTicket(ticketDTO);
		assertNotNull(ret);
		assertEquals(ticketDTO.getZoneId(), ret.getZone().getId());
		//assertTrue(ret.isActive());
		assertEquals(ticketDTO.getTicketTemporal(), ret.getTicketTemporal().ordinal());
		assertEquals(ticketDTO.getTransportType(), ret.getTransportType().ordinal());
		assertNotNull(ret.getStartTime());
		assertNotNull(ret.getEndTime());
	}
	
	
	
	
	@Test
	public void convertToPriceList_PASS_Test() {
		plDTO.setStartDate(new Date());
		PriceList ret = converter.convertDTOtoPriceList(plDTO);
		assertNotNull(ret);
		assertEquals(plDTO.getLineDiscount(), ret.getLineDiscount(), 0.0001);
		assertEquals(plDTO.getMonthlyCoeff(), ret.getMonthlyCoeffitient(), 0.0001);
		assertEquals(plDTO.getSeniorDiscount(), ret.getSeniorDiscount(), 0.0001);
		assertEquals(plDTO.getStudentDiscount(), ret.getStudentDiscount(), 0.0001);
		assertEquals(plDTO.getYearlyCoeff(), ret.getYearlyCoeffitient(), 0.0001);
		assertEquals(plDTO.getZonePrices().size(), ret.getOneTimePrices().size());
		assertFalse(ret.isActive());
	}
	
	@Transactional
	@Test(expected = DAOException.class)
	public void convertToPriceList_ZoneNotFound_Test() {
		plDTO.getZonePrices().remove(1L);
		plDTO.getZonePrices().put(-1L, 200.00);
		converter.convertDTOtoPriceList(plDTO);
	}
	
	@Test(expected = InvalidInputDataException.class)
	public void convertToPriceList_InvalidLineDiscount_Test() {
		plDTO.setLineDiscount(-10.0);
		converter.convertDTOtoPriceList(plDTO);
	}
	
	@Test(expected = InvalidInputDataException.class)
	public void convertToPriceList_InvalidMonthlyCoeff_Test() {
		plDTO.setMonthlyCoeff(-10.0);
		converter.convertDTOtoPriceList(plDTO);
	}
	
	@Test(expected = InvalidInputDataException.class)
	public void convertToPriceList_InvalidYearlyCoeff_Test() {
		plDTO.setYearlyCoeff(-10.0);
		converter.convertDTOtoPriceList(plDTO);
	}
	
	@Test(expected = InvalidInputDataException.class)
	public void convertToPriceList_InvalidSeniorDiscount_Test() {
		plDTO.setSeniorDiscount(-10.0);
		converter.convertDTOtoPriceList(plDTO);
	}
	
	@Test(expected = InvalidInputDataException.class)
	public void convertToPriceList_InvalidStudentDiscount_Test() {
		plDTO.setStudentDiscount(-10.0);
		converter.convertDTOtoPriceList(plDTO);
	}
	
}
