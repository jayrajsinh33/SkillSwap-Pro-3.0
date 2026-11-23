package com.skillswap.web;

import com.skillswap.config.SecurityConfig;
import com.skillswap.model.AppUser;
import com.skillswap.repo.AppUserRepository;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api")
public class AuthController {
    private final AppUserRepository users;private final PasswordEncoder encoder;private final SecurityConfig security;private final ApiSupport helper;
    AuthController(AppUserRepository users, PasswordEncoder encoder, SecurityConfig security,ApiSupport helper){this.users=users;this.encoder=encoder;this.security=security;this.helper=helper;}
    @GetMapping("/health") public Map<String,Object> health(){return Map.of("status","ok","service","skillswap-api");}
    @PostMapping("/auth/register") public Map<String,Object> register(@RequestBody Map<String,Object> form){
        String email=ApiSupport.str(form,"email").toLowerCase(Locale.ROOT);String name=ApiSupport.str(form,"name");String password=ApiSupport.str(form,"password");
        ApiSupport.require(email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")&&name.length()>=2&&password.length()>=8,"Provide a valid email, name and password (8+ chars)");
        ApiSupport.require(users.findByEmailIgnoreCase(email).isEmpty(),"Email already registered");
        AppUser user=new AppUser();user.name=name;user.email=email;user.passwordHash=encoder.encode(password);users.save(user);
        return Map.of("token",security.createToken(user),"user",ApiSupport.user(user));
    }
    @PostMapping("/auth/login") public Map<String,Object> login(@RequestBody Map<String,Object> form){
        AppUser u=users.findByEmailIgnoreCase(ApiSupport.str(form,"email")).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));
        if(!u.active||!encoder.matches(ApiSupport.str(form,"password"),u.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
        u.sessionKey=java.util.UUID.randomUUID().toString();users.save(u);return Map.of("token",security.createToken(u),"user",ApiSupport.user(u));
    }
    @GetMapping("/auth/me") public Map<String,Object> me(Authentication a){return ApiSupport.user(helper.me(a));}
    @PatchMapping("/auth/me") public Map<String,Object> profile(Authentication a,@RequestBody Map<String,Object> f){
        AppUser u=helper.me(a);
        for(String s:List.of("name","headline","bio","city","skills","avatar")){
            if(!f.containsKey(s))continue;
            String v=ApiSupport.str(f,s);ApiSupport.require(v.length()<= (s.equals("bio")||s.equals("skills")?1000:s.equals("avatar")?500:180),"Field is too long");if(s.equals("name"))ApiSupport.require(v.length()>=2,"Name must be at least 2 characters");if(s.equals("avatar"))ApiSupport.require(v.isEmpty()||v.startsWith("https://"),"Use a secure HTTPS avatar URL");
            switch(s){case "name"->u.name=v;case "headline"->u.headline=v;case "bio"->u.bio=v;case "city"->u.city=v;case "skills"->u.skills=v;case "avatar"->u.avatar=v;}
        }
        users.save(u);return ApiSupport.user(u);
    }
    @PostMapping("/auth/password") public Map<String,Object> password(Authentication a,@RequestBody Map<String,Object> f){
        AppUser u=helper.me(a);String current=ApiSupport.str(f,"currentPassword"),next=ApiSupport.str(f,"newPassword");
        if(!encoder.matches(current,u.passwordHash))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Current password is incorrect");
        ApiSupport.require(next.length()>=8&&next.length()<=128,"Password must contain 8-128 characters");
        u.passwordHash=encoder.encode(next);u.sessionKey=java.util.UUID.randomUUID().toString();users.save(u);return Map.of("changed",true);
    }
    @PostMapping("/auth/logout") public Map<String,Boolean> logout(Authentication a){AppUser u=helper.me(a);u.sessionKey=UUID.randomUUID().toString();users.save(u);return Map.of("signedOut",true);}
    @PostMapping("/auth/mentor-apply") public Map<String,Object> apply(Authentication a,@RequestBody Map<String,Object> f){
        AppUser u=helper.me(a);ApiSupport.require(ApiSupport.str(f,"bio").length()>=20,"Tell us about your teaching experience (20+ characters)");
        ApiSupport.require(u.mentorDocumentKey!=null && !u.mentorDocumentKey.isBlank() && u.mentorDocumentVerified,"Verified identity document required");
        ApiSupport.require(u.mentorBankLast4!=null && !u.mentorBankLast4.isBlank() && u.mentorBankVerified,"Verified bank details required");
        ApiSupport.require(u.mentorAgreedAt!=null && "2026-11".equals(u.mentorAgreementVersion),"Sign the current mentor agreement first");
        u.bio=ApiSupport.str(f,"bio");u.headline=ApiSupport.str(f,"headline");u.skills=ApiSupport.str(f,"skills");u.mentorRequested=true;users.save(u);
        return Map.of("status",u.mentor?"APPROVED":"UNDER_REVIEW","message","The admin can review your mentor profile.");
    }
}
