package com.skillswap.config;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.util.*;

@Configuration
public class DemoData {
 @Bean CommandLineRunner seed(AppUserRepository users,CourseRepository courses,EnrollmentRepository enrollments,WalletEntryRepository entries,BookingRepository bookings,ConversationRepository conversations,ChatMessageRepository messages,GroupClassRepository groupClasses,LessonSeeder lessonSeeder,PasswordEncoder encoder,@Value("${app.demo}")boolean demo){return args->{
  if(!demo)return;
  if(users.count()>0){seedExtras(users,courses,groupClasses);lessonSeeder.seed();return;}
  String[] names={"Aarav Sharma","Riya Mehta","Ishita Patel","Kabir Verma","Neha Iyer","Vivek Rao"};
  String[] headlines={"Senior Full-Stack Engineer","Product Design Lead","Data Science Educator","Growth & Digital Marketing Strategist","Creative Photography Mentor","Communication & Career Coach"};
  String[] cities={"Bengaluru","Mumbai","Ahmedabad","Delhi","Pune","Hyderabad"};
  String[] photos={
   "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=800&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=800&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=800&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=800&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&auto=format&fit=crop&q=85"};
  String[] skills={"React,Next.js,TypeScript","Figma,UI/UX,Product Design","Python,Machine Learning,Data Analysis","SEO,Marketing,Growth","Photography,Lightroom,Composition","Public Speaking,Communication,Interviewing"};
  List<AppUser> mentors=new ArrayList<>();
  for(int i=0;i<names.length;i++){AppUser m=new AppUser();m.email="mentor"+(i+1)+"@skillswap.local";m.passwordHash=encoder.encode("Demo12345!");m.name=names[i];m.headline=headlines[i];m.city=cities[i];m.avatar=photos[i];m.mentor=true;m.skills=skills[i];m.bio="I help curious learners build real-world skills through hands-on teaching, practical projects, constructive feedback, and clear learning goals.";users.save(m);mentors.add(m);}
  AppUser learner=new AppUser();learner.name="Demo Learner";learner.email="student@skillswap.local";learner.passwordHash=encoder.encode("Demo12345!");learner.balancePaise=350000;learner.avatar="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=800&auto=format&fit=crop&q=85";learner.city="Ahmedabad";users.save(learner);
  WalletEntry credit=new WalletEntry();credit.user=learner;credit.changePaise=350000;credit.balanceAfterPaise=350000;credit.type="DEMO_TOPUP";credit.description="Initial demonstration wallet funds";credit.reference="DEMO-SEED";entries.save(credit);
  AppUser admin=new AppUser();admin.name="SkillSwap Administrator";admin.email="admin@skillswap.local";admin.passwordHash=encoder.encode("Demo12345!");admin.admin=true;admin.mentor=true;admin.avatar=photos[0];admin.headline="Platform operations";users.save(admin);
  String[] titles={"Modern Web Development with React","UI/UX Design: From Idea to Product","Python & Data Science Essentials","Performance Marketing Playbook","Photography & Visual Storytelling","Confident Public Speaking","Build APIs with Java Spring Boot","Figma Design Systems Masterclass","Machine Learning Foundations","Content Strategy & Personal Branding"};
  String[] descriptions={"Build professional modern websites with React, TypeScript and reusable components. Create a portfolio-ready project from scratch.","Learn research, wireframes, prototyping, interaction design and polished interfaces in Figma.","Explore Python data structures, Pandas, practical analysis and data visualization with real datasets.","Master SEO, social campaigns, analytics, conversion funnels and sustainable customer acquisition.","Develop your photographic eye, understand lighting and compose confident visual stories.","Improve communication through guided practice, feedback, interviews and compelling presentations.","Create reliable REST APIs with Java 21, Spring Boot and PostgreSQL.","Build reusable component libraries, design tokens and scalable collaboration workflows.","Understand the intuition behind supervised learning and build your first practical models.","Create content systems that communicate clearly and help audiences trust your work."};
  String[] cats={"Development","Design","Data & AI","Marketing","Creative","Business","Development","Design","Data & AI","Marketing"};
  String[] covers={
   "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1558655146-9f40138edfeb?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1545235617-9465d2a55698?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1456324504439-367cee3b3c32?w=1200&auto=format&fit=crop&q=85"};
  List<Course> seeded=new ArrayList<>();
  for(int i=0;i<titles.length;i++){Course c=new Course();c.title=titles[i];c.description=descriptions[i];c.category=cats[i];c.mentor=mentors.get(i%6);c.cover=covers[i];c.tags=skills[i%6];c.pricePaise=(499+(i%4)*200)*100;c.lessons=6+i;c.durationMinutes=60;c.level=i%3==0?"Beginner":"All levels";c.published=true;c.rating=4.7+(i%4)*0.1;courses.save(c);seeded.add(c);}
  Enrollment e=new Enrollment();e.learner=learner;e.course=seeded.get(0);e.progress=48;e.amountPaidPaise=0;enrollments.save(e);
  Booking b=new Booking();b.course=seeded.get(0);b.learner=learner;b.mentor=mentors.get(0);b.startAt=Instant.now().plusSeconds(86400);b.lectureId="LEC-DEMO-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);b.roomName="SkillSwap-Demo-"+UUID.randomUUID().toString().replace("-","");b.status="CONFIRMED";bookings.save(b);
  Conversation c=new Conversation();c.title="Direct";c.members.add(learner);c.members.add(mentors.get(0));conversations.save(c);
  ChatMessage msg=new ChatMessage();msg.conversation=c;msg.sender=mentors.get(0);msg.body="Welcome to SkillSwap! Looking forward to our React session. Feel free to ask any questions here.";messages.save(msg);
  seedExtras(users,courses,groupClasses);
  lessonSeeder.seed();
 };}
 // Safe, repeatable demo content upgrades. Existing students, balances and enrollments stay untouched.
 private void seedExtras(AppUserRepository users,CourseRepository courses,GroupClassRepository groupClasses){
  String[] names={
   "Advanced TypeScript & Next.js","SQL & PostgreSQL for Developers","Java Backend Architecture",
   "Cybersecurity Fundamentals","DevOps with Docker & CI/CD","AI Automation with Python",
   "Digital Advertising & Analytics","Search Engine Optimization Mastery",
   "Mobile UI Design in Figma","Business Communication Workshop"};
  String[] categories={"Development","Data & AI","Development","Development","Development","Data & AI","Marketing","Marketing","Design","Business"};
  String[] descriptions={
   "Build production-ready TypeScript components, App Router pages, APIs and practical portfolio projects.",
   "Design relational schemas, write efficient SQL queries, and learn indexes, joins and transactions.",
   "Learn Spring Boot architecture, REST API design, validation, service layers and secure authentication.",
   "Understand safer password practices, web threats, access control, basic networking and secure applications.",
   "Containerize your apps, automate tests and deployment pipelines, and understand production environments.",
   "Create responsible automation scripts, data workflows and small Python AI integrations.",
   "Measure campaigns, build conversion funnels and interpret advertising dashboards with practical exercises.",
   "Research keywords, improve on-page content, and analyze organic search visibility step-by-step.",
   "Design polished mobile screens, interactive prototypes and reusable Figma components.",
   "Improve presentations, client communication, interviews and professional writing through practical feedback."};
  String[] covers={
   "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1456324504439-367cee3b3c32?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1558655146-9f40138edfeb?w=1200&auto=format&fit=crop&q=85",
   "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=1200&auto=format&fit=crop&q=85"};
  List<AppUser> mentors=new ArrayList<>();
  for(int i=1;i<=6;i++){users.findByEmailIgnoreCase("mentor"+i+"@skillswap.local").ifPresent(mentors::add);}
  if(mentors.isEmpty())return;
  Set<String> existing=new HashSet<>();for(Course c:courses.findAll())existing.add(c.title);
  for(int i=0;i<names.length;i++)if(!existing.contains(names[i])){
   Course c=new Course();c.title=names[i];c.description=descriptions[i];c.category=categories[i];c.cover=covers[i];
   c.mentor=mentors.get(i%mentors.size());c.tags=categories[i];c.pricePaise=(599+(i%4)*200)*100;
   c.lessons=10+i%6;c.durationMinutes=60;c.level=i%3==0?"Intermediate":"All levels";c.published=true;c.rating=4.6+(i%4)*.1;courses.save(c);
  }
  // Prepopulate workshops on both fresh and existing demo installations.
  if(groupClasses.findAllByOrderByStartAtAsc().stream().noneMatch(g->g.startAt.isAfter(Instant.now()))){
   AppUser learner=users.findByEmailIgnoreCase("student@skillswap.local").orElse(null);
   List<Course> all=courses.findAll();
   for(int i=0;i<Math.min(3,all.size());i++){
    Course c=all.get(i);GroupClass g=new GroupClass();g.course=c;g.mentor=c.mentor;
    g.title=(i==0?"React Live Code Lab":i==1?"Design Critique & Q&A":"Python Hands-on Workshop");
    g.description="Instructor-led interactive workshop with questions, practice and shared resources.";
    g.startAt=Instant.now().plusSeconds((i+1)*86400L);g.durationMinutes=60;g.capacity=30;
    g.lectureId="LEC-G-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);g.roomName="SkillSwap-Demo-Group-"+UUID.randomUUID().toString().replace("-","");
    if(i==0&&learner!=null)g.attendees.add(learner);
    groupClasses.save(g);
   }
  }
 }

}
