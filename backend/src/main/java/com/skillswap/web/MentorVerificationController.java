package com.skillswap.web;

import com.skillswap.model.AppUser;
import com.skillswap.repo.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Private onboarding files: never exposed by the general /api/files route. */
@RestController @RequestMapping("/api")
public class MentorVerificationController {
 private final ApiSupport helper;private final AppUserRepository users;
 @Value("${app.storage-dir}") String storage;
 MentorVerificationController(ApiSupport helper,AppUserRepository users){this.helper=helper;this.users=users;}
 @PostMapping(value="/mentor/verification/document",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public Map<String,Object> document(Authentication a,@RequestPart("file")MultipartFile file)throws Exception{
  AppUser u=helper.me(a);
  ApiSupport.require(!u.mentor,"Already an approved mentor");
  ApiSupport.require(file.getSize()>0&&file.getSize()<=5L*1024*1024,"Identity document must be under 5MB");
  String mime=Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
  ApiSupport.require(Set.of("application/pdf","image/png","image/jpeg").contains(mime),"Upload PDF, JPEG or PNG");
  byte[] head=new byte[8];try(var in=file.getInputStream()){int n=in.read(head);ApiSupport.require(n>=4,"File is empty or invalid");}
  boolean valid=mime.equals("application/pdf")&&new String(head,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF") || mime.equals("image/png")&&head[0]==(byte)0x89&&head[1]==0x50&&head[2]==0x4e&&head[3]==0x47 || mime.equals("image/jpeg")&&head[0]==(byte)0xff&&head[1]==(byte)0xd8;
  ApiSupport.require(valid,"Document file header does not match its format");
  String suffix=mime.equals("application/pdf")?"pdf":mime.equals("image/png")?"png":"jpg";
  Path folder=Paths.get(storage,"mentor-private").toAbsolutePath().normalize();Files.createDirectories(folder);
  String key=UUID.randomUUID()+"."+suffix;Files.copy(file.getInputStream(),folder.resolve(key));
  if(u.mentorDocumentKey!=null&&!u.mentorDocumentKey.isBlank())Files.deleteIfExists(folder.resolve(u.mentorDocumentKey));
  u.mentorDocumentKey=key;u.mentorDocumentName=Optional.ofNullable(file.getOriginalFilename()).orElse("Identity document").replaceAll("[^a-zA-Z0-9._ -]","_");u.mentorDocumentVerified=false;users.save(u);
  return Map.of("uploaded",true,"verified",false);
 }
 @PostMapping("/mentor/verification/bank")public Map<String,Object> bank(Authentication a,@RequestBody Map<String,Object> f){
  AppUser u=helper.me(a);String number=ApiSupport.str(f,"accountNumber"),ifsc=ApiSupport.str(f,"ifsc").toUpperCase(Locale.ROOT);
  ApiSupport.require(number.matches("[0-9]{9,18}")&&ifsc.matches("[A-Z]{4}0[A-Z0-9]{6}"),"Enter a valid bank account number and IFSC");
  // No raw account number or bank credential is retained. Independent bank verification is mandatory.
  u.mentorBankLast4=number.substring(number.length()-4);u.mentorBankIfsc=ifsc;u.mentorBankVerified=false;users.save(u);
  return Map.of("submitted",true,"verified",false,"last4",u.mentorBankLast4);
 }
 @PostMapping("/mentor/verification/agreement")public Map<String,Object> agreement(Authentication a,@RequestBody Map<String,Object> f){
  AppUser u=helper.me(a);ApiSupport.require("2026-11".equals(ApiSupport.str(f,"version")),"Current agreement version is required");
  ApiSupport.require(u.name.trim().equalsIgnoreCase(ApiSupport.str(f,"signature")),"Type your full profile name to sign");
  ApiSupport.require(Boolean.parseBoolean(ApiSupport.str(f,"accepted")),"Review and accept the mentor agreement");
  u.mentorAgreementVersion="2026-11";u.mentorAgreedAt=Instant.now();users.save(u);
  return Map.of("signed",true,"version",u.mentorAgreementVersion,"signedAt",u.mentorAgreedAt);
 }
 @PatchMapping("/admin/mentor-verification/{id}")public Map<String,Object> review(Authentication a,@PathVariable Long id,@RequestBody Map<String,Object> f){
  helper.admin(a);AppUser u=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(f.containsKey("documentVerified")){ApiSupport.require(u.mentorDocumentKey!=null&&!u.mentorDocumentKey.isBlank(),"No document submitted");u.mentorDocumentVerified=Boolean.parseBoolean(ApiSupport.str(f,"documentVerified"));}
  if(f.containsKey("bankVerified")){ApiSupport.require(u.mentorBankLast4!=null&&!u.mentorBankLast4.isBlank(),"No bank submitted");u.mentorBankVerified=Boolean.parseBoolean(ApiSupport.str(f,"bankVerified"));}
  users.save(u);return ApiSupport.user(u);
 }
 @GetMapping("/admin/mentor-verification/{id}/document")public ResponseEntity<Resource> privateDocument(Authentication a,@PathVariable Long id){
  helper.admin(a);AppUser u=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(u.mentorDocumentKey==null||u.mentorDocumentKey.isBlank())throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  Path dir=Paths.get(storage,"mentor-private").toAbsolutePath().normalize();Path path=dir.resolve(u.mentorDocumentKey).normalize();
  if(!path.startsWith(dir)||!Files.exists(path))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  String mime=u.mentorDocumentKey.endsWith(".pdf")?"application/pdf":u.mentorDocumentKey.endsWith(".png")?"image/png":"image/jpeg";
  return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=identity-document."+u.mentorDocumentKey.substring(u.mentorDocumentKey.lastIndexOf('.')+1)).contentType(MediaType.parseMediaType(mime)).body(new FileSystemResource(path));
 }
}
