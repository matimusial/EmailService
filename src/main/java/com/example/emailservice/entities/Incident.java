package com.example.emailservice.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "incidents", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"incident_number", "ehelpdesk_id"})
})
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    private Long incidentID;

    @Column(name = "hash", nullable = false, unique = true)
    private String hash;

    @Column(name = "incident_number", nullable = false)
    private String incidentNumber;

    @Column(name = "incident_topic", nullable = false)
    private String incidentTopic;

    @Column(name = "is_done", nullable = false)
    private Boolean isDone;

    @Column(name = "question_version", nullable = false)
    private Integer questionVersion;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "ehelpdesk_id", nullable = false)
    private EHelpdesk ehelpdesk;

    @ManyToOne
    @JoinColumn(name = "serviceman_id", nullable = false)
    private Serviceman serviceman;

    public Long getIncidentID() {
        return incidentID;
    }

    public void setIncidentID(Long incidentID) {
        this.incidentID = incidentID;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getIncidentNumber() {
        return incidentNumber;
    }

    public void setIncidentNumber(String incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    public String getIncidentTopic() {
        return incidentTopic;
    }

    public void setIncidentTopic(String incidentTopic) {
        this.incidentTopic = incidentTopic;
    }

    public Boolean getIsDone() {
        return isDone;
    }

    public void setIsDone(Boolean isDone) {
        this.isDone = isDone;
    }

    public Integer getQuestionVersion() {
        return questionVersion;
    }

    public void setQuestionVersion(Integer questionVersion) {
        this.questionVersion = questionVersion;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public EHelpdesk getEhelpdesk() {
        return ehelpdesk;
    }

    public void setEhelpdesk(EHelpdesk ehelpdesk) {
        this.ehelpdesk = ehelpdesk;
    }

    public Serviceman getServiceman() {
        return serviceman;
    }

    public void setServiceman(Serviceman serviceman) {
        this.serviceman = serviceman;
    }
}
