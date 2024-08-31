package com.example.emailservice.services.incident;

import com.example.emailservice.entities.EHelpdesk;
import com.example.emailservice.entities.Incident;
import com.example.emailservice.entities.Serviceman;
import com.example.emailservice.entities.Client;
import com.example.emailservice.models.FormDataDTO;
import com.example.emailservice.repositories.IncidentRepository;
import com.example.emailservice.repositories.ServicemanRepository;
import com.example.emailservice.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.emailservice.utils.ExceptionUtil.getRootCause;

@Service
public class IncidentDataService {
    /**
     * Handles communication with the eHelpDesk API for all responses.
     */

    private final ClientRepository clientRepository;
    private final ServicemanRepository servicemanRepository;
    private final IncidentRepository incidentRepository;

    @Autowired
    public IncidentDataService(ClientRepository clientRepository,
                               ServicemanRepository servicemanRepository,
                               IncidentRepository incidentRepository) {
        this.clientRepository = clientRepository;
        this.servicemanRepository = servicemanRepository;
        this.incidentRepository = incidentRepository;
    }

    /**
     * Saves incident data, including client and serviceman information.
     */
    @Transactional
    public ResponseEntity<String> saveIncidentData(FormDataDTO formDataDTO, Incident incident, String hash, EHelpdesk eHelpdesk) {
        try {
            Client client = clientRepository.findByEmail(formDataDTO.getEmail())
                    .orElseGet(Client::new);
            client.setEmail(formDataDTO.getEmail());
            client.setFirstName(formDataDTO.getClientFirstName());
            client = clientRepository.save(client);

            Serviceman serviceman = servicemanRepository.findByFirstNameAndLastName(
                            formDataDTO.getServicemanFirstName(), formDataDTO.getServicemanLastName())
                    .orElseGet(Serviceman::new);
            serviceman.setFirstName(formDataDTO.getServicemanFirstName());
            serviceman.setLastName(formDataDTO.getServicemanLastName());
            serviceman = servicemanRepository.save(serviceman);

            incident.setHash(hash);
            incident.setIncidentNumber(formDataDTO.getIncidentNumber());
            incident.setIncidentTopic(formDataDTO.getIncidentTopic());
            incident.setClient(client);
            incident.setServiceman(serviceman);
            incident.setIsDone(false);
            incident.setEhelpdesk(eHelpdesk);
            incident.setQuestionVersion(formDataDTO.getQuestionVersion());

            incidentRepository.save(incident);

        } catch (DataIntegrityViolationException e) {
            String errorMessage = "Error: data integrity violation occurred - " + getRootCause(e).getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
        }
        return ResponseEntity.ok("Incident data saved successfully");
    }
}
