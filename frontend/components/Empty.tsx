import {Inbox} from 'lucide-react';
export default function Empty({title='Nothing here yet',description='New activity will appear here.',children}:{title?:string;description?:string;children?:React.ReactNode}){return <div className="empty"><Inbox size={33}/><h3>{title}</h3><p>{description}</p>{children}</div>}
