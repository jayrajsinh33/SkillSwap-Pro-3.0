package com.skillswap.web;

import com.skillswap.model.AppUser;
import com.skillswap.repo.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class DirectoryController {
  private final AppUserRepository users;
  private final ApiSupport support;
  public DirectoryController(AppUserRepository users, ApiSupport support){this.users=users;this.support=support;}
  @GetMapping("/directory")
  public List<Map<String,Object>> directory(Authentication authentication,
      @RequestParam(value="q",defaultValue="") String query){
    AppUser current=support.me(authentication);
    String q=query.trim().toLowerCase(Locale.ROOT);
    if(q.length()>100)q=q.substring(0,100);
    final String term=q;
    return users.findAll().stream().filter(u->u.active&&!u.admin&&!u.id.equals(current.id))
      .filter(u->term.isEmpty()||String.join(" ",u.name,u.headline,u.skills,u.city).toLowerCase(Locale.ROOT).contains(term))
      .sorted(Comparator.comparing(u->u.name.toLowerCase(Locale.ROOT)))
      .limit(100).map(ApiSupport::publicUser).toList();
  }
}
