package com.jitterted.mobreg.adapter.out.jdbc;

import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.EnsembleId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Database-specific DTO for Ensemble to be stored in the database
@Table("ensembles")
class EnsembleDbo {
    @Id
    private Long id;

    private String name;
    private String zoomMeetingLink;
    private LocalDateTime dateTimeUtc;
    private String state;
    private String recordingLink;

    @MappedCollection(idColumn = "ensemble_id")
    private Set<AcceptedMember> acceptedMembers = new HashSet<>();

    @MappedCollection(idColumn = "ensemble_id")
    private Set<DeclinedMember> declinedMembers = new HashSet<>();

    public static EnsembleDbo from(Ensemble ensemble) {
        EnsembleDbo ensembleDbo = new EnsembleDbo();
        if (ensemble.getId() != null) {
            ensembleDbo.setId(ensemble.getId().id());
        }
        ensembleDbo.setName(ensemble.name());
        ensembleDbo.setDateTimeUtc(ensemble.startDateTime().toLocalDateTime());
        ensembleDbo.setZoomMeetingLink(ensemble.meetingLink().toString());
        ensembleDbo.setState(ensemble.state().toString());
        ensembleDbo.setRecordingLink(ensemble.recordingLink().toString());
        ensembleDbo.setAcceptedMembers(
                ensemble.acceptedMembers()
                        .map(AcceptedMember::toEntityId)
                        .collect(Collectors.toSet()));
        ensembleDbo.setDeclinedMembers(
                ensemble.declinedMembers()
                        .map(DeclinedMember::toEntityId)
                        .collect(Collectors.toSet()));
        return ensembleDbo;
    }

    public Ensemble asEnsemble() {
        ZonedDateTime startDateTime = ZonedDateTime.of(dateTimeUtc, ZoneOffset.UTC);
        Ensemble ensemble = new Ensemble(name, URI.create(zoomMeetingLink), startDateTime);
        ensemble.setId(EnsembleId.of(id));
        ensemble.linkToRecordingAt(URI.create(recordingLink));

        acceptedMembers.stream()
                       .map(AcceptedMember::asMemberId)
                       .forEach(ensemble::acceptedBy);

        declinedMembers.stream()
                       .map(DeclinedMember::asMemberId)
                       .forEach(ensemble::declinedBy);

        if (state.equalsIgnoreCase("COMPLETED")) {
            ensemble.complete();
        } else if (state.equalsIgnoreCase("CANCELED")) {
            ensemble.cancel();
        }

        return ensemble;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDateTimeUtc() {
        return dateTimeUtc;
    }

    public void setDateTimeUtc(LocalDateTime dateTimeUtc) {
        this.dateTimeUtc = dateTimeUtc;
    }

    public Set<AcceptedMember> getAcceptedMembers() {
        return acceptedMembers;
    }

    public void setAcceptedMembers(Set<AcceptedMember> acceptedMembers) {
        this.acceptedMembers = acceptedMembers;
    }

    public Set<DeclinedMember> getDeclinedMembers() {
        return declinedMembers;
    }

    public void setDeclinedMembers(Set<DeclinedMember> declinedMembers) {
        this.declinedMembers = declinedMembers;
    }

    public String getZoomMeetingLink() {
        return zoomMeetingLink;
    }

    public void setZoomMeetingLink(String zoomMeetingLink) {
        this.zoomMeetingLink = zoomMeetingLink;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRecordingLink() {
        return recordingLink;
    }

    public void setRecordingLink(String recordingLink) {
        this.recordingLink = recordingLink;
    }
}
