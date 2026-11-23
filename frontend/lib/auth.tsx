'use client';
import {createContext,useContext,useEffect,useState} from 'react';
import {api,post} from './api';
import type {User} from './types';
type Auth={user:User|null;loading:boolean;login:(email:string,password:string)=>Promise<void>;register:(name:string,email:string,password:string)=>Promise<void>;refresh:()=>Promise<void>;logout:()=>void};
const Context=createContext<Auth|null>(null);
export function AuthProvider({children}:{children:React.ReactNode}){
 const [user,setUser]=useState<User|null>(null);const [loading,setLoading]=useState(true);
 async function refresh(){try{setUser(await api<User>('/auth/me'));}catch{setUser(null);}finally{setLoading(false);}}
 useEffect(()=>{refresh();},[]);
 async function login(email:string,password:string){const res=await post<{token:string;user:User}>('/auth/login',{email,password});sessionStorage.setItem('skillswap_token',res.token);setUser(res.user);}
 async function register(name:string,email:string,password:string){const res=await post<{token:string;user:User}>('/auth/register',{name,email,password});sessionStorage.setItem('skillswap_token',res.token);setUser(res.user);}
 async function logout(){try{await post('/auth/logout')}catch{}sessionStorage.removeItem('skillswap_token');setUser(null);window.location.href='/login';}
 return <Context.Provider value={{user,loading,login,register,refresh,logout}}>{children}</Context.Provider>;
}
export function useAuth(){const v=useContext(Context);if(!v)throw new Error('AuthProvider missing');return v;}
