'use client';
import {use,useEffect,useState} from 'react';
import Link from 'next/link';
import {Video,VideoOff,ExternalLink,Headphones,ShieldCheck,MessageSquare,BookOpen} from 'lucide-react';
import Shell from '@/components/Shell';
import {api} from '@/lib/api';
import {useAuth} from '@/lib/auth';

type Room={roomName:string;displayName:string;videoProvider:string;role?:"MENTOR"|"STUDENT";lectureId?:string};
export default function Classroom({params}:{params:Promise<{kind:string;id:string}>}){
 const {kind,id}=use(params);const {user}=useAuth();const [room,setRoom]=useState<Room|null>(null),[joined,setJoined]=useState(false),[error,setError]=useState(''),[busy,setBusy]=useState(true);
 const valid=kind==='booking'||kind==='group';
 useEffect(()=>{if(!user?.id)return;if(!valid){setError('Unsupported classroom type.');setBusy(false);return;}setBusy(true);api<Room>(kind==='booking'?`/bookings/${id}/join`:`/group-classes/${id}/join`).then(x=>{setRoom(x);setError('')}).catch(e=>setError(e.message)).finally(()=>setBusy(false));},[user?.id,kind,id,valid]);
 const roomUrl=room?`https://meet.jit.si/${encodeURIComponent(room.roomName.replace(/[^a-zA-Z0-9-]/g,''))}`:'';
 return <Shell title="Live classroom"><div className="breadcrumbs"><Link href="/classes">Live classes</Link> / Classroom</div><span className="eyebrow">SKILLSWAP LIVE LEARNING</span><h1 className="page-title">Your video classroom.</h1><p className="page-subtitle">Join your mentor face to face. Mentors can demonstrate work; students can watch and ask questions.</p>
 {error&&<div role="alert" className="error">{error} <Link href="/bookings">View bookings</Link></div>}
 {busy&&<div className="panel">Checking your booking and classroom access…</div>}
 {room&&<><div className="classroom-toolbar panel"><div><strong>{kind==='booking'?'Personal mentoring session':'Live group workshop'}</strong><p className="muted" style={{margin:0}}>Logged in as {room.displayName}. Camera and microphone permissions are managed by your browser. <strong>{room.role==='MENTOR'?'Mentor hosting':'Student attending'}</strong> {room.lectureId&&<span className="lecture-id">· {room.lectureId}</span>}</p></div><div className="study-actions">{joined?<button className="btn btn-outline" onClick={()=>setJoined(false)}><VideoOff size={17}/> Leave classroom</button>:<button className="btn btn-primary" onClick={()=>setJoined(true)}><Video size={17}/> Enter video class</button>}{room.role==='MENTOR'&&<a className="btn btn-outline" href={roomUrl} target="_blank" rel="noopener noreferrer"><ExternalLink size={17}/> Open externally</a>}</div></div>
 {joined?<div className="classroom-frame"><iframe src={`${roomUrl}#config.prejoinPageEnabled=true`} title="SkillSwap live video classroom" allow={room.role==='MENTOR'?"camera; microphone; fullscreen; display-capture; autoplay; clipboard-write":"camera; microphone; fullscreen; autoplay; clipboard-write"} allowFullScreen referrerPolicy="strict-origin-when-cross-origin"/></div>:<div className="classroom-welcome panel"><Video size={48}/><h2>Ready to join the class?</h2><p>Click <strong>Enter video class</strong> above, check your camera and microphone, and enter your name. Mentors may open the external room if embedded video is blocked.</p><div className="classroom-checks"><span><Headphones size={18}/> Use headphones for clear audio</span><span><ShieldCheck size={18}/> Only authorized enrolled users can request the room link</span></div></div>}
 <div className="grid-2" style={{marginTop:20}}><Link href="/messages" className="panel classroom-action"><MessageSquare size={25}/><div><strong>Course chat</strong><p className="muted">Ask questions and share your notes with classmates.</p></div></Link><Link href="/my-learning" className="panel classroom-action"><BookOpen size={25}/><div><strong>Your course material</strong><p className="muted">Review curriculum and lesson notes alongside your class.</p></div></Link></div>
 <div className="notice-bar" style={{marginTop:20}}>This development build embeds a Jitsi test room. It needs internet access, browser camera/mic permission, and availability of the external Jitsi service. A production deployment needs a configured hosted video service, room moderation and participant controls.</div></>}
 </Shell>;
}
