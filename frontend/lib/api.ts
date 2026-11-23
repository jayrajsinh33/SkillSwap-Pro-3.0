const BASE=process.env.NEXT_PUBLIC_API_URL||'http://localhost:8080/api';
export function token(){if(typeof window==='undefined')return '';return sessionStorage.getItem('skillswap_token')||'';}
export class ApiError extends Error{constructor(public status:number,message:string){super(message);this.name='ApiError';}}
export async function api<T=any>(endpoint:string,options:RequestInit={}):Promise<T>{
 const hdrs:Record<string,string>={...(options.headers as Record<string,string>||{})};
 if(token())hdrs.Authorization=`Bearer ${token()}`;
 if(options.body&&!(options.body instanceof FormData))hdrs['Content-Type']='application/json';
 let response:Response;
 try{response=await fetch(`${BASE}${endpoint}`,{...options,headers:hdrs,cache:'no-store'});}catch{throw new ApiError(0,'Backend not reachable. Start Spring Boot on port 8080.');}
 const data=await response.json().catch(()=>null);
 if(!response.ok)throw new ApiError(response.status,data?.detail||data?.message||data?.error||(response.status===403?'Access denied. Please sign out and sign in again if this is your account.':response.status===401?'Session expired. Please sign in again.':`Request failed: ${response.status}`));
 return data as T;
}
export const post=<T=any>(endpoint:string,body:object={})=>api<T>(endpoint,{method:'POST',body:JSON.stringify(body)});
export const patch=<T=any>(endpoint:string,body:object)=>api<T>(endpoint,{method:'PATCH',body:JSON.stringify(body)});
export async function downloadFile(id:number,filename:string){
 const result=await fetch(`${BASE}/files/${id}`,{headers:{Authorization:`Bearer ${token()}`}});if(!result.ok)throw new Error('Could not download file');
 const blob=await result.blob();const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download=filename;document.body.appendChild(a);a.click();a.remove();URL.revokeObjectURL(url);
}

export async function uploadAsset(file:File,params:Record<string,string>){
 const form=new FormData();form.append('file',file);const qs=new URLSearchParams(params);
 const response=await fetch(`${BASE}/files?${qs.toString()}`,{method:'POST',headers:{Authorization:`Bearer ${token()}`},body:form});
 const data=await response.json().catch(()=>null);if(!response.ok)throw new ApiError(response.status,data?.detail||data?.message||'Upload failed');return data as {id:number;filename:string};
}
export async function authenticatedBlob(id:number){const r=await fetch(`${BASE}/files/${id}`,{headers:{Authorization:`Bearer ${token()}`}});if(!r.ok)throw new Error('You cannot access this lesson file.');return URL.createObjectURL(await r.blob());}
