package com.skillswap.service;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import com.skillswap.web.ApiSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class WalletService {
 private final AppUserRepository users;private final WalletEntryRepository entries;private final EnrollmentRepository enrollments;private final CourseRepository courses;private final SettlementRepository settlements;
 @Value("${app.commission-bps}") long commissionBps;
 @Value("${app.gst-bps}") long gstBps;
 public WalletService(AppUserRepository users,WalletEntryRepository entries,EnrollmentRepository enrollments,CourseRepository courses,SettlementRepository settlements){this.users=users;this.entries=entries;this.enrollments=enrollments;this.courses=courses;this.settlements=settlements;}
 @Transactional public AppUser lockUser(Long id){return users.lockById(id).orElseThrow();}
 public long percent(long amount,long bps){return Math.multiplyExact(amount,bps)/10000;}
 public Map<String,Long> quote(Course c){long base=c.pricePaise,courseTax=percent(base,gstBps),comm=percent(base,commissionBps),commTax=percent(comm,gstBps);return Map.of("basePaise",base,"courseTaxPaise",courseTax,"totalPaise",base+courseTax,"commissionPaise",comm,"commissionTaxPaise",commTax,"mentorNetPaise",base-comm-commTax);}
 public void record(AppUser u,long delta,String type,String desc,String ref){u.balancePaise+=delta;WalletEntry e=new WalletEntry();e.user=u;e.changePaise=delta;e.balanceAfterPaise=u.balancePaise;e.type=type;e.description=desc;e.reference=ref;entries.save(e);users.save(u);}
 @Transactional public Map<String,Object> buy(Long buyerId,Long courseId){
   Course c=courses.findById(courseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));
   ApiSupport.require(c.published,"Course is not published");ApiSupport.require(!c.mentor.id.equals(buyerId),"Cannot purchase your own course");
   ApiSupport.require(!enrollments.existsByLearnerIdAndCourseId(buyerId,courseId),"Already enrolled");
   AppUser buyer=users.lockById(buyerId).orElseThrow();AppUser mentor=users.lockById(c.mentor.id).orElseThrow();
   var q=quote(c);long total=q.get("totalPaise"),net=q.get("mentorNetPaise");
   ApiSupport.require(buyer.balancePaise>=total,"Insufficient wallet balance. Add funds first.");
   ApiSupport.require(net>=0,"Invalid fee configuration");
   Enrollment enrollment=new Enrollment();enrollment.learner=buyer;enrollment.course=c;enrollment.amountPaidPaise=total;enrollments.save(enrollment);
   Settlement s=new Settlement();s.buyer=buyer;s.mentor=mentor;s.course=c;s.source="COURSE";s.basePaise=q.get("basePaise");s.courseTaxPaise=q.get("courseTaxPaise");s.commissionPaise=q.get("commissionPaise");s.commissionTaxPaise=q.get("commissionTaxPaise");s.mentorNetPaise=net;settlements.save(s);
   record(buyer,-total,"COURSE_PURCHASE","Purchased: "+c.title,"SET-"+s.id);
   record(mentor,net,"MENTOR_EARNING","Earnings: "+c.title,"SET-"+s.id);
   mentor.lifetimeEarningsPaise+=net;users.save(mentor);
   return Map.of("enrollmentId",enrollment.id,"settlementId",s.id,"quote",q);
 }
 @Transactional public Map<String,Object> demoTopup(Long userId,long amount,boolean enabled){
   if(!enabled)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Demo top-ups disabled");
   ApiSupport.require(amount>=10000&&amount<=2000000,"Choose ₹100 to ₹20,000");
   AppUser u=users.lockById(userId).orElseThrow();record(u,amount,"DEMO_TOPUP","Demo wallet credit (no actual payment)","DEMO-"+UUID.randomUUID());
   return Map.of("balancePaise",u.balancePaise,"demo",true);
 }
 @Transactional public void gatewayTopup(Long userId,long amount,String reference){AppUser u=users.lockById(userId).orElseThrow();record(u,amount,"GATEWAY_TOPUP","Verified online payment",reference);}
}
