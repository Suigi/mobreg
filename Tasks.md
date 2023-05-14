# Tasks

[X] Ensemble: Add "Spectators" (as Set<MemberId>)
    [X] void joinAsSpectator(MemberId memberId)
        [X] remove from Accepted 
        [X] remove from Declined
    [X] Set<MemberId> spectators()
    [X] Accept removes from Spectators
    [X] Decline removes from Spectators
[ ] EnsembleService: joinAsSpectator(EnsembleId, MemberId)
[ ] Add "Spectators" column to the member-register template
    [ ] Add to the EnsembleSummaryView object
    [ ] Add button for "Join as Spectator"
[ ] MemberController: handle the POST for Join as Spectator
[ ] Update RSVP enum to also have SPECTATOR
[ ] Admin Ensemble details screen: move a Member between Participant, Spectator, and Declined
[ ] Update labels and location of "accept" and "decline" buttons

# Later

* Add notification when joining as spectator
* Change storage of Member registrations from 3 separate Sets to a single Set,
  where there's an Enum for each (PARTICIPANT, SPECTATOR, and DECLINED)
* 