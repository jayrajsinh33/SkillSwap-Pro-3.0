package com.skillswap.web;
import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
@RestController @RequestMapping("/api/group-classes")
public class GroupClassController {
 @Value("${app.demo}") private boolean demo;
 private final ApiSupport helper;private final GroupClassRepository sessions;private final CourseRepository courses;private final EnrollmentRepository enrollments;
 GroupClassController(ApiSupport helper,GroupClassRepository sessions,CourseRepository courses,EnrollmentRepository enrollments){this.helper=helper;this.sessions=sessions;this.courses=courses;this.enrollments=enrollments;}
 private Map<String,Object> view(GroupClass s,AppUser me){Map<String,Object> m=new LinkedHashMap<>();m.put("id",s.id);m.put("lectureId",s.lectureId==null?"":s.lectureId);m.put("title",s.title);m.put("description",s.description);m.put("courseId",s.course.id);m.put("courseTitle",s.course.title);m.put("mentorName",s.mentor.name);m.put("startAt",s.startAt);m.put("durationMinutes",s.durationMinutes);m.put("capacity",s.capacity);m.put("attendeeCount",s.attendees.size());m.put("registered",s.attendees.stream().anyMatch(u->u.id.equals(me.id)));m.put("host",s.mentor.id.equals(me.id)||me.admin);return m;}
 @GetMapping public List<Map<String,Object>> list(Authentication a){AppUser me=helper.me(a);return sessions.findAllByOrderByStartAtAsc().stream().filter(s->s.startAt.isAfter(Instant.now().minusSeconds(7200))).filter(s->s.course.published||s.mentor.id.equals(me.id)||me.admin).map(s->view(s,me)).toList();}
 @PostMapping public Map<String,Object> create(Authentication a,@RequestBody Map<String,Object> f){AppUser me=helper.mentor(a);Course c=courses.findById(ApiSupport.num(f,"courseId",0)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(c.mentor.id.equals(me.id)||me.admin,"Only this course's mentor can schedule");
  Instant start;try{start=Instant.parse(ApiSupport.str(f,"startAt"));}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Date must be ISO UTC");}
  ApiSupport.require(start.isAfter(Instant.now().plusSeconds(900))&&start.isBefore(Instant.now().plusSeconds(365L*86400)),"Schedule between 15 minutes and one year from now");String title=ApiSupport.str(f,"title");ApiSupport.require(title.length()>=5&&title.length()<=140,"Title requires 5–140 characters");
  int capacity=(int)ApiSupport.num(f,"capacity",30);ApiSupport.require(capacity>=2&&capacity<=100,"Capacity must be 2–100");GroupClass s=new GroupClass();s.course=c;s.mentor=me;s.title=title;s.description=ApiSupport.str(f,"description");s.startAt=start;s.durationMinutes=c.durationMinutes;s.capacity=capacity;s.lectureId="LEC-G-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);s.roomName="SkillSwap-Group-"+UUID.randomUUID().toString().replace("-","");sessions.save(s);return view(s,me);
 }
 @PostMapping("/{id}/register") @Transactional public Map<String,Object> register(Authentication a,@PathVariable Long id){AppUser me=helper.me(a);GroupClass s=sessions.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(s.startAt.isAfter(Instant.now()),"Registration is closed");ApiSupport.require(enrollments.existsByLearnerIdAndCourseId(me.id,s.course.id)||s.mentor.id.equals(me.id)||me.admin,"You must purchase the course before joining this class");if(s.attendees.stream().noneMatch(u->u.id.equals(me.id))){ApiSupport.require(s.attendees.size()<s.capacity,"This class is full");s.attendees.add(me);sessions.save(s);}return Map.of("registered",true,"attendeeCount",s.attendees.size());}
 @GetMapping("/{id}/join") public Map<String,String> join(Authentication a,@PathVariable Long id){AppUser me=helper.me(a);GroupClass s=sessions.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(s.attendees.stream().anyMatch(u->u.id.equals(me.id))||s.mentor.id.equals(me.id)||me.admin,"Register for this class first");if(!demo) ApiSupport.require(Instant.now().isAfter(s.startAt.minusSeconds(1800))&&Instant.now().isBefore(s.startAt.plusSeconds(s.durationMinutes*60L+1800)),"Room opens 30 minutes before the session");return Map.of("roomName",s.roomName,"displayName",me.name,"videoProvider","jitsi-dev","role",s.mentor.id.equals(me.id)?"MENTOR":"STUDENT","lectureId",s.lectureId==null?"":s.lectureId);}
}
