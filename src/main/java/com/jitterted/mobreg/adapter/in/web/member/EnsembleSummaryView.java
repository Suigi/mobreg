package com.jitterted.mobreg.adapter.in.web.member;

import com.jitterted.mobreg.adapter.DateTimeFormatting;
import com.jitterted.mobreg.adapter.in.web.admin.MemberView;
import com.jitterted.mobreg.application.GoogleCalendarLinkCreator;
import com.jitterted.mobreg.application.MemberService;
import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.MemberEnsembleStatus;
import com.jitterted.mobreg.domain.MemberId;
import com.jitterted.mobreg.domain.MemberStatus;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public record EnsembleSummaryView(long id,
                                  String name,
                                  String dateTime,
                                  int participantCount,
                                  List<MemberView> participants,
                                  List<MemberView> spectators,
                                  @Deprecated String memberStatus,
                                  List<DisplayLink> links,
                                  boolean showActionButtons,
                                  SpectatorAction spectatorAction,
                                  ParticipantAction participantAction) {

    public static List<EnsembleSummaryView> from(List<Ensemble> ensembles, MemberId memberId, MemberService memberService) {
        return ensembles.stream()
                        .filter(ensemble -> ensemble.statusFor(memberId, ZonedDateTime.now()) != MemberEnsembleStatus.HIDDEN)
                        .map(ensemble -> toView(ensemble, memberId, memberService))
                        .toList();
    }

    public static EnsembleSummaryView toView(Ensemble ensemble, MemberId memberId, MemberService memberService) {
        List<MemberView> participantViews = transform(memberService, ensemble.acceptedMembers());
        List<MemberView> spectatorViews = transform(memberService, ensemble.spectators());

        String memberStatusAsString = memberStatusToViewString(ensemble, memberId);
        MemberStatus memberStatusForEnsemble = ensemble.memberStatusFor(memberId);
        SpectatorAction spectatorAction = SpectatorAction.from(memberStatusForEnsemble);
        ParticipantAction participantAction = ParticipantAction.from(memberStatusForEnsemble,
                                                                     ensemble.isFull() && memberStatusForEnsemble != MemberStatus.PARTICIPANT);

        return new EnsembleSummaryView(
                ensemble.getId().id(),
                ensemble.name(),
                DateTimeFormatting.formatAsDateTimeForCommonIso8601(ensemble.startDateTime()),
                ensemble.acceptedCount(),
                participantViews,
                spectatorViews,
                memberStatusAsString,
                createLinksFor(ensemble, memberStatusForEnsemble),
                showActionButtonsFor(ensemble),
                spectatorAction,
                participantAction
        );
    }

    private static boolean showActionButtonsFor(Ensemble ensemble) {
        return !ensemble.endTimeIsInThePast(ZonedDateTime.now());
    }

    private static List<DisplayLink> createLinksFor(Ensemble ensemble, MemberStatus memberStatusForEnsemble) {
        if (ensemble.isCompleted()) {
            return List.of(new DisplayLink(ensemble.recordingLink().toString(),
                                           "Recording Link"));
        }
        if (ensemble.isPendingCompletedAsOf(ZonedDateTime.now())) {
            return List.of(new DisplayLink("", "Recording Coming Soon..."));
        }
        return switch (memberStatusForEnsemble) {
            case UNKNOWN, DECLINED -> Collections.emptyList();
            case PARTICIPANT, SPECTATOR -> {
                DisplayLink calendarLink = new DisplayLink(
                        new GoogleCalendarLinkCreator().createFor(ensemble),
                        "<i class=\"fas fa-calendar-plus pr-2\" aria-hidden=\"true\"></i>Add to Google Calendar");
                DisplayLink zoomLink = new DisplayLink(
                        ensemble.meetingLink().toString(),
                        "<i class=\"far fa-video pr-2\" aria-hidden=\"true\"></i>Zoom Link");
                yield List.of(calendarLink,
                              zoomLink);
            }
        };
    }

    private static String memberStatusToViewString(Ensemble ensemble, MemberId memberId) {
        return ensemble.statusFor(memberId, ZonedDateTime.now())
                       .toString()
                       .toLowerCase();
    }

    private static List<MemberView> transform(MemberService memberService, Stream<MemberId> memberIdStream) {
        return memberIdStream
                .map(memberService::findById)
                .map(MemberView::from)
                .toList();
    }

}

record SpectatorAction(String actionUrl, String buttonText) {
    public static SpectatorAction from(MemberStatus memberStatus) {
        return switch (memberStatus) {
            case UNKNOWN, DECLINED -> new SpectatorAction(
                    "/member/join-as-spectator",
                    "Join as Spectator &#x1F440;");
            case PARTICIPANT -> new SpectatorAction(
                    "/member/join-as-spectator",
                    "Switch to Spectator &#x1F440;");
            case SPECTATOR -> new SpectatorAction(
                    "/member/decline",
                    "Leave Spectators &#x1f44b;");
        };
    }
}

record ParticipantAction(String actionUrl, String buttonText, boolean disabled) {

    public static ParticipantAction from(MemberStatus memberStatus, boolean disabled) {
        if (disabled && memberStatus == MemberStatus.PARTICIPANT) {
            throw new IllegalStateException("Can't disable Participate Button if Member is a Participant");
        }
        if (disabled) {
            return new ParticipantAction(
                    "",
                    "Cannot Participate: Ensemble Full",
                    true);
        }
        return switch (memberStatus) {
            case UNKNOWN, DECLINED -> new ParticipantAction(
                    "/member/accept",
                    "Participate in Rotation &#x2328;",
                    false);
            case PARTICIPANT -> new ParticipantAction(
                    "/member/decline",
                    "Leave Rotation &#x1f44b;",
                    false); // can always leave
            case SPECTATOR -> new ParticipantAction(
                    "/member/accept",
                    "Switch to Participant &#x2328;",
                    false);
        };
    }
}

record DisplayLink(String url, String text) {
}