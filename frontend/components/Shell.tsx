'use client';
import Link from 'next/link';
import {usePathname,useRouter} from 'next/navigation';
import {useState,useEffect} from 'react';
import {LayoutDashboard,BookOpen,GraduationCap,CalendarDays,Video,MessagesSquare,Wallet,BarChart3,UserRound,Settings,Bell,LifeBuoy,ShieldCheck,LogOut,Menu,X,Compass,PlusCircle,ChevronRight} from 'lucide-react';
import Brand from './Brand';
import BackendStatus from './BackendStatus';
import {useAuth} from '@/lib/auth';
import {photo} from '@/lib/types';
const menus=[
 {label:'Learn',items:[{href:'/dashboard',label:'Home',icon:LayoutDashboard},{href:'/discover',label:'Explore skills',icon:Compass},{href:'/courses',label:'Courses',icon:BookOpen},{href:'/mentors',label:'Mentors',icon:GraduationCap},{href:'/my-learning',label:'My learning',icon:BookOpen},{href:'/classes',label:'Live classes',icon:Video},{href:'/bookings',label:'Bookings',icon:CalendarDays}]},
 {label:'Connect',items:[{href:'/messages',label:'Messages',icon:MessagesSquare},{href:'/wallet',label:'Wallet',icon:Wallet},{href:'/earnings',label:'Earnings',icon:BarChart3},{href:'/mentor-studio',label:'Teach skills',icon:PlusCircle}]},
 {label:'Account',items:[{href:'/profile',label:'My profile',icon:UserRound},{href:'/settings',label:'Settings',icon:Settings},{href:'/notifications',label:'Notifications',icon:Bell},{href:'/support',label:'Help',icon:LifeBuoy}]}
];
export default function Shell({children,title}:{children:React.ReactNode;title?:string}){
 const {user,loading,logout}=useAuth();const pathname=usePathname();const router=useRouter();const [open,setOpen]=useState(false);
 useEffect(()=>{if(!loading&&!user)router.replace('/login?next='+encodeURIComponent(pathname));},[loading,user,pathname,router]);
 useEffect(()=>setOpen(false),[pathname]);
 if(loading)return <div className="app-loading">Opening SkillSwap…</div>;
 if(!user)return null;
 const all=menus.flatMap(x=>x.items);
 const active=(href:string)=>pathname===href||pathname.startsWith(href+'/');
 return <div className="app-shell horizontal-shell">
  <header className="skill-header">
   <div className="skill-header-main"><div className="skill-brand-link"><Brand /></div>
    <span className="skill-current">{title||'Your learning space'}</span>
    <div className="skill-header-actions"><Link className="skill-help" href="/support"><LifeBuoy size={17}/> Help</Link><Link href="/notifications" className="icon-button" aria-label="Notifications"><Bell size={19}/></Link><Link href="/profile" aria-label="Profile"><img className="avatar avatar-sm" src={photo(user.avatar)} alt="My profile"/></Link><button className="icon-button" aria-label="Open all navigation" aria-expanded={open} onClick={()=>setOpen(x=>!x)}>{open?<X size={21}/>:<Menu size={21}/>}</button></div>
   </div>
   <nav className="skill-nav" aria-label="Main navigation">{all.map(item=><Link key={item.href} className={`skill-nav-link ${active(item.href)?'active':''}`} href={item.href} aria-current={active(item.href)?'page':undefined}><item.icon size={16}/><span>{item.label}</span></Link>)}{user.admin&&<Link className={`skill-nav-link ${active('/admin')?'active':''}`} href="/admin"><ShieldCheck size={16}/> Admin</Link>}</nav>
  </header>
  {open&&<><button className="skill-menu-mask" aria-label="Close menu" onClick={()=>setOpen(false)}/><aside className="skill-menu-panel" aria-label="All pages"><div className="align-between"><h3>Where would you like to go?</h3><button onClick={()=>setOpen(false)} className="icon-button" aria-label="Close menu"><X size={19}/></button></div>{menus.map(group=><section key={group.label}><h4>{group.label}</h4><div className="skill-menu-links">{group.items.map(item=><Link href={item.href} key={item.href} className={active(item.href)?'current':''}><item.icon size={19}/>{item.label}<ChevronRight size={16}/></Link>)}</div></section>)}{user.admin&&<Link href="/admin" className="skill-menu-admin">Admin console</Link>}<button className="skill-menu-logout" onClick={logout}><LogOut size={18}/> Sign out</button></aside></>}
  <main className="main-content"><BackendStatus/>{children}</main>
  <nav className="mobile-tabbar" aria-label="Quick navigation">{[{href:'/dashboard',label:'Home',icon:LayoutDashboard},{href:'/courses',label:'Courses',icon:BookOpen},{href:'/classes',label:'Live',icon:Video},{href:'/messages',label:'Chat',icon:MessagesSquare},{href:'/profile',label:'Profile',icon:UserRound}].map(item=><Link href={item.href} key={item.href} className={active(item.href)?'active':''}><item.icon size={20}/>{item.label}</Link>)}</nav>
 </div>;
}
