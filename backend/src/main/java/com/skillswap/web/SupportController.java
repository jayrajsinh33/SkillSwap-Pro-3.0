package com.skillswap.web;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import com.skillswap.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api")
public class SupportController {
 private final SupportTicketRepository tickets;private final ApiSupport helper;private final BookingRepository bookings;private final AppUserRepository users;private final WalletService wallet;private final GroupClassRepository groupClasses;
 SupportController(SupportTicketRepository tickets,ApiSupport helper,BookingRepository bookings,AppUserRepository users,WalletService wallet,GroupClassRepository groupClasses){this.tickets=tickets;this.helper=helper;this.bookings=bookings;this.users=users;this.wallet=wallet;this.groupClasses=groupClasses;}
 @PostMapping("/support/tickets")public Map<String,Object> create(Authentication a,@RequestBody Map<String,Object> body){
  AppUser u=helper.me(a);String subject=ApiSupport.str(body,"subject"),message=ApiSupport.str(body,"message");ApiSupport.require(subject.length()>=5&&subject.length()<=140&&message.length()>=10&&message.length()<=3000,"Provide a subject and detailed description");
  SupportTicket t=new SupportTicket();t.user=u;t.subject=subject;t.message=message;tickets.save(t);return Map.of("ticketId",t.id,"status",t.status);
 }
 @PostMapping("/support/reports")public Map<String,Object> report(Authentication a,@RequestBody Map<String,Object> body){
  AppUser author=helper.me(a);String lectureId=ApiSupport.str(body,"lectureId").toUpperCase(Locale.ROOT),message=ApiSupport.str(body,"message"),against=ApiSupport.str(body,"against").toUpperCase(Locale.ROOT);
  Booking b=bookings.findByLectureId(lectureId).orElse(null);
  GroupClass group=b==null?groupClasses.findByLectureId(lectureId).orElse(null):null;
  if(b==null&&group==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Check the Lecture ID in your booking or live class");
  ApiSupport.require(message.length()>=20&&message.length()<=3000,"Explain what happened in at least 20 characters");
  ApiSupport.require(against.equals("MENTOR")||against.equals("STUDENT"),"Choose the person involved");
  AppUser accused;
  if(b!=null){
   ApiSupport.require(b.learner.id.equals(author.id)||b.mentor.id.equals(author.id),"Only lecture participants can report an issue");
   accused=against.equals("MENTOR")?b.mentor:b.learner;
  }else{
   boolean host=group.mentor.id.equals(author.id),attendee=group.attendees.stream().anyMatch(x->x.id.equals(author.id));
   ApiSupport.require(host||attendee,"Only registered live class participants can report an issue");
   if(against.equals("MENTOR"))accused=group.mentor;
   else{
    ApiSupport.require(host,"Only the host may identify a student in a group lecture report");
    long targetId=ApiSupport.num(body,"targetUserId",0);
    accused=group.attendees.stream().filter(x->x.id.equals(targetId)).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose the registered student ID"));
   }
  }
  ApiSupport.require(!accused.id.equals(author.id),"You cannot report yourself");
  boolean duplicate=tickets.findAll().stream().anyMatch(t->t.user.id.equals(author.id)&&lectureId.equals(t.lectureId)&&!"REJECTED".equals(t.status));
  ApiSupport.require(!duplicate,"You already submitted a report for this lecture");
  SupportTicket t=new SupportTicket();t.user=author;t.lectureId=lectureId;t.accusedUserId=accused.id;t.category="LECTURE_REPORT";t.subject="Lecture report: "+lectureId;t.message=message;t.status="PENDING_REVIEW";tickets.save(t);
  return Map.of("ticketId",t.id,"status",t.status,"lectureId",lectureId);
 }
 @GetMapping("/support/tickets")public List<Map<String,Object>> mine(Authentication a){AppUser u=helper.me(a);return tickets.findByUserIdOrderByCreatedAtDesc(u.id).stream().map(this::view).toList();}
 @GetMapping("/admin/support/tickets")public List<Map<String,Object>> all(Authentication a){helper.admin(a);return tickets.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();}
 @PatchMapping("/admin/support/tickets/{id}")public Map<String,Object> status(Authentication a,@PathVariable Long id,@RequestBody Map<String,Object> body){
  helper.admin(a);SupportTicket t=tickets.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(!"LECTURE_REPORT".equals(t.category),"Use the review report action for lecture complaints");String value=ApiSupport.str(body,"status");ApiSupport.require(value.equals("OPEN")||value.equals("CLOSED"),"Invalid status");t.status=value;tickets.save(t);return view(t);
 }
 @PatchMapping("/admin/support/reports/{id}")@Transactional public Map<String,Object> decide(Authentication a,@PathVariable Long id,@RequestBody Map<String,Object> body){
  helper.admin(a);SupportTicket t=tickets.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require("LECTURE_REPORT".equals(t.category)&&"PENDING_REVIEW".equals(t.status),"Report already decided or invalid");
  String decision=ApiSupport.str(body,"decision").toUpperCase(Locale.ROOT),resolution=ApiSupport.str(body,"resolution");ApiSupport.require(decision.equals("VALID")||decision.equals("REJECTED"),"Select VALID or REJECTED");ApiSupport.require(resolution.length()>=15&&resolution.length()<=1000,"Record evidence and investigation outcome (15+ characters)");
  t.status=decision;t.resolution=resolution;
  if(decision.equals("VALID")){
   boolean alreadyPenalised=tickets.findAll().stream().anyMatch(r->!r.id.equals(t.id)&&r.accusedUserId!=null&&r.accusedUserId.equals(t.accusedUserId)&&t.lectureId.equals(r.lectureId)&&r.status.equals("VALID"));
   AppUser accused=users.lockById(t.accusedUserId).orElseThrow();
   if(!alreadyPenalised){accused.violationCount++;int n=accused.violationCount;accused.performanceScore=n==1?96:n==2?93:0;
    if(accused.mentor){long penalty=n==1?200000:n==2?500000:1000000;accused.assessedPenaltyPaise+=penalty;long collected=Math.min(Math.max(0,accused.balancePaise),penalty);if(collected>0)wallet.record(accused,-collected,"PENALTY","Reviewed violation #"+n,"REPORT-"+t.id);}
    if(n>=3){accused.permanentlyBanned=true;accused.active=false;accused.mentor=false;accused.sessionKey=UUID.randomUUID().toString();}
    users.save(accused);
   }
   Booking b=bookings.findByLectureId(t.lectureId).orElse(null);
   // Paid 1:1 sessions can be refunded exactly once. Group classes need a separate
   // purchase/settlement investigation because attendance alone has no session charge.
   if(b!=null&&!b.refunded&&b.paidPaise>0&&b.mentor.id.equals(accused.id)){
    AppUser learner=users.lockById(b.learner.id).orElseThrow();
    wallet.record(learner,b.paidPaise,"SESSION_REFUND","Validated report "+t.lectureId,"REPORT-"+t.id);
    b.refunded=true;bookings.save(b);t.refundedPaise=b.paidPaise;
   }
  }
  tickets.save(t);return view(t);
 }
 private Map<String,Object> view(SupportTicket t){Map<String,Object> m=new LinkedHashMap<>();m.put("id",t.id);m.put("name",t.user.name);m.put("email",t.user.email);m.put("subject",t.subject);m.put("message",t.message);m.put("status",t.status);m.put("createdAt",t.createdAt);m.put("category",t.category);m.put("lectureId",t.lectureId);m.put("accusedUserId",t.accusedUserId==null?0:t.accusedUserId);m.put("resolution",t.resolution);m.put("refundedPaise",t.refundedPaise);return m;}
}
