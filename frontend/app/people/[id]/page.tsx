'use client';
import {useEffect,useState} from 'react';
import {useParams} from 'next/navigation';
import Link from 'next/link';
import {MapPin,MessageSquare,GraduationCap,ShieldCheck} from 'lucide-react';
import {api} from '@/lib/api';
import {User,photo,money} from '@/lib/types';
import {useAuth} from '@/lib/auth';
import Brand from '@/components/Brand';
export default function PublicProfile(){
 const params=useParams();const {user}=useAuth();const [person,setPerson]=useState<User|null>(null),[error,setError]=useState('');
 useEffect(()=>{api<User>(`/catalog/profiles/${params.id}`).then(setPerson).catch(e=>setError(e.message))},[params.id]);
 return <div className="landing-container" style={{paddingTop:25,paddingBottom:50}}><div className="align-between" style={{marginBottom:26}}><Link href="/"><Brand/></Link><Link href={user?'/messages':'/login'} className="btn btn-outline">{user?'Go to messages':'Log in'}</Link></div>
 {error&&<div className="error">{error}</div>}
 {!person&&!error&&<p className="muted">Loading public profile…</p>}
 {person&&<><div className="panel"><div className="large-profile"><img src={photo(person.avatar)} className="avatar avatar-xl" alt={`${person.name} profile`}/><div style={{flex:1}}><span className="tag">{person.mentor?'SkillSwap mentor':'SkillSwap learner'}</span><h1 style={{margin:'10px 0'}}>{person.name}</h1><p className="muted">{person.headline}</p><div className="badge-inline"><MapPin size={16}/>{person.city}</div></div>{person.mentor&&<Link className="btn btn-primary" href={`/mentors/${person.id}`}>View mentor courses</Link>}</div></div>
 <div className="grid-2" style={{marginTop:18}}><section className="panel"><h2>About me</h2><p style={{whiteSpace:'pre-wrap'}}>{person.bio||'This member has not added a biography yet.'}</p></section><section className="panel"><h2>Skills and interests</h2><div className="study-actions">{person.skills?person.skills.split(',').filter(Boolean).map((x,i)=><span className="chip" key={i}>{x.trim()}</span>):<p className="muted">No skills listed yet.</p>}</div>{person.mentor&&<><div className="divider"/><p><ShieldCheck size={17} style={{display:'inline'}}/> Mentor score: {person.performanceScore??100}/100</p><p>Private learning from {money(person.hourlyRatePaise||0)} per hour.</p></>}</section></div>
 <p className="muted" style={{marginTop:15}}>For privacy, members' email addresses and payment information are not shown in public profiles.</p></>}
 </div>
}
