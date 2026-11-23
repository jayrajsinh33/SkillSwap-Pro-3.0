package com.skillswap.web;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import com.skillswap.service.WalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@RestController @RequestMapping("/api")
public class ClassController {
 @Value("${app.demo}") private boolean demo;
 @Value("${app.gst-bps}") private long gstBps;
 @Value("${app.commission-bps}") private long commissionBps;
 private final ApiSupport helper;private final BookingRepository bookings;private final CourseRepository courses;private final AppUserRepository users;private final WalletService wallet;private final SettlementRepository settlements;
 ClassController(ApiSupport helper,BookingRepository bookings,CourseRepository courses,AppUserRepository users,WalletService wallet,SettlementRepository settlements){this.helper=helper;this.bookings=bookings;this.courses=courses;this.users=users;this.wallet=wallet;this.settlements=settlements;}
 @GetMapping("/bookings")public List<Map<String,Object>> myBookings(Authentication a){AppUser u=helper.me(a);return bookings.findByLearnerIdOrMentorIdOrderByStartAtDesc(u.id,u.id).stream().map(ApiSupport::booking).toList();}
 private Map<String,Long> amounts(long rate,int hours,boolean free){
  long chargeable=hours-(free?1:0);long base=Math.multiplyExact(rate,chargeable),tax=wallet.percent(base,gstBps),comm=wallet.percent(base,commissionBps),feeTax=wallet.percent(comm,gstBps);
  return Map.of("basePaise",base,"taxPaise",tax,"totalPaise",base+tax,"commissionPaise",comm,"commissionTaxPaise",feeTax,"mentorNetPaise",base-comm-feeTax,"freeHours",free?1L:0L);
 }
 @GetMapping("/bookings/quote")public Map<String,Object> quote(Authentication a,@RequestParam Long courseId,@RequestParam(defaultValue="1")int hours){
  AppUser me=helper.me(a);Course c=courses.findById(courseId).filter(x->x.published).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(hours>=1&&hours<=8,"Choose 1–8 one-hour sessions");return Map.of("courseId",c.id,"hours",hours,"hourlyRatePaise",c.mentor.hourlyRatePaise,"quote",amounts(c.mentor.hourlyRatePaise,hours,!me.freeLectureUsed));
 }
 @PostMapping("/bookings") @Transactional public Map<String,Object> book(Authentication a,@RequestBody Map<String,Object> f){
  AppUser principal=helper.me(a);long cid=ApiSupport.num(f,"courseId",0);int hours=(int)ApiSupport.num(f,"hours",1);
  Course c=courses.findById(cid).filter(x->x.published).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Published course required"));
  ApiSupport.require(hours>=1&&hours<=8,"Choose 1 to 8 one-hour sessions");ApiSupport.require(!c.mentor.id.equals(principal.id),"You cannot book yourself");
  Instant start;try{start=Instant.parse(ApiSupport.str(f,"startAt"));}catch(Exception ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a valid date and time");}
  ApiSupport.require(start.isAfter(Instant.now().plusSeconds(900))&&start.isBefore(Instant.now().plusSeconds(365L*86400-8*86400)),"Choose a date 15 minutes to one year ahead");
  AppUser buyer=users.lockById(principal.id).orElseThrow();AppUser mentor=users.lockById(c.mentor.id).orElseThrow();
  boolean free=!buyer.freeLectureUsed;Map<String,Long> q=amounts(mentor.hourlyRatePaise,hours,free);
  ApiSupport.require(buyer.balancePaise>=q.get("totalPaise"),"Wallet balance is too low. Add money before booking.");
  ApiSupport.require(q.get("mentorNetPaise")>=0,"Invalid fee configuration");
  String bundle="BND-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT);
  List<Map<String,Object>> created=new ArrayList<>();long gross=0;
  for(int i=0;i<hours;i++){
   boolean trial=free&&i==0;long rate=trial?0:mentor.hourlyRatePaise;long tax=wallet.percent(rate,gstBps),comm=wallet.percent(rate,commissionBps),feeTax=wallet.percent(comm,gstBps),net=rate-comm-feeTax,total=rate+tax;
   Booking b=new Booking();b.learner=buyer;b.mentor=mentor;b.course=c;b.startAt=start.plusSeconds(i*75L*60);b.durationMinutes=60;b.paidPaise=total;b.freeTrial=trial;b.bundleId=bundle;b.lectureId="LEC-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);b.roomName="SkillSwap-"+UUID.randomUUID().toString().replace("-","");bookings.save(b);
   if(!trial){Settlement s=new Settlement();s.course=c;s.buyer=buyer;s.mentor=mentor;s.basePaise=rate;s.courseTaxPaise=tax;s.commissionPaise=comm;s.commissionTaxPaise=feeTax;s.mentorNetPaise=net;s.source="SESSION";settlements.save(s);wallet.record(buyer,-total,"SESSION_BOOKING","Booking "+b.lectureId,"SET-"+s.id);wallet.record(mentor,net,"MENTOR_SESSION","Session "+b.lectureId,"SET-"+s.id);mentor.lifetimeEarningsPaise+=net;}
   gross+=total;created.add(ApiSupport.booking(b));
  }
  if(free)buyer.freeLectureUsed=true;
  users.save(buyer);users.save(mentor);
  return Map.of("bookings",created,"bundleId",bundle,"quote",q,"totalPaidPaise",gross);
 }
 @PatchMapping("/bookings/{id}") @Transactional public Map<String,Object> change(Authentication a,@PathVariable Long id,@RequestBody Map<String,Object> form){
  AppUser u=helper.me(a);Booking b=bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));String status=ApiSupport.str(form,"status").toUpperCase(Locale.ROOT);
  ApiSupport.require(b.learner.id.equals(u.id)||b.mentor.id.equals(u.id)||u.admin,"No access");ApiSupport.require(!List.of("COMPLETED","CANCELLED").contains(b.status),"Booking already closed");
  if(status.equals("CONFIRMED")||status.equals("COMPLETED"))ApiSupport.require(b.mentor.id.equals(u.id)||u.admin,"Only mentor can confirm or complete");else ApiSupport.require(status.equals("CANCELLED"),"Invalid status");
  if(status.equals("COMPLETED"))ApiSupport.require("CONFIRMED".equals(b.status),"Confirm first");
  // Paid cancellations enter support review; automatic refunds require a fair settlement process.
  b.status=status;bookings.save(b);return ApiSupport.booking(b);
 }
 @GetMapping("/bookings/{id}/join")public Map<String,String> join(Authentication a,@PathVariable Long id){
  AppUser u=helper.me(a);Booking b=bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!b.learner.id.equals(u.id)&&!b.mentor.id.equals(u.id)&&!u.admin)throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  ApiSupport.require(b.status.equals("CONFIRMED"),"Only confirmed classes can be joined");
  if(!demo)ApiSupport.require(Instant.now().isAfter(b.startAt.minusSeconds(1800))&&Instant.now().isBefore(b.startAt.plusSeconds(b.durationMinutes*60L+1800)),"Room opens 30 minutes before the session");
  return Map.of("roomName",b.roomName,"displayName",u.name,"videoProvider","jitsi-dev","role",u.id.equals(b.mentor.id)?"MENTOR":"STUDENT","lectureId",b.lectureId==null?"":b.lectureId);
 }
}
