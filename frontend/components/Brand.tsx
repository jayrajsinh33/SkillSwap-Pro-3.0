import Link from 'next/link';
export default function Brand({inverse=false}:{inverse?:boolean}){return <Link href="/" className="brand" style={{color:inverse?'white':undefined}}><span className="brand-mark">S</span>SkillSwap<span style={{color:'#48b8a9',marginLeft:-9}}>.</span></Link>}
