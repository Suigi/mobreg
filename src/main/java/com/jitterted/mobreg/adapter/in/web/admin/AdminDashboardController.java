package com.jitterted.mobreg.adapter.in.web.admin;

import com.jitterted.mobreg.adapter.DateTimeFormatting;
import com.jitterted.mobreg.adapter.in.web.HuddleSummaryView;
import com.jitterted.mobreg.domain.Huddle;
import com.jitterted.mobreg.domain.HuddleId;
import com.jitterted.mobreg.domain.HuddleService;
import com.jitterted.mobreg.domain.Member;
import com.jitterted.mobreg.domain.MemberId;
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
import java.time.ZonedDateTime;
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
        MemberId memberId;
        if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            String username = oAuth2User.getAttribute("login");
            Member member = memberService.findByGithubUsername(username);
            memberId = member.getId();
            model.addAttribute("username", username); // Member.githubUsername
            model.addAttribute("name", member.firstName());
            model.addAttribute("github_id", oAuth2User.getAttribute("id"));
        } else {
            throw new IllegalStateException("Not an OAuth2User");
        }
        List<Huddle> huddles = huddleService.allHuddles();
        List<HuddleSummaryView> huddleSummaryViews = HuddleSummaryView.from(huddles, memberId);
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
        model.addAttribute("registration", new AdminRegistrationForm(huddle.getId()));

        return "huddle-detail";
    }

    @PostMapping("/schedule")
    public String scheduleHuddle(ScheduleHuddleForm scheduleHuddleForm) {
        ZonedDateTime dateTime = DateTimeFormatting.fromBrowserDateAndTime(
                scheduleHuddleForm.getDate(),
                scheduleHuddleForm.getTime());
        if (scheduleHuddleForm.getZoomMeetingLink().isBlank()) {
            huddleService.scheduleHuddle(scheduleHuddleForm.getName(), dateTime);
        } else {
            huddleService.scheduleHuddle(scheduleHuddleForm.getName(), URI.create(scheduleHuddleForm.getZoomMeetingLink()), dateTime);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/register")
    public String registerParticipant(AdminRegistrationForm adminRegistrationForm) {
        HuddleId huddleId = HuddleId.of(adminRegistrationForm.getHuddleId());
        // TODO: register on behalf of user -- requires lookup

        return "redirect:/admin/huddle/" + huddleId.id();
    }
}
