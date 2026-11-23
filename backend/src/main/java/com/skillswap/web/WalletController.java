package com.skillswap.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillswap.model.*;
import com.skillswap.repo.*;
import com.skillswap.service.WalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api")
public class WalletController {
 private final ApiSupport helper;private final WalletService wallet;private final WalletEntryRepository entries;private final CourseRepository courses;private final WithdrawalRepository withdrawals;private final SettlementRepository settlements;private final TopupOrderRepository topups;private final ObjectMapper json;
 @Value("${app.demo}") boolean demo;
 @Value("${app.razorpay-key-id}") String key;
 @Value("${app.razorpay-key-secret}") String secret;
 WalletController(ApiSupport helper,WalletService wallet,WalletEntryRepository entries,CourseRepository courses,WithdrawalRepository withdrawals,SettlementRepository settlements,TopupOrderRepository topups,ObjectMapper json){this.helper=helper;this.wallet=wallet;this.entries=entries;this.courses=courses;this.withdrawals=withdrawals;this.settlements=settlements;this.topups=topups;this.json=json;}
 @GetMapping("/wallet")public Map<String,Object> balance(Authentication a){AppUser u=helper.me(a);return Map.of("balancePaise",u.balancePaise,"lifetimeEarningsPaise",u.lifetimeEarningsPaise,"transactions",entries.findByUserIdOrderByCreatedAtDesc(u.id).stream().map(ApiSupport::entry).toList(),"demo",demo,"gatewayEnabled",!key.isBlank()&&!secret.isBlank(),"withdrawablePaise",availableForWithdrawal(u),"withdrawals",withdrawals.findByUserIdOrderByCreatedAtDesc(u.id).stream().map(w->Map.of("id",w.id,"amountPaise",w.amountPaise,"status",w.status,"createdAt",w.createdAt)).toList());}
 private long availableForWithdrawal(AppUser u){
  long processed=withdrawals.findByUserIdOrderByCreatedAtDesc(u.id).stream().filter(w->!"REJECTED".equals(w.status)).mapToLong(w->w.amountPaise).sum();
  return Math.max(0,Math.min(u.balancePaise,u.lifetimeEarningsPaise-processed));
 }
 @PostMapping("/wallet/demo-topup")public Map<String,Object> topup(Authentication a,@RequestBody Map<String,Object> f){return wallet.demoTopup(helper.me(a).id,ApiSupport.num(f,"amountPaise",0),demo);}
 @GetMapping("/checkout/{courseId}")public Map<String,Object> quote(@PathVariable Long courseId){Course c=courses.findById(courseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));return Map.of("course",ApiSupport.course(c),"quote",wallet.quote(c));}
 @PostMapping("/checkout/{courseId}")public Map<String,Object> buy(Authentication a,@PathVariable Long courseId){return wallet.buy(helper.me(a).id,courseId);}
 @GetMapping("/mentor/settlements")public List<Map<String,Object>> settlements(Authentication a){AppUser u=helper.mentor(a);return settlements.findByMentorIdOrderByCreatedAtDesc(u.id).stream().map(s->Map.<String,Object>of("id",s.id,"course",s.course.title,"basePaise",s.basePaise,"courseTaxPaise",s.courseTaxPaise,"commissionPaise",s.commissionPaise,"commissionTaxPaise",s.commissionTaxPaise,"mentorNetPaise",s.mentorNetPaise,"createdAt",s.createdAt)).toList();}
 @PostMapping("/wallet/withdrawals") @Transactional public Map<String,Object> withdraw(Authentication a,@RequestBody Map<String,Object> f){
  AppUser principal=helper.me(a);AppUser u=wallet.lockUser(principal.id);long amount=ApiSupport.num(f,"amountPaise",0);String bank=ApiSupport.str(f,"bankDetails");
  ApiSupport.require(u.mentor,"Only approved mentors can withdraw earnings");ApiSupport.require(amount>=10000&&amount<=availableForWithdrawal(u),"Minimum ₹100 and sufficient eligible mentor earnings required");ApiSupport.require(bank.length()>=8&&bank.length()<=200,"Enter bank or payout details");
  // Withdrawal is held for admin review, not transferred automatically.
  Withdrawal w=new Withdrawal();w.user=u;w.amountPaise=amount;w.bankDetails=bank;withdrawals.save(w);
  // Row-level lock serializes concurrent withdrawals for this wallet.
  u.balancePaise-=amount;WalletEntry e=new WalletEntry();e.user=u;e.changePaise=-amount;e.balanceAfterPaise=u.balancePaise;e.type="WITHDRAWAL_HOLD";e.description="Withdrawal request #"+w.id;e.reference="WD-"+w.id;entries.save(e);
  // saved by JPA dirty checking
  return Map.of("id",w.id,"status",w.status,"remainingPaise",u.balancePaise);
 }
 @GetMapping("/wallet/withdrawals")public List<Map<String,Object>> withdrawals(Authentication a){AppUser u=helper.me(a);return withdrawals.findByUserIdOrderByCreatedAtDesc(u.id).stream().map(w->Map.<String,Object>of("id",w.id,"amountPaise",w.amountPaise,"status",w.status,"createdAt",w.createdAt)).toList();}
 @PostMapping("/wallet/gateway/order")@Transactional public Map<String,Object> createOrder(Authentication a,@RequestBody Map<String,Object> f)throws Exception{
  AppUser u=helper.me(a);if(key.isBlank()||secret.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Razorpay is not configured");long amount=ApiSupport.num(f,"amountPaise",0);ApiSupport.require(amount>=10000&&amount<=10000000,"Invalid amount");
  String receipt="ss"+UUID.randomUUID().toString().replace("-","").substring(0,22);
  JsonNode result=razorpay("POST","https://api.razorpay.com/v1/orders",json.writeValueAsString(Map.of("amount",amount,"currency","INR","receipt",receipt)));
  TopupOrder order=new TopupOrder();order.user=u;order.amountPaise=amount;order.gatewayOrderId=result.get("id").asText();topups.save(order);
  return Map.of("keyId",key,"orderId",order.gatewayOrderId,"amountPaise",amount,"currency","INR");
 }
 @PostMapping("/wallet/gateway/verify")@Transactional public Map<String,Object> verify(Authentication a,@RequestBody Map<String,Object> f)throws Exception{
  AppUser u=helper.me(a);String orderId=ApiSupport.str(f,"orderId"),paymentId=ApiSupport.str(f,"paymentId"),signature=ApiSupport.str(f,"signature");
  TopupOrder order=topups.findByGatewayOrderId(orderId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
  if(!order.user.id.equals(u.id))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  if("PAID".equals(order.status))return Map.of("paid",true,"alreadyCredited",true);
  ApiSupport.require(!secret.isBlank()&&!signature.isBlank(),"Gateway not configured");
  String expected=hmac(orderId+"|"+paymentId);
  ApiSupport.require(java.security.MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),signature.getBytes(StandardCharsets.UTF_8)),"Payment signature invalid");
  JsonNode p=razorpay("GET","https://api.razorpay.com/v1/payments/"+paymentId,null);
  ApiSupport.require("captured".equals(p.path("status").asText())&&p.path("amount").asLong()==order.amountPaise&&orderId.equals(p.path("order_id").asText()),"Payment not captured or mismatch");
  order.status="PAID";order.gatewayPaymentId=paymentId;topups.save(order);wallet.gatewayTopup(u.id,order.amountPaise,paymentId);
  return Map.of("paid",true,"alreadyCredited",false);
 }
 private String hmac(String value)throws Exception {Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));}
 private JsonNode razorpay(String method,String url,String body)throws Exception{
  String credentials=Base64.getEncoder().encodeToString((key+":"+secret).getBytes(StandardCharsets.UTF_8));
  HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(url)).header("Authorization","Basic "+credentials).header("Content-Type","application/json");
  HttpRequest req="POST".equals(method)?b.POST(HttpRequest.BodyPublishers.ofString(body)).build():b.GET().build();
  HttpResponse<String> res=HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString());
  if(res.statusCode()<200||res.statusCode()>=300)throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Razorpay request failed: "+res.statusCode());
  return json.readTree(res.body());
 }
}
