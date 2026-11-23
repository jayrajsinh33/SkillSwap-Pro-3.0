'use client';
import {useState} from 'react';
export default function Notice(){const [message,setMessage]=useState('');return {message,setMessage,alert:message?<div role="alert" className="floating-alert" onClick={()=>setMessage('')}>{message} ✕</div>:null};}
