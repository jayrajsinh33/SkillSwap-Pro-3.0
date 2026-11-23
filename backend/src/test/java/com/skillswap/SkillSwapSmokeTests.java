package com.skillswap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:smoketests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.demo=true","app.jwt-secret=SMOKE_TEST_ONLY_NOT_FOR_DEPLOYMENT_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"})
@AutoConfigureMockMvc
public class SkillSwapSmokeTests {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;
 @Test void healthReturnsOk()throws Exception{mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));}
 @Test void publicCatalogIsSeeded()throws Exception{String result=mvc.perform(get("/api/catalog/courses")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();assertThat(mapper.readTree(result).size()).isGreaterThanOrEqualTo(20);}
 @Test void studentLoginAndWallet()throws Exception{
  String result=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"student@skillswap.local\",\"password\":\"Demo12345!\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty()).andReturn().getResponse().getContentAsString();
  String token=mapper.readTree(result).get("token").asText();
  mvc.perform(get("/api/wallet").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.balancePaise").isNumber());
  mvc.perform(get("/api/users/directory").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").isNotEmpty());
  String courseList=mvc.perform(get("/api/catalog/courses")).andReturn().getResponse().getContentAsString();
  long courseId=mapper.readTree(courseList).get(0).get("id").asLong();
  mvc.perform(get("/api/catalog/courses/"+courseId+"/syllabus")).andExpect(status().isOk()).andExpect(jsonPath("$[0].title").isNotEmpty());
  String enrolled=mvc.perform(get("/api/enrollments").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  long firstEnrolledCourse=mapper.readTree(enrolled).get(0).get("course").get("id").asLong();
  mvc.perform(get("/api/learning/course/"+firstEnrolledCourse).header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.lessons[0].title").isNotEmpty());
  mvc.perform(get("/api/group-classes").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$[0].title").isNotEmpty());
  mvc.perform(get("/api/admin/overview").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
 }
 @Test void oldLoginTokenIsRevokedOnNewLogin()throws Exception{
  String body="{\"email\":\"student@skillswap.local\",\"password\":\"Demo12345!\"}";
  String first=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  String token1=mapper.readTree(first).get("token").asText();
  String second=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  String token2=mapper.readTree(second).get("token").asText();
  mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token1)).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token2)).andExpect(status().isOk());
 }
 @Test void quoteUsesFivePercentCommissionAndFirstFreeHour()throws Exception{
  String body="{\"email\":\"student@skillswap.local\",\"password\":\"Demo12345!\"}";
  String login=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  String token=mapper.readTree(login).get("token").asText();
  String catalog=mvc.perform(get("/api/catalog/courses")).andReturn().getResponse().getContentAsString();
  long courseId=mapper.readTree(catalog).get(0).get("id").asLong();
  String quote=mvc.perform(get("/api/bookings/quote").header("Authorization","Bearer "+token).param("courseId",String.valueOf(courseId)).param("hours","2")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  JsonNode q=mapper.readTree(quote).path("quote");
  assertThat(q.path("commissionPaise").asLong()).isEqualTo(q.path("basePaise").asLong()*5/100);
  assertThat(q.path("freeHours").asLong()).isEqualTo(1);
 }
 @Test void mentorCannotApplyWithoutVerifiedDocuments()throws Exception{
  String email="test_"+java.util.UUID.randomUUID().toString().substring(0,16)+"@skillswap.local";
  String payload="{\"name\":\"Prospective Mentor\",\"email\":\""+email+"\",\"password\":\"Demo12345!\"}";
  String result=mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  String token=mapper.readTree(result).get("token").asText();
  mvc.perform(post("/api/auth/mentor-apply").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"bio\":\"Experienced teacher and software developer.\",\"headline\":\"Teacher\",\"skills\":\"Java\"}")).andExpect(status().isBadRequest());
 }
 @Test void publicMemberProfileIsAvailableAndProtectedDataExcluded()throws Exception{
  String catalog=mvc.perform(get("/api/catalog/mentors")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  long id=mapper.readTree(catalog).get(0).path("id").asLong();
  String result=mvc.perform(get("/api/catalog/profiles/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.bio").isNotEmpty()).andReturn().getResponse().getContentAsString();
  assertThat(mapper.readTree(result).has("email")).isFalse();
  assertThat(mapper.readTree(result).has("mentorDocumentKey")).isFalse();
 }
 @Test void recordedLessonsRejectAnonymousUsers()throws Exception{
  String catalog=mvc.perform(get("/api/catalog/courses")).andReturn().getResponse().getContentAsString();
  long id=mapper.readTree(catalog).get(0).path("id").asLong();
  mvc.perform(get("/api/learning/course/"+id)).andExpect(status().isUnauthorized());
 }

}
