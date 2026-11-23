'use client';
import {Search,SlidersHorizontal,RotateCcw} from 'lucide-react';
import {GROUP_LABELS,SKILL_GROUPS} from '@/lib/skills';
export default function SkillFilters({query,onQuery,category,onCategory,topic,onTopic,count,label='results'}:{query:string;onQuery:(s:string)=>void;category:string;onCategory:(s:string)=>void;topic:string;onTopic:(s:string)=>void;count:number;label?:string}){
 const topics=category==='All skills'?[]:SKILL_GROUPS[category]||[];
 return <section className="skill-filter-panel" aria-label="Find skills and filter results"><div className="skill-filter-heading"><SlidersHorizontal size={19}/><strong>Find exactly what you need</strong><span>{count} {label}</span></div>
 <div className="skill-filter-fields"><label className="skill-filter-search"><span>Search by name or skill</span><div className="search-field"><Search size={18}/><input value={query} onChange={e=>onQuery(e.target.value)} placeholder="e.g. Python, English, Photoshop" aria-label="Search by name or skill"/></div></label><label><span>Choose a category</span><select value={category} onChange={e=>{onCategory(e.target.value);onTopic('All topics')}}><option>All skills</option>{GROUP_LABELS.map(c=><option key={c}>{c}</option>)}</select></label><label><span>Choose a topic</span><select value={topic} disabled={category==='All skills'} onChange={e=>onTopic(e.target.value)}><option>All topics</option>{topics.map(x=><option key={x}>{x}</option>)}</select></label><button className="btn btn-outline skill-reset" type="button" onClick={()=>{onQuery('');onCategory('All skills');onTopic('All topics')}}><RotateCcw size={17}/> Clear filters</button></div>
 </section>;
}
