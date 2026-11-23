package com.skillswap.web;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/admin")
public class AdminController {
 private final ApiSupport helper;private final AppUserRepository users;private final CourseRepository courses;private final BookingRepository bookings;private final SettlementRepository settlements;private final WithdrawalRepository withdrawals;private final WalletEntryRepository entries;
 AdminController(ApiSupport helper,AppUserRepository users,CourseRepository courses,BookingRepository bookings,SettlementRepository settlements,WithdrawalRepository withdrawals,WalletEntryRepository entries){this.helper=helper;this.users=users;this.courses=courses;this.bookings=bookings;this.settlements=settlements;this.withdrawals=withdrawals;this.entries=entries;}
 @GetMapping("/overview")public Map<String,Object> overview(Authentication a){helper.admin(a);var all=settlements.findAll();return Map.of("users",users.count(),"mentors",users.findAll().stream().filter(x->x.mentor).count(),"courses",courses.count(),"bookings",bookings.count(),"grossSalesPaise",all.stream().mapToLong(x->x.basePaise+x.courseTaxPaise).sum(),"commissionPaise",all.stream().mapToLong(x->x.commissionPaise).sum(),"commissionTaxPaise",all.stream().mapToLong(x->x.commissionTaxPaise).sum(),"courseTaxPaise",all.stream().mapToLong(x->x.courseTaxPaise).sum());}
 @GetMapping("/users")public List<Map<String,Object>> users(Authentication a){helper.admin(a);return users.findAll().stream().map(ApiSupport::user).toList();}
 @PatchMapping("/users/{id}")public Map<String,Object> user(Authentication a,@PathVariable Long id,@RequestBody Map<String,Object> f){AppUser admin=helper.admin(a);AppUser u=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(f.containsKey("mentor")){ApiSupport.require(!u.admin,"Cannot revoke admin mentor flag");boolean approving=Boolean.parseBoolean(f.get("mentor").toString());if(approving){ApiSupport.require(!u.permanentlyBanned,"Permanently banned mentors cannot reapply");ApiSupport.require(u.mentorRequested,"Mentor must apply first");ApiSupport.require(u.mentorDocumentVerified&&u.mentorBankVerified&&u.mentorAgreedAt!=null&&"2026-11".equals(u.mentorAgreementVersion),"Identity, bank and signed contract are required");}u.mentor=approving;if(u.mentor)u.mentorRequested=false;}if(f.containsKey("active")){ApiSupport.require(!u.permanentlyBanned || !Boolean.parseBoolean(f.get("active").toString()),"Permanently banned user cannot be enabled");ApiSupport.require(!u.id.equals(admin.id),"Cannot disable yourself");u.active=Boolean.parseBoolean(f.get("active").toString());}users.save(u);return ApiSupport.user(u);}
 @GetMapping("/courses")public List<Map<String,Object>> courses(Authentication a){helper.admin(a);return courses.findAll().stream().map(ApiSupport::course).toList();}
 @PatchMapping("/courses/{id}")public Map<String,Object> course(Authentication a,@PathVariable Long id,@RequestBody Map<String,Object> f){helper.admin(a);Course c=courses.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));c.published=Boolean.parseBoolean(ApiSupport.str(f,"published"));courses.save(c);return ApiSupport.course(c);}
 @GetMapping("/bookings")public List<Map<String,Object>> bookings(Authentication a){helper.admin(a);return bookings.findAll().stream().map(ApiSupport::booking).toList();}
 @GetMapping("/settlements")public List<Map<String,Object>> settlements(Authentication a){helper.admin(a);return settlements.findAll().stream().map(s->Map.<String,Object>of("id",s.id,"course",s.course.title,"learner",s.buyer.name,"mentor",s.mentor.name,"basePaise",s.basePaise,"courseTaxPaise",s.courseTaxPaise,"commissionPaise",s.commissionPaise,"commissionTaxPaise",s.commissionTaxPaise,"mentorNetPaise",s.mentorNetPaise)).toList();}
 @GetMapping("/withdrawals")public List<Map<String,Object>> withdrawals(Authentication a){helper.admin(a);return withdrawals.findAll().stream().map(w->Map.<String,Object>of("id",w.id,"user",w.user.name,"amountPaise",w.amountPaise,"bankDetails",w.bankDetails,"status",w.status)).toList();}
 @PatchMapping("/withdrawals/{id}")@Transactional public Map<String,Object> settle(Authentication a,@PathVariable Long id,@RequestBody Map<String,Object> f){helper.admin(a);Withdrawal w=withdrawals.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(w.status.equals("REQUESTED"),"Already processed");String next=ApiSupport.str(f,"status");ApiSupport.require(next.equals("PAID")||next.equals("REJECTED"),"Choose PAID or REJECTED");
  if(next.equals("REJECTED")){w.user.balancePaise+=w.amountPaise;WalletEntry e=new WalletEntry();e.user=w.user;e.changePaise=w.amountPaise;e.balanceAfterPaise=w.user.balancePaise;e.type="WITHDRAWAL_REFUND";e.description="Reversed withdrawal #"+w.id;e.reference="WD-"+w.id;entries.save(e);users.save(w.user);}
  w.status=next;withdrawals.save(w);return Map.of("id",w.id,"status",w.status);
 }
}
