package com.skillswap.config;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LessonSeeder {
 private final CourseRepository courses;private final LessonRepository lessons;
 @Value("${app.demo}") boolean demo;
 LessonSeeder(CourseRepository courses,LessonRepository lessons){this.courses=courses;this.lessons=lessons;}
 @Transactional
 public void seed(){if(!demo)return;
  for(Course course:courses.findAll()){
   if(lessons.countByCourseId(course.id)>0)continue;
   String[] topics=topics(course.category);
   int count=Math.max(6,Math.min(course.lessons,15));
   for(int i=0;i<count;i++){
    String topic=topics[i%topics.length];Lesson lesson=new Lesson();lesson.course=course;lesson.position=i+1;
    lesson.title=String.format("%02d · %s",i+1,topic);
    lesson.content="COURSE: "+course.title+"\n\nLEARNING GOAL\nUnderstand "+topic.toLowerCase()+" and apply it to a practical "+course.category.toLowerCase()+" task.\n\nLESSON GUIDE\n1. Review the core ideas and define important terms.\n2. Study a realistic example from "+course.title+".\n3. Work through an independent exercise and document your approach.\n4. Share your questions with the mentor in Messages or during a live session.\n\nPRACTICE ACTIVITY\nCreate a short deliverable that demonstrates "+topic.toLowerCase()+". Explain your decisions, what worked, and what you would improve.\n\nMENTOR NOTE\nThis is editable demo starter content. Your mentor can expand it with original lessons, resources and assignments.";
    lesson.estimatedMinutes=25+(i%3)*10;lessons.save(lesson);
   }
   course.lessons=count;courses.save(course);
  }
 }
 private String[] topics(String category){return switch(category){
  case "Development" -> new String[]{"Tools and workspace setup","Requirements and project planning","Core programming concepts","Components and architecture","Build the first feature","Data flow and state","Testing and debugging","Security and best practices","Deployment fundamentals","Final practical project","Code review and improvements","Portfolio presentation"};
  case "Data & AI" -> new String[]{"Introduction and key terminology","Prepare the learning environment","Collect and inspect data","Clean and transform data","Analyze trends and patterns","Build your first model","Evaluate results","Interpret responsibly","Visualize insights","Mini capstone project","Validate and present findings"};
  case "Design" -> new String[]{"Understand the design brief","Research users and needs","Explore competitor patterns","Information architecture","Wireframes and user flows","Visual hierarchy and typography","Color and accessibility","Components and design systems","Interactive prototyping","Usability testing","Design handoff","Portfolio case study"};
  case "Marketing" -> new String[]{"Define goals and audience","Customer research","Positioning and messages","Content planning","Channels and campaigns","Campaign execution","Analytics and measurement","Conversion optimization","Budget and ROI","Report and iteration","Final marketing plan"};
  case "Creative" -> new String[]{"Explore your creative brief","Tools and techniques","Composition and storytelling","Lighting and visual balance","Practice with examples","Develop personal style","Editing and refinement","Share and receive feedback","Publish your portfolio"};
  default -> new String[]{"Set learning objectives","Core fundamentals","Plan your approach","Practice key techniques","Real-world case study","Communicate and collaborate","Solve practical problems","Review and improve","Present your final project"};
 };}
}
