'use client';
import {useEffect,useState} from 'react';import Link from 'next/link';import Shell from '@/components/Shell';import CourseCard from '@/components/CourseCard';import MentorCard from '@/components/MentorCard';import SkillFilters from '@/components/SkillFilters';
import {BookOpen,Code2,Palette,Brain,TrendingUp,Camera,BriefcaseBusiness,Languages,HeartPulse,School,GraduationCap,Hammer,ArrowRight} from 'lucide-react';
import {api} from '@/lib/api';import {Course,User} from '@/lib/types';import {GROUP_LABELS,SKILL_GROUPS,matchingCategory,matchingSubcategory} from '@/lib/skills';
const icons=[Code2,Brain,Palette,TrendingUp,BriefcaseBusiness,Languages,School,Camera,GraduationCap,HeartPulse,Hammer];
export default function Discover(){const [q,setQ]=useState(''),[category,setCategory]=useState('All skills'),[topic,setTopic]=useState('All topics'),[courses,setCourses]=useState<Course[]>([]),[mentors,setMentors]=useState<User[]>([]),[error,setError]=useState('');
 useEffect(()=>{Promise.all([api<Course[]>('/catalog/courses'),api<User[]>('/catalog/mentors')]).then(([c,m])=>{setCourses(c);setMentors(m)}).catch(e=>setError(e.message))},[]);
 const filtered=courses.filter(c=>{const body=`${c.title} ${c.description} ${c.tags} ${c.category}`;return matchingCategory(c.category,body,category)&&matchingSubcategory(body,topic)&&body.toLowerCase().includes(q.toLowerCase());});
 const matchedMentors=mentors.filter(m=>matchingCategory(m.skills,`${m.skills} ${m.headline} ${m.bio}`,category)&&matchingSubcategory(`${m.skills} ${m.headline} ${m.bio}`,topic));
 return <Shell title="Explore skills"><span className="eyebrow">YOUR NEXT SKILL STARTS HERE</span><h1 className="page-title">What would you like to learn?</h1><p className="page-subtitle">Tap a category below, pick your topic and choose a course or mentor. Everything is organised in simple steps.</p>
 <div className="skill-category-grid">{GROUP_LABELS.map((label,i)=>{const Icon=icons[i];return <button key={label} className={`skill-category-btn ${category===label?'selected':''}`} onClick={()=>{setCategory(label);setTopic('All topics')}}><Icon size={24}/><span>{label}<small>{SKILL_GROUPS[label].length} topics</small></span></button>})}</div>
 <SkillFilters query={q} onQuery={setQ} category={category} onCategory={setCategory} topic={topic} onTopic={setTopic} count={filtered.length} label="courses"/>
 {error&&<div className="error">{error}</div>}
 <div className="section-heading"><h2>Courses you can start</h2><Link href={`/courses?category=${encodeURIComponent(category)}`}>See all <ArrowRight size={16}/></Link></div>
 {filtered.length?<div className="grid-courses">{filtered.slice(0,6).map(c=><CourseCard key={c.id} course={c}/>)}</div>:<div className="panel muted">No courses match this topic yet. Select All skills to see everything available.</div>}
 <div className="section-heading" style={{marginTop:30}}><h2>People who can teach you</h2><Link href="/mentors">Browse all mentors <ArrowRight size={16}/></Link></div>
 {matchedMentors.length?<div className="grid-mentors">{matchedMentors.slice(0,3).map(m=><MentorCard key={m.id} mentor={m}/>)}</div>:<div className="panel muted">No mentor with this topic has been listed yet.</div>}
 <div className="action-steps"><Link href="/courses" className="action-step"><span className="step-number">1</span><strong><BookOpen size={17} style={{display:'inline'}}/> Learn from videos</strong><p>Purchase once and learn from published materials.</p></Link><Link href="/mentors" className="action-step"><span className="step-number">2</span><strong>Talk to a mentor</strong><p>Ask questions with a one-to-one class.</p></Link><Link href="/classes" className="action-step"><span className="step-number">3</span><strong>Join a live class</strong><p>Choose your hours and book a time.</p></Link></div>
 </Shell>;
}
