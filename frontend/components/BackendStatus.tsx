'use client';
import {useEffect,useState} from 'react';
import {AlertCircle,RefreshCw} from 'lucide-react';
import {api} from '@/lib/api';
export default function BackendStatus(){
 const [offline,setOffline]=useState(false),[checking,setChecking]=useState(false);
 async function ping(){setChecking(true);try{await api('/health');setOffline(false);}catch{setOffline(true);}finally{setChecking(false);}}
 useEffect(()=>{ping();const timer=setInterval(ping,45000);return()=>clearInterval(timer)},[]);
 if(!offline)return null;
 return <div className="api-offline" role="alert"><AlertCircle size={19}/><div><strong>Java backend is not responding.</strong><span> Start Spring Boot on localhost:8080. Your pages may open, but saved actions will not work until the API is connected.</span></div><button type="button" className="btn btn-outline btn-sm" disabled={checking} onClick={ping}><RefreshCw size={16}/> Retry</button></div>;
}
