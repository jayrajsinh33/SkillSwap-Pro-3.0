package com.skillswap.web;

import com.skillswap.model.*;
import com.skillswap.repo.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@Component
public class ApiSupport {
    private final AppUserRepository users;
    public ApiSupport(AppUserRepository users){this.users=users;}
    public AppUser me(Authentication a){if(a==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);return users.findById(((AppUser)a.getPrincipal()).id).filter(u->u.active).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
    public AppUser admin(Authentication a){AppUser u=me(a);if(!u.admin)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Admin only");return u;}
    public AppUser mentor(Authentication a){AppUser u=me(a);if(!u.mentor&&!u.admin)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Mentor approval required");return u;}
    public static String str(Map<String,?> m,String key){Object v=m.get(key);return v==null?"":v.toString().trim();}
    public static long num(Map<String,?> m,String key,long fallback){Object v=m.get(key);if(v==null)return fallback;try{return Long.parseLong(v.toString());}catch(NumberFormatException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid numeric field: "+key);}}
    public static void require(boolean ok,String message){if(!ok)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    public static Map<String,Object> user(AppUser u){Map<String,Object> m=new LinkedHashMap<>();m.put("id",u.id);m.put("name",u.name);m.put("email",u.email);m.put("headline",u.headline);m.put("bio",u.bio);m.put("city",u.city);m.put("avatar",u.avatar);m.put("skills",u.skills);m.put("mentor",u.mentor);m.put("mentorRequested",u.mentorRequested);m.put("admin",u.admin);m.put("active",u.active);m.put("balancePaise",u.balancePaise);m.put("lifetimeEarningsPaise",u.lifetimeEarningsPaise);m.put("freeLectureUsed",u.freeLectureUsed);m.put("hourlyRatePaise",u.hourlyRatePaise);m.put("performanceScore",u.performanceScore);m.put("violationCount",u.violationCount);m.put("permanentlyBanned",u.permanentlyBanned);m.put("assessedPenaltyPaise",u.assessedPenaltyPaise);m.put("mentorDocumentSubmitted",u.mentorDocumentKey!=null&&!u.mentorDocumentKey.isBlank());m.put("mentorDocumentVerified",u.mentorDocumentVerified);m.put("mentorBankSubmitted",u.mentorBankLast4!=null&&!u.mentorBankLast4.isBlank());m.put("mentorBankVerified",u.mentorBankVerified);m.put("mentorBankLast4",u.mentorBankLast4);m.put("mentorAgreementSigned",u.mentorAgreedAt!=null);return m;}
    public static Map<String,Object> publicUser(AppUser u){Map<String,Object> m=new LinkedHashMap<>();m.put("id",u.id);m.put("name",u.name);m.put("headline",u.headline);m.put("bio",u.bio);m.put("city",u.city);m.put("avatar",u.avatar);m.put("skills",u.skills);m.put("mentor",u.mentor);m.put("hourlyRatePaise",u.hourlyRatePaise);m.put("performanceScore",u.performanceScore);return m;}
    public static Map<String,Object> course(Course c){Map<String,Object> m=new LinkedHashMap<>();m.put("id",c.id);m.put("title",c.title);m.put("description",c.description);m.put("category",c.category);m.put("cover",c.cover);m.put("level",c.level);m.put("tags",c.tags);m.put("lessons",c.lessons);m.put("durationMinutes",c.durationMinutes);m.put("pricePaise",c.pricePaise);m.put("published",c.published);m.put("rating",c.rating);m.put("mentor",publicUser(c.mentor));return m;}
    public static Map<String,Object> booking(Booking b){Map<String,Object> m=new LinkedHashMap<>();m.put("id",b.id);m.put("status",b.status);m.put("startAt",b.startAt);m.put("durationMinutes",b.durationMinutes);m.put("course",course(b.course));m.put("lectureId",b.lectureId==null?"":b.lectureId);m.put("paidPaise",b.paidPaise);m.put("freeTrial",b.freeTrial);m.put("refunded",b.refunded);m.put("bundleId",b.bundleId);m.put("learner",publicUser(b.learner));m.put("mentor",publicUser(b.mentor));return m;}
    public static Map<String,Object> entry(WalletEntry e){return Map.of("id",e.id,"changePaise",e.changePaise,"balanceAfterPaise",e.balanceAfterPaise,"type",e.type,"description",e.description,"createdAt",e.createdAt);}
}
