package com.jitterted.mobreg.adapter.out.websocket;

import com.jitterted.mobreg.domain.EnsembleTimer;
import com.jitterted.mobreg.domain.Member;
import com.jitterted.mobreg.domain.Rotation;
import com.jitterted.mobreg.domain.TimeRemaining;

import java.util.List;

public class TimerToHtmlTransformer {

    public static String htmlFor(EnsembleTimer ensembleTimer) {
        return switch (ensembleTimer.state()) {
            case WAITING_TO_START -> htmlForWaitingToStart(ensembleTimer);
            case RUNNING -> htmlForRunning(ensembleTimer);
            case PAUSED -> null;
            case FINISHED -> htmlForFinished(ensembleTimer);
        };
    }

    // language=html
    private static String htmlForWaitingToStart(EnsembleTimer ensembleTimer) {
        return htmlForTimerControlButton(ensembleTimer, "/admin/start-timer", "Start Timer")
               + htmlForTimerContainer(ensembleTimer.timeRemaining())
               + htmlForSwappingInRotationMembers(ensembleTimer.rotation());
    }

    private static String htmlForTimerControlButton(EnsembleTimer ensembleTimer,
                                                    String buttonEndpointUrl,
                                                    String buttonLabel) {
        return """
               <button id="timer-control-button"
                       hx-swap-oob="outerHTML"
                       hx-swap="none"
                       hx-post="%s/%s"
                       class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
                   %s
               </button>
               """.formatted(buttonEndpointUrl,
                             ensembleTimer.ensembleId().id(),
                             buttonLabel);
    }

    public static String htmlForSwappingInRotationMembers(Rotation rotation) {
        return """
               <swap-container id="%s" hx-swap-oob="innerHTML">
                   <p>%s</p>
               </swap-container>
               <swap-container id="%s" hx-swap-oob="innerHTML">
                   <p>%s</p>
               </swap-container>
               <swap-container id="%s" hx-swap-oob="innerHTML">
                   <p>%s</p>
               </swap-container>
               """.formatted(RotationRole.ROLE_DRIVER.idString(),
                             rotation.driver().firstName(),
                             RotationRole.ROLE_NAVIGATOR.idString(),
                             rotation.navigator().firstName(),
                             RotationRole.ROLE_NEXT_DRIVER.idString(),
                             rotation.nextDriver().firstName())
               + """
                 <swap-container id="%s" hx-swap-oob="innerHTML">
                 %s
                 </swap-container>
                 """.formatted(RotationRole.ROLE_REST_OF_PARTICIPANTS.idString(),
                               htmlForRestOfParticipants(rotation.restOfParticipants()));
    }

    public static String htmlForRestOfParticipants(List<Member> restOfParticipants) {
        return restOfParticipants.stream()
                                 .map(Member::firstName)
                                 .map("    <p>%s</p>"::formatted)
                                 .reduce((left, right) -> left + "\n" + right)
                                 .orElse("    <p>(no other participants)</p>");
    }

    // language=html
    private static String htmlForRunning(EnsembleTimer ensembleTimer) {
        return htmlForTimerControlButton(ensembleTimer, "/admin/pause-timer", "Pause Timer")
               + htmlForTimerContainer(ensembleTimer.timeRemaining());
    }

    // language=html
    private static String htmlForTimerContainer(TimeRemaining timeRemaining) {
        double percentRemaining = timeRemaining.percent();
        return """
               <div id="timer-container"
                    class="circle circle-running"
                    style="background: conic-gradient(lightgreen 0%% %f%%, black %f%% 100%%);">
                   <svg class="progress-ring">
                       <circle class="progress-circle"/>
                   </svg>
                   <div class="timer-text-container timer-running">
                       <div class="timer-text">%d:%02d</div>
                   </div>
               </div>
               """.formatted(percentRemaining, percentRemaining,
                             timeRemaining.minutes(),
                             timeRemaining.seconds());
    }

    // language=html
    private static String htmlForFinished(EnsembleTimer ensembleTimer) {
        return """
               <button id="timer-control-button"
                       hx-swap-oob="outerHTML"
                       hx-swap="none"
                       hx-post="/admin/rotate-timer/%s"
                       class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
                   Next Rotation
               </button>
               """.formatted(ensembleTimer.ensembleId().id())
               +
               """ 
               <div id="timer-container"
                    class="circle circle-finished">
                   <div class="timer-text-container timer-finished">
                       <div class="timer-text">next</div>
                   </div>
               </div>
               """;
    }

}
