package com.skillswap.web;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import java.util.*;

@RestController @RequestMapping("/api/files")
public class FileController {
 private final ApiSupport helper;private final MaterialFileRepository files;private final EnrollmentRepository enrollments;private final CourseRepository courses;private final ConversationRepository chats;
 @Value("${app.storage-dir}")String storageDir;
 FileController(ApiSupport helper,MaterialFileRepository files,EnrollmentRepository enrollments,CourseRepository courses,ConversationRepository chats){this.helper=helper;this.files=files;this.enrollments=enrollments;this.courses=courses;this.chats=chats;}
 private boolean access(AppUser u,MaterialFile m){if(u.admin||m.owner.id.equals(u.id))return true;
  if(m.courseId!=null){Course c=courses.findById(m.courseId).orElse(null);if(c!=null&&(c.mentor.id.equals(u.id)||enrollments.existsByLearnerIdAndCourseId(u.id,m.courseId)))return true;}
  if(m.conversationId!=null)return chats.findById(m.conversationId).map(c->c.members.stream().anyMatch(x->x.id.equals(u.id))).orElse(false);
  return false;
 }
 @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)public Map<String,Object> upload(Authentication a,@RequestPart("file")MultipartFile file,@RequestParam(required=false)Long courseId,@RequestParam(required=false)Long conversationId)throws Exception {
  AppUser u=helper.me(a);ApiSupport.require((courseId!=null) != (conversationId!=null),"Choose exactly one course or chat");
  if(courseId!=null){Course c=courses.findById(courseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(c.mentor.id.equals(u.id)||u.admin,"Only course mentors can upload course materials");}
  if(conversationId!=null){Conversation c=chats.findById(conversationId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));ApiSupport.require(c.members.stream().anyMatch(x->x.id.equals(u.id)),"Not a chat member");}
  String mime=file.getContentType()==null?"":file.getContentType().toLowerCase(Locale.ROOT);
  ApiSupport.require(Set.of("application/pdf","image/jpeg","image/png","text/plain","application/vnd.openxmlformats-officedocument.wordprocessingml.document","video/mp4").contains(mime),"Allowed: PDF, PNG, JPG, TXT, DOCX");
  ApiSupport.require(file.getSize()>0&&file.getSize()<=(mime.equals("video/mp4")?100L:12L)*1024*1024,"Maximum 100 MB for MP4 and 12 MB for documents");
  String original=Optional.ofNullable(file.getOriginalFilename()).orElse("document").replaceAll("[^a-zA-Z0-9._ -]","_");
  String key=UUID.randomUUID()+".bin";Path folder=Paths.get(storageDir).toAbsolutePath().normalize();Files.createDirectories(folder);Path path=folder.resolve(key);
  Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
  MaterialFile m=new MaterialFile();m.owner=u;m.courseId=courseId;m.conversationId=conversationId;m.filename=original;m.mime=mime;m.storageKey=key;m.bytes=file.getSize();files.save(m);
  return Map.of("id",m.id,"filename",m.filename,"bytes",m.bytes);
 }
 @GetMapping("/course/{courseId}")public List<Map<String,Object>> courseFiles(Authentication a,@PathVariable Long courseId){AppUser u=helper.me(a);return files.findByCourseIdOrderByCreatedAtDesc(courseId).stream().filter(f->access(u,f)).map(f->Map.<String,Object>of("id",f.id,"filename",f.filename,"mime",f.mime,"bytes",f.bytes)).toList();}
 @GetMapping("/{id}")public ResponseEntity<Resource> download(Authentication a,@PathVariable Long id)throws Exception{AppUser u=helper.me(a);MaterialFile f=files.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(!access(u,f))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  Path path=Paths.get(storageDir).toAbsolutePath().normalize().resolve(f.storageKey);ApiSupport.require(Files.exists(path),"File not found on disk");
  return ResponseEntity.ok().contentType(MediaType.parseMediaType(f.mime)).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+f.filename.replace("\"","")+"\"").body(new FileSystemResource(path));
 }
}
