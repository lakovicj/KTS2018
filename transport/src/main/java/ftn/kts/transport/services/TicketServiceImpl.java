package ftn.kts.transport.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.BaseEncoding;

import ftn.kts.transport.enums.TicketActivationType;
import ftn.kts.transport.enums.TicketTypeTemporal;
import ftn.kts.transport.exception.DAOException;
import ftn.kts.transport.exception.InvalidInputDataException;
import ftn.kts.transport.exception.TicketAlreadyActivatedException;
import ftn.kts.transport.model.RouteTicket;
import ftn.kts.transport.model.Ticket;
import ftn.kts.transport.model.User;
import ftn.kts.transport.repositories.TicketRepository;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;


@Service
public class TicketServiceImpl implements TicketService{

	@Autowired
	private TicketRepository ticketRepository;
	@Autowired
	private PriceListService priceListService;
	@Autowired
	private UserService userService;
	@Autowired
	private StorageService storageService;
	
	@Override
	public Ticket buyTicket(Ticket ticket, String token) {

		User logged = userService.getUser(token);
		
		// mesecne i godisnje karte mogu kupiti samo Useri kojima je approved verification document!
		if (ticket.getTicketTemporal().ordinal() != 0 && ticket.getTicketTemporal().ordinal() != 3) {
			if (logged.getDocumentVerified().ordinal() == 0) {
				throw new InvalidInputDataException("User's personal document is not uploaded! Only"
						+ " One-hour/One-time ticket can be purchased if User hasn't uploaded personal document!", 
						HttpStatus.FORBIDDEN);
			} else if (logged.getDocumentVerified().ordinal() == 1) {
				throw new InvalidInputDataException("User's personal document has not been verified yet! Only"
						+ " One-hour/One-time ticket can be purchased if personal document is not verified!", 
						HttpStatus.FORBIDDEN);
			} else if (logged.getDocumentVerified().ordinal() == 2) {
				throw new InvalidInputDataException("User's personal document has been rejected! Try"
						+ " uploading document again! One-hour/One-time tickets can be purchased without personal document", 
						HttpStatus.FORBIDDEN);
			} 
		}
		
		
		ticket.setUser(logged);
		
		double calculatedPrice = priceListService.calculateTicketPrice(ticket);
		ticket.setPrice(calculatedPrice);
		
		return ticketRepository.save(ticket);
	
	}
	
	@Override
	public Ticket activateTicket(Ticket ticket) {
		if (ticket.getStartTime() != null) {
			throw new TicketAlreadyActivatedException("Ticket had been already activated!");
		}
		ticket.setActive(TicketActivationType.ACTIVE);
		DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date currentDate = new Date();
		Date endDate = Date.from(currentDate.toInstant().plus(Duration.ofHours(1)));
		System.out.println("Current date " + sdf.format(currentDate) + " + 1 hour : " + sdf.format(endDate));
		ticket.setStartTime(currentDate);
		ticket.setEndTime(endDate);
		return ticketRepository.save(ticket);
	}

	@Override
	public Ticket findById(Long id) {
		Optional<Ticket> ticket = ticketRepository.findById(id);
		Ticket t = ticket.orElseThrow(() -> new DAOException("Ticket [id=" + id + "] not found!", HttpStatus.NOT_FOUND));	
		return checkTicket(t);
	}


	@Override
    public List<Ticket> getTickets(User user){
		List<Ticket> tickets = this.ticketRepository.findByUser(user);
		
		for(Ticket t : tickets) {
			t = checkTicket(t);
		}
		
		return tickets;
	}
	
	@Override
	public Ticket checkTicket(Ticket t) {
		Date currentTime = new Date();
		if(t.getTicketTemporal().equals(TicketTypeTemporal.ONE_HOUR_PASS)) {
			if(t.getEndTime() != null && t.getEndTime().before(currentTime)) {
				t.setActive(TicketActivationType.EXPIRED);
				ticketRepository.save(t);
			}
		} else if(t.getTicketTemporal().equals(TicketTypeTemporal.ONE_TIME_PASS)) {
			int duration = ((RouteTicket) t).getRoute().getLine().getDuration();
			Date endDateOfRoute = Date.from(((RouteTicket) t).getRoute().getDate().toInstant().plus(Duration.ofMinutes(duration)));
			if(endDateOfRoute.before(currentTime)) {
				t.setActive(TicketActivationType.EXPIRED);
				ticketRepository.save(t);
			}
		}
		return t;
	}
	
	@Override
    public String generateQrCode(Long id) {

        String encodedID = BaseEncoding.base64()
                .encode(("TicketID=" + id.toString()).getBytes());

        MultipartFile qrCode = new MockMultipartFile("file", id.toString() + ".jpg", "image/jpeg", QRCode.from(encodedID).to(ImageType.JPG).withSize(250, 250).stream().toByteArray());
       
        this.storageService.store(qrCode);
        
        return qrCode.getOriginalFilename();
    }

	@Override
    public Long decodeId(String encodedID){

	    byte[] decodedID = BaseEncoding.base64()
                .decode(encodedID);

	    String stringID = new String(decodedID);

	    return Long.parseLong(stringID.substring(9));
    }

}
