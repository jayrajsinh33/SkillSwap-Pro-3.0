export const SKILL_GROUPS:Record<string,string[]>={
 'Coding & IT':['Python','Java','JavaScript','TypeScript','React','Next.js','Node.js','C','C++','C#','PHP','Django','Spring Boot','HTML & CSS','SQL','DevOps','Cybersecurity','Mobile apps'],
 'Data & AI':['Data analysis','Excel','Power BI','Machine learning','Deep learning','Generative AI','Statistics','Data engineering'],
 'Design':['Graphic design','UI/UX','Figma','Canva','Photoshop','Illustration','Interior design','Fashion design'],
 'Digital marketing':['SEO','Social media','Google Ads','Meta Ads','Content writing','Email marketing','E-commerce','Branding'],
 'Business & finance':['Entrepreneurship','Accounting','Tally','GST basics','Sales','Public speaking','Project management','Excel for business'],
 'Languages':['English speaking','Hindi','Gujarati','French','German','Spanish','Business communication'],
 'School subjects':['Mathematics','Science','Physics','Chemistry','Biology','Computer science','Social studies'],
 'Creative arts':['Photography','Videography','Video editing','Music','Drawing','Painting','Crafts','Animation'],
 'Career skills':['Resume writing','Interview prep','Communication','Leadership','Time management','Freelancing'],
 'Lifestyle & wellness':['Yoga','Fitness','Meditation','Cooking','Nutrition basics','Personal finance'],
 'Skilled trades':['Electrical basics','Electronics','Arduino','Automotive','Woodworking','Repair skills']
};
export const GROUP_LABELS=Object.keys(SKILL_GROUPS);
const aliases:Record<string,string[]>={
 'Coding & IT':['development','coding','programming','software','web','it'],
 'Data & AI':['data','analytics','ai','machine learning'],
 'Design':['design'],
 'Digital marketing':['marketing','seo'],
 'Business & finance':['business','finance'],
 'Languages':['language','english'],
 'School subjects':['academic','education','science','math'],
 'Creative arts':['creative','photo','art','video'],
 'Career skills':['career','communication'],
 'Lifestyle & wellness':['wellness','fitness'],
 'Skilled trades':['technical','electrical']
};
export function matchingCategory(category:string,body:string,chosen:string){
 if(chosen==='All skills')return true;
 const tokens=aliases[chosen]||[];
 const normalized=category.toLowerCase();
 if(chosen.toLowerCase()===normalized||tokens.some(t=>normalized.includes(t)))return true;
 const full=body.toLowerCase();return (SKILL_GROUPS[chosen]||[]).some(skill=>full.includes(skill.toLowerCase()));
}
export function matchingSubcategory(body:string,skill:string){return skill==='All topics'||body.toLowerCase().includes(skill.toLowerCase());}
