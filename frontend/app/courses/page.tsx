'use client';
import {useEffect,useState} from 'react';
import Shell from '@/components/Shell';import CourseCard from '@/components/CourseCard';import Empty from '@/components/Empty';import SkillFilters from '@/components/SkillFilters';
import {api} from '@/lib/api';import {Course} from '@/lib/types';import {matchingCategory,matchingSubcategory} from '@/lib/skills';
export default function Courses(){
 const [courses,setCourses]=useState<Course[]>([]),[q,setQ]=useState(''),[category,setCategory]=useState('All skills'),[topic,setTopic]=useState('All topics'),[loading,setLoading]=useState(true),[error,setError]=useState('');
 useEffect(()=>{const params=new URLSearchParams(window.location.search);const c=params.get('category');if(c)setCategory(c);api<Course[]>('/catalog/courses').then(setCourses).catch(e=>setError(e.message)).finally(()=>setLoading(false))},[]);
 const filtered=courses.filter(c=>{const body=`${c.title} ${c.description} ${c.mentor.name} ${c.tags} ${c.category}`;return matchingCategory(c.category,body,category)&&matchingSubcategory(body,topic)&&body.toLowerCase().includes(q.toLowerCase());});
 return <Shell title="Recorded courses"><span className="eyebrow">LEARN AT YOUR OWN SPEED</span><h1 className="page-title">Recorded courses</h1><p className="page-subtitle">Choose a skill, purchase a course and watch uploaded lessons at your own pace. Download PDF notes whenever your mentor adds them.</p>
 <div className="skill-tip"><strong>How it works:</strong> Find a course → View lessons and price → Buy the course → Watch the recorded lectures. Live classes are booked separately.</div>
 <SkillFilters query={q} onQuery={setQ} category={category} onCategory={setCategory} topic={topic} onTopic={setTopic} count={filtered.length} label="courses"/>
 {error&&<div role="alert" className="error">{error}</div>}
 {loading?<div className="panel">Loading available courses…</div>:filtered.length===0?<Empty title="No matching courses yet" description="Try another skill, choose All skills, or search with fewer words."/>:<div className="grid-courses">{filtered.map(c=><CourseCard key={c.id} course={c}/>)}</div>}
 </Shell>;
}
