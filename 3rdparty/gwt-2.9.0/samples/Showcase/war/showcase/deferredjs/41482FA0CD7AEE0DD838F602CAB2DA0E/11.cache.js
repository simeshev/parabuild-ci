$wnd.showcase.runAsyncCallback11("function FSb(){}\nfunction HSb(){}\nfunction ASb(a,b){a.b=b}\nfunction BSb(a){if(a==qSb){return true}ZA();return a==tSb}\nfunction CSb(a){if(a==pSb){return true}ZA();return a==oSb}\nfunction GSb(a){this.b=(hUb(),cUb).a;this.e=(mUb(),lUb).a;this.a=a}\nfunction ySb(a,b){var c;c=DP(a.fb,161);c.b=b.a;!!c.d&&sNb(c.d,b)}\nfunction zSb(a,b){var c;c=DP(a.fb,161);c.e=b.a;!!c.d&&uNb(c.d,b)}\nfunction uSb(){uSb=L9;nSb=new FSb;qSb=new FSb;pSb=new FSb;oSb=new FSb;rSb=new FSb;sSb=new FSb;tSb=new FSb}\nfunction DSb(){uSb();wNb.call(this);this.b=(hUb(),cUb);this.c=(mUb(),lUb);(eKb(),this.e)[Bpc]=0;this.e[Cpc]=0}\nfunction vSb(a,b,c){var d;if(c==nSb){if(b==a.a){return}else if(a.a){throw f9(new l9b('Only one CENTER widget may be added'))}}Sh(b);r2b(a.j,b);c==nSb&&(a.a=b);d=new GSb(c);b.fb=d;ySb(b,a.b);zSb(b,a.c);xSb(a);Uh(b,a)}\nfunction wSb(a){var b,c,d,e,f,g,h;$1b((eKb(),a.hb),'',crc);g=new _gc;h=new B2b(a.j);while(h.b<h.c.c){b=z2b(h);f=DP(b.fb,161).a;d=DP(dcc(rhc(g.d,f)),84);c=!d?1:d.a;e=f==rSb?'north'+c:f==sSb?'south'+c:f==tSb?'west'+c:f==oSb?'east'+c:f==qSb?'linestart'+c:f==pSb?'lineend'+c:hoc;$1b(Wo(b.hb),crc,e);pcc(g,f,y9b(c+1))}}\nfunction xSb(a){var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r;b=(eKb(),a.d);while(KLb(b)>0){Co(b,JLb(b,0))}o=1;e=1;for(i=new B2b(a.j);i.b<i.c.c;){d=z2b(i);f=DP(d.fb,161).a;f==rSb||f==sSb?++o:(f==oSb||f==tSb||f==qSb||f==pSb)&&++e}p=MO(n3,vlc,272,o,0,1);for(g=0;g<o;++g){p[g]=new HSb;p[g].b=$doc.createElement(zpc);yo(b,lKb(p[g].b))}k=0;l=e-1;m=0;q=o-1;c=null;for(h=new B2b(a.j);h.b<h.c.c;){d=z2b(h);j=DP(d.fb,161);r=$doc.createElement(Apc);j.d=r;j.d[npc]=j.b;j.d.style[opc]=j.e;j.d[Nlc]=j.f;j.d[Mlc]=j.c;if(j.a==rSb){hKb(p[m].b,r,p[m].a);yo(r,lKb(d.hb));r[mqc]=l-k+1;++m}else if(j.a==sSb){hKb(p[q].b,r,p[q].a);yo(r,lKb(d.hb));r[mqc]=l-k+1;--q}else if(j.a==nSb){c=r}else if(BSb(j.a)){n=p[m];hKb(n.b,r,n.a++);yo(r,lKb(d.hb));r[drc]=q-m+1;++k}else if(CSb(j.a)){n=p[m];hKb(n.b,r,n.a);yo(r,lKb(d.hb));r[drc]=q-m+1;--l}}if(a.a){n=p[m];hKb(n.b,c,n.a);yo(c,lKb(fh(a.a)))}}\nvar crc='cwDockPanel';K9(434,1,goc);_.Ec=function ysb(){var a,b,c;$bb(this.a,(a=new DSb,(eKb(),a.hb).className='cw-DockPanel',a.e[Bpc]=4,ASb(a,(hUb(),bUb)),vSb(a,new aRb(Yqc),(uSb(),rSb)),vSb(a,new aRb(Zqc),sSb),vSb(a,new aRb($qc),oSb),vSb(a,new aRb(_qc),tSb),vSb(a,new aRb(arc),rSb),vSb(a,new aRb(brc),sSb),b=new aRb(\"Voici un <code>panneau de d\\xE9filement<\\/code> situ\\xE9 au centre d'un <code>panneau d'ancrage<\\/code>. Si des contenus relativement volumineux sont ins\\xE9r\\xE9s au milieu de ce panneau \\xE0 d\\xE9filement et si sa taille est d\\xE9finie, il prend la forme d'une zone dot\\xE9e d'une fonction de d\\xE9filement \\xE0 l'int\\xE9rieur de la page, sans l'utilisation d'un IFRAME.<br><br>Voici un texte encore plus obscur qui va surtout servir \\xE0 faire d\\xE9filer cet \\xE9l\\xE9ment jusqu'en bas de sa zone visible. Sinon, il vous faudra r\\xE9duire ce panneau \\xE0 une taille minuscule pour pouvoir afficher ces formidables barres de d\\xE9filement!\"),c=new vOb(b),c.hb.style[Nlc]='400px',c.hb.style[Mlc]='100px',vSb(a,c,nSb),wSb(a),a))};K9(893,264,Slc,DSb);_.gc=function ESb(a){var b;b=qMb(this,a);if(b){a==this.a&&(this.a=null);xSb(this)}return b};var nSb,oSb,pSb,qSb,rSb,sSb,tSb;var o3=R8b(Qlc,'DockPanel',893);K9(160,1,{},FSb);var l3=R8b(Qlc,'DockPanel/DockLayoutConstant',160);K9(161,1,{161:1},GSb);_.c='';_.f='';var m3=R8b(Qlc,'DockPanel/LayoutData',161);K9(272,1,{272:1},HSb);_.a=0;var n3=R8b(Qlc,'DockPanel/TmpRow',272);alc(Fl)(11);\n//# sourceURL=showcase-11.js\n")
