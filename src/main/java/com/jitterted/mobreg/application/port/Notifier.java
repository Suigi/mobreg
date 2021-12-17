package com.jitterted.mobreg.application.port;

import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.Member;

import java.net.URI;

public interface Notifier {
    int ensembleScheduled(Ensemble ensemble, URI registrationLink);

    void memberRegistered(Ensemble ensemble, Member member);
}
