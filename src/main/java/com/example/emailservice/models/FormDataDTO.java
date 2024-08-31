package com.example.emailservice.models;

public class FormDataDTO {

    private String email;
    private String clientFirstName;
    private String servicemanFirstName;
    private String servicemanLastName;
    private String incidentNumber;
    private String incidentTopic;
    private Long ehelpdeskId;
    private Integer questionVersion;

    public Integer getQuestionVersion(){
        return questionVersion;
    }

    public void setQuestionVersion(Integer questionVersion){
        this.questionVersion = questionVersion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public void setClientFirstName(String clientFirstName) {
        this.clientFirstName = clientFirstName;
    }

    public String getServicemanFirstName() {
        return servicemanFirstName;
    }

    public void setServicemanFirstName(String servicemanFirstName) {
        this.servicemanFirstName = servicemanFirstName;
    }

    public String getServicemanLastName() {
        return servicemanLastName;
    }

    public void setServicemanLastName(String servicemanLastName) {
        this.servicemanLastName = servicemanLastName;
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

    public Long getEhelpdeskId(){
        return ehelpdeskId;
    }

    private void setEhelpdeskId(Long ehelpdeskId){
        this.ehelpdeskId = ehelpdeskId;
    }
}
