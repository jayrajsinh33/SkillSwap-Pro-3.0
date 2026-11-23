package com.skillswap.web;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/chats")
public class ChatController {
 private final ApiSupport helper;private final ConversationRepository chats;private final ChatMessageRepository messages;private final AppUserRepository users;private final CourseRepository courses;private final EnrollmentRepository enrollments;private final MaterialFileRepository files;
 ChatController(ApiSupport helper,ConversationRepository chats,ChatMessageRepository messages,AppUserRepository users,CourseRepository courses,EnrollmentRepository enrollments,MaterialFileRepository files){this.files=files;this.helper=helper;this.chats=chats;this.messages=messages;this.users=users;this.courses=courses;this.enrollments=enrollments;}
 private Map<String,Object> summary(Conversation c,AppUser current){return Map.of("id",c.id,"title",c.type.equals("DIRECT")?c.members.stream().filter(m->!m.id.equals(current.id)).map(m->m.name).findFirst().orElse(c.title):c.title,"type",c.type,"courseId",c.courseId==null?0:c.courseId,"members",c.members.stream().map(ApiSupport::publicUser).toList());}
 @GetMapping public List<Map<String,Object>> all(Authentication a){AppUser u=helper.me(a);return chats.forMember(u.id).stream().map(c->summary(c,u)).toList();}
 @PostMapping("/direct/{otherId}")public Map<String,Object> direct(Authentication a,@PathVariable Long otherId){AppUser u=helper.me(a);AppUser other=users.findById(otherId).filter(x->x.active).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(!u.id.equals(other.id),"Cannot message yourself");
  Conversation c=chats.forMember(u.id).stream().filter(x->x.type.equals("DIRECT")&&x.members.stream().anyMatch(m->m.id.equals(otherId))).findFirst().orElse(null);
  if(c==null){c=new Conversation();c.title="Conversation";c.members.add(u);c.members.add(other);chats.save(c);}return summary(c,u);
 }
 @PostMapping("/group/{courseId}")public Map<String,Object> group(Authentication a,@PathVariable Long courseId){AppUser u=helper.me(a);Course course=courses.findById(courseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(course.mentor.id.equals(u.id)||enrollments.existsByLearnerIdAndCourseId(u.id,courseId)||u.admin,"Enroll first");
  Conversation c=chats.findByCourseIdAndType(courseId,"GROUP").orElseGet(()->{Conversation n=new Conversation();n.type="GROUP";n.title=course.title+" • Community";n.courseId=courseId;n.members.add(course.mentor);return chats.save(n);});
  c.members.add(u);chats.save(c);return summary(c,u);
 }
 private Conversation access(Long id,AppUser user){Conversation c=chats.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(c.members.stream().noneMatch(x->x.id.equals(user.id))&&!user.admin)throw new ResponseStatusException(HttpStatus.FORBIDDEN);return c;}
 @GetMapping("/{id}/messages")public List<Map<String,Object>> messages(Authentication a,@PathVariable Long id){AppUser u=helper.me(a);access(id,u);return messages.findTop150ByConversationIdOrderByCreatedAtAsc(id).stream().map(x->Map.<String,Object>of("id",x.id,"sender",ApiSupport.publicUser(x.sender),"body",x.body,"attachmentId",x.attachmentId==null?0:x.attachmentId,"createdAt",x.createdAt)).toList();}
 @PostMapping("/{id}/messages")public Map<String,Object> send(Authentication a,@PathVariable Long id,@RequestBody Map<String,Object> f){AppUser u=helper.me(a);Conversation c=access(id,u);String body=ApiSupport.str(f,"body");long fileId=ApiSupport.num(f,"attachmentId",0);ApiSupport.require((!body.isEmpty()||fileId!=0)&&body.length()<=4000,"Message must contain text or attachment (max 4,000 chars)");
  if(fileId>0){MaterialFile attachment=files.findById(fileId).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Attachment not found"));ApiSupport.require(attachment.conversationId!=null&&attachment.conversationId.equals(id)&&attachment.owner.id.equals(u.id),"Attachment must be uploaded by you to this chat");}
  ChatMessage m=new ChatMessage();m.sender=u;m.conversation=c;m.body=body;if(fileId>0)m.attachmentId=fileId;messages.save(m);return Map.of("sent",true,"id",m.id);
 }
}
