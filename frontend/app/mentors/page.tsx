'use client';
import {useEffect,useState} from 'react';import Shell from '@/components/Shell';import MentorCard from '@/components/MentorCard';import SkillFilters from '@/components/SkillFilters';import Empty from '@/components/Empty';import {api} from '@/lib/api';import {User} from '@/lib/types';import {matchingCategory,matchingSubcategory} from '@/lib/skills';
export default function Mentors(){
 const [mentors,setMentors]=useState<User[]>([]),[query,setQuery]=useState(''),[category,setCategory]=useState('All skills'),[topic,setTopic]=useState('All topics'),[sort,setSort]=useState('Best match'),[error,setError]=useState('');
 useEffect(()=>{api<User[]>('/catalog/mentors').then(setMentors).catch(e=>setError(e.message))},[]);
 const list=mentors.filter(m=>{const full=`${m.name} ${m.headline} ${m.skills} ${m.bio} ${m.city}`;return matchingCategory(m.skills,full,category)&&matchingSubcategory(full,topic)&&full.toLowerCase().includes(query.toLowerCase());}).sort((a,b)=>sort==='Price: low to high'?(a.hourlyRatePaise||50000)-(b.hourlyRatePaise||50000):sort==='Price: high to low'?(b.hourlyRatePaise||50000)-(a.hourlyRatePaise||50000):(b.performanceScore||100)-(a.performanceScore||100));
 return <Shell title="Top mentors"><span className="eyebrow">ONE-TO-ONE SUPPORT</span><h1 className="page-title">Find a mentor for your skill</h1><p className="page-subtitle">Filter by subject, see their profile and hourly price, then request your first trial lecture or a paid session.</p>
 <SkillFilters query={query} onQuery={setQuery} category={category} onCategory={setCategory} topic={topic} onTopic={setTopic} count={list.length} label="mentors"/>
 <div className="filters"><label style={{display:'flex',alignItems:'center',gap:9,margin:0}}>Sort mentors <select value={sort} onChange={e=>setSort(e.target.value)} style={{width:205}}><option>Best match</option><option>Price: low to high</option><option>Price: high to low</option></select></label></div>
 {error&&<div role="alert" className="error">{error}</div>}{list.length?<div className="grid-mentors">{list.map(m=><MentorCard key={m.id} mentor={m}/>)}</div>:<Empty title="No mentors found" description="Try clearing your filters or choose a broader category."/>}
 </Shell>;
}
