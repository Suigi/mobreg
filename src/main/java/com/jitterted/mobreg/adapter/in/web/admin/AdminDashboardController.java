package com.jitterted.mobreg.adapter.in.web.admin;

import com.jitterted.mobreg.domain.Huddle;
import com.jitterted.mobreg.domain.HuddleId;
import com.jitterted.mobreg.domain.HuddleService;
import com.jitterted.mobreg.domain.Member;
import com.jitterted.mobreg.domain.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final HuddleService huddleService;
    private final MemberService memberService;

    @Autowired
    public AdminDashboardController(HuddleService huddleService,
                                    MemberService memberService) {
        this.huddleService = huddleService;
        this.memberService = memberService;
    }

    @GetMapping("/dashboard")
    public String dashboardView(Model model, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (principal instanceof OAuth2User oAuth2User) {
            String username = oAuth2User.getAttribute("login");
            Member member = memberService.findByGithubUsername(username);
            model.addAttribute("username", username); // Member.githubUsername
            model.addAttribute("name", member.firstName());
            model.addAttribute("github_id", oAuth2User.getAttribute("id"));
        } else {
            throw new IllegalStateException("Not an OAuth2User");
        }
        List<Huddle> huddles = huddleService.allHuddlesByDateTimeDescending();
        List<HuddleSummaryView> huddleSummaryViews = HuddleSummaryView.from(huddles);
        model.addAttribute("huddles", huddleSummaryViews);
        model.addAttribute("scheduleHuddleForm", new ScheduleHuddleForm());
        return "dashboard";
    }

    @GetMapping("/huddle/{huddleId}")
    public String huddleDetailView(Model model, @PathVariable("huddleId") Long huddleId) {
        Huddle huddle = huddleService.findById(HuddleId.of(huddleId))
                                     .orElseThrow(() -> {
                                         throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                                     });

        HuddleDetailView huddleDetailView = HuddleDetailView.from(huddle, memberService);
        model.addAttribute("huddle", huddleDetailView);
        model.addAttribute("scheduleHuddleForm", ScheduleHuddleForm.from(huddle));
        model.addAttribute("completeHuddle", new CompleteHuddleForm(""));
        model.addAttribute("registration", new AdminRegistrationForm(huddle.getId()));

        return "huddle-detail";
    }

    @PostMapping("/huddle/{huddleId}")
    public String changeHuddle(ScheduleHuddleForm scheduleHuddleForm, @PathVariable("huddleId") Long id) {
        HuddleId huddleId = HuddleId.of(id);
        huddleService.changeNameDateTimeTo(huddleId, scheduleHuddleForm.getName(), scheduleHuddleForm.getDateTimeInUtc());
        return "redirect:/admin/huddle/" + id;
    }

    @PostMapping("/schedule")
    public String scheduleHuddle(ScheduleHuddleForm scheduleHuddleForm) {
        if (scheduleHuddleForm.getZoomMeetingLink().isBlank()) {
            huddleService.scheduleHuddle(scheduleHuddleForm.getName(),
                                         scheduleHuddleForm.getDateTimeInUtc());
        } else {
            huddleService.scheduleHuddle(scheduleHuddleForm.getName(),
                                         URI.create(scheduleHuddleForm.getZoomMeetingLink()),
                                         scheduleHuddleForm.getDateTimeInUtc());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/notify/{huddleId}")
    public String notifyHuddleScheduled(@PathVariable("huddleId") Long huddleId) {
        Huddle huddle = huddleService.findById(HuddleId.of(huddleId))
                .orElseThrow(() -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
        huddleService.triggerHuddleOpenedNotification(huddle);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/register")
    public String registerParticipant(AdminRegistrationForm adminRegistrationForm) {
        HuddleId huddleId = HuddleId.of(adminRegistrationForm.getHuddleId());

        Member member = memberService.findByGithubUsername(adminRegistrationForm.getGithubUsername());

        huddleService.registerMember(huddleId, member.getId());

        return redirectToDetailViewFor(huddleId);
    }

    @PostMapping("/huddle/{huddleId}/complete")
    public String completeHuddle(@PathVariable("huddleId") long id, CompleteHuddleForm completeHuddleForm) {
        HuddleId huddleId = HuddleId.of(id);
        huddleService.completeWith(huddleId, completeHuddleForm.recordingLink());

        return redirectToDetailViewFor(huddleId);
    }

    private String redirectToDetailViewFor(HuddleId huddleId) {
        return "redirect:/admin/huddle/" + huddleId.id();
    }

}
