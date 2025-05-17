// -- https://xemantic.github.io/shader-web-background/
const shaderWebBackground={};(()=>{'use strict';const r=(a,b)=>{b.initHalfFloatRGBATexture(b.width,b.height);a.texParameteri(a.TEXTURE_2D,
a.TEXTURE_MIN_FILTER,a.LINEAR);a.texParameteri(a.TEXTURE_2D,a.TEXTURE_MAG_FILTER,
a.LINEAR);a.texParameteri(a.TEXTURE_2D,a.TEXTURE_WRAP_S,a.CLAMP_TO_EDGE);a.texParameteri(a.TEXTURE_2D,
a.TEXTURE_WRAP_T,a.CLAMP_TO_EDGE);return{width:b.width,height:b.height}},y=(a,b)=>
{console.warn("shader-web-background cannot shade, adding fallback CSS classes");
document.documentElement.classList.add("shader-web-background-fallback");b.classList.add("shader-web-background-fallback");
if(a instanceof shaderWebBackground.GlError)console.warn("Not sufficient WebGL support:",
a);else throw a;};
function z(a,b){if(!a)throw new shaderWebBackground.ConfigError(b);}
function A(a){z(a instanceof HTMLCanvasElement,"config.canvas must be instance of canvas");
return a}
function B(){const a=document.createElement("canvas"),b=a.style;a.id="shader-web-background";b.width=
"100vw";b.height="100vh";b.position="fixed";b.top="0";b.left="0";b.zIndex=-9999;return a}
function C(a,b,d){z(a instanceof HTMLScriptElement&&a.type===b,'Shader source element of id "'+
d+'" should be of type: <script type="'+(b+'" id="'+d+'">'))}
function E(a){const b=document.getElementById(a);z(b,'Missing shader source: <script type="x-shader/x-fragment" id="'+
(a+'">'));C(b,"x-shader/x-fragment",a);return b.text}
function F(a){a+="Vertex";const b=document.getElementById(a);return b?(C(b,"x-shader/x-vertex",
a),b.text):"attribute vec2 V;void main(){gl_Position=vec4(V,0,1);}"}
function G(a,b){"loading"!==document.readyState?b():window.addEventListener(a,b)}
class H{constructor(a,b,d){this.g=b;this.h=()=>{for(const c of d)c()};this.i=()=>
{var c=b.u,e=a.gl;e.bindBuffer(e.ARRAY_BUFFER,a.j);e.enableVertexAttribArray(c);e.vertexAttribPointer(c,
2,e.FLOAT,!1,0,0);e.drawArrays(e.TRIANGLE_STRIP,0,4);e.disableVertexAttribArray(c);
e.bindBuffer(e.ARRAY_BUFFER,null);c=a.gl;for(e=0;e<a.g;e++)c.activeTexture(c.TEXTURE0+
e),c.bindTexture(c.TEXTURE_2D,null);a.g=0}}}
function I(a){var b={antialias:!1,depth:!1,alpha:!1};try{return new J(a,b)}catch(d){throw new shaderWebBackground.GlError(d.message);
}}
function K(a,b,d,c,e,n,u){function w(f,k,m){try{{var h=g;const p=h.gl,R=L(h,f,p.VERTEX_SHADER,k),
S=L(h,f,p.FRAGMENT_SHADER,m),x=p.createProgram();p.attachShader(x,R);p.attachShader(x,
S);p.linkProgram(x);var q=x}return q}catch(p){throw new shaderWebBackground.ConfigError(p.message);
}}const g=I(b),l=[];a.gl=g.gl;a.canvas=b;a.width=0;a.height=0;a.cssPixelRatio=0;a.cssWidth=
0;a.cssHeight=0;a.isOverShader=(f,k)=>{const m=b.getBoundingClientRect();return f>=
m.left&&f<=m.right&&k>=m.top&&k<=m.bottom};a.toShaderX=f=>(f-b.getBoundingClientRect().left)*
a.cssPixelRatio+.5;a.toShaderY=f=>b.height-(f-b.getBoundingClientRect().top)*a.cssPixelRatio-
.5;const v=()=>{const f=window.devicePixelRatio||1,k=b.clientWidth,m=b.clientHeight,
h=Math.floor(k*f),q=Math.floor(m*f);b.width=h;b.height=q;a.width=h;a.height=q;a.cssPixelRatio=
f;a.cssWidth=k;a.cssHeight=m;a.resolution=[h,q];for(const p of l)p.g.l(h,q)};a.buffers=
{};a.initHalfFloatRGBATexture=(f,k)=>{g.h.g(f,k)};const T=Object.keys(d).length-1;
let U=0;for(const f in d){if(U++<T){const h=d[f].texture||r;a.buffers[f]=M(g,()=>
h(g.gl,a))}const k=N(g,w(f,F(f),E(f)),a.buffers[f]),m=d[f].uniforms||{};var t=Object.keys(m);
for(const h of k.o)z(m[h.name],'No configuration for uniform "'+h.name+'" defined in shader "'+
f+'"'),t=t.filter(q=>q!==h.name);0!==t.length&&console.warn('Extra uniforms configured for shader "'+
f+'", which are not present in the shader code - might have been removed by GLSL compiler if not used: '+
t.join(", "));t=k.o.map(h=>{const q=m[h.name];return"function"===typeof q?()=>{const p=
q(h.location);h.m(p)}:()=>{h.m(q)}});l.push(new H(g,k,t))}const D=()=>{requestAnimationFrame(D);
if(0<b.getBoundingClientRect().top+b.height&&b.getBoundingClientRect().top<(window.innerHeight||
document.documentElement.clientHeight)){if(a.cssWidth!==b.clientWidth||a.cssHeight!==
b.clientHeight){v();var f=!0}else f=!1;f&&e&&e(a.width,a.height);n&&n();for(const k of l)k.g.i(k.h,
k.i);u&&u()}};G("load",()=>{v();c&&c();e&&e(a.width,a.height);requestAnimationFrame(D)})}
shaderWebBackground.Error=class extends Error{constructor(a){super(a);this.name="shaderWebBackground.Error"}};
shaderWebBackground.ConfigError=class extends shaderWebBackground.Error{constructor(a){super(a);
this.name="shaderWebBackground.ConfigError"}};
shaderWebBackground.GlError=class extends shaderWebBackground.Error{constructor(a){super(a);this.name=
"shaderWebBackground.GlError"}};
shaderWebBackground.shade=function(a){z(a,"Missing configurer argument");const b=
{};a=a(b);const d=a.canvas?A(a.canvas):B();z(a.shaders,"No shaders specified in config");
try{return K(b,d,a.shaders,a.onInit,a.onResize,a.onBeforeFrame,a.onAfterFrame),a.canvas||
G("DOMContentLoaded",()=>{document.body.appendChild(d)}),b}catch(c){(a.onError||y)(c,
d)}};const O=[-1,1,1,1,-1,-1,1,-1];
function P(a,b){return a.j(a.gl.getExtension(b),b+" extension is not supported")}
class Q{constructor(a,b){this.gl=a;this.j=b}g(){}}
class V extends Q{constructor(a,b){super(a,b);this.h=P(this,"OES_texture_half_float");
P(this,"OES_texture_half_float_linear")}g(a,b){const d=this.gl;d.texImage2D(d.TEXTURE_2D,
0,d.RGBA,a,b,0,d.RGBA,this.h.HALF_FLOAT_OES,null)}}
class W extends Q{constructor(a,b){super(a,b);P(this,"EXT_color_buffer_float");this.gl.getExtension("OES_texture_float_linear")}g(a,
b){const d=this.gl;d.texImage2D(d.TEXTURE_2D,0,d.RGBA16F,a,b,0,d.RGBA,d.HALF_FLOAT,
null)}}
function aa(a){a=a.split(/\r?\n/);const b=a.length.toString().length;var d=[];a.forEach((c,
e)=>{e=(e+1).toString();e=e.length>=b?e:" ".repeat(b-e.length)+e;d.push(e+": "+c+
"\n")});return d.join("")}const X=a=>a.map(b=>b?1:0);
function M(a,b){return new Y(a.gl,()=>b(a.gl))}
function N(a,b,d){const c=a.gl,e=[],n=c.getProgramParameter(b,c.ACTIVE_UNIFORMS);
for(let g=0;g<n;g++){const l=c.getActiveUniform(b,g),v=c.getUniformLocation(b,l.name);
e.push({name:l.name,info:l,location:v,m:ba(a,v,l.type)})}let u=0,w=0;return{u:c.getAttribLocation(b,
"V"),o:e,l:d?(g,l)=>d.l(g,l):(g,l)=>{u=g;w=l},i:(g,l)=>{c.useProgram(b);g();d?(c.viewport(0,
0,d.width,d.height),g=d.g,d.g=d.h,d.h=g,d.i(l)):(c.viewport(0,0,u,w),l())}}}
function L(a,b,d,c){a=a.gl;d=a.createShader(d);a.shaderSource(d,c);a.compileShader(d);
if(!a.getShaderParameter(d,a.COMPILE_STATUS)){const e=String(a.getShaderInfoLog(d));
a.deleteShader(d);b="Cannot compile shader - "+b+": "+e;console.log(b);console.log(aa(c));
throw Error(b);}return d}
function ba(a,b,d){const c=a.gl;switch(d){case c.FLOAT:d=e=>c.uniform1f(b,e);break;
case c.FLOAT_VEC2:d=e=>c.uniform2fv(b,e);break;case c.FLOAT_VEC3:d=e=>c.uniform3fv(b,
e);break;case c.FLOAT_VEC4:d=e=>c.uniform4fv(b,e);break;case c.INT:d=e=>c.uniform1i(b,
e);break;case c.INT_VEC2:d=e=>c.uniform2iv(b,e);break;case c.INT_VEC3:d=e=>c.uniform3iv(b,
e);break;case c.INT_VEC4:d=e=>c.uniform4iv(b,e);break;case c.BOOL:d=e=>c.uniform1i(b,
e?1:0);break;case c.BOOL_VEC2:d=e=>c.uniform2iv(b,X(e));break;case c.BOOL_VEC3:d=
e=>c.uniform3iv(b,X(e));break;case c.BOOL_VEC4:d=e=>c.uniform4iv(b,X(e));break;case c.FLOAT_MAT2:d=
e=>c.uniformMatrix2fv(b,!1,e);break;case c.FLOAT_MAT3:d=e=>c.uniformMatrix3fv(b,!1,
e);break;case c.FLOAT_MAT4:d=e=>c.uniformMatrix4fv(b,!1,e);break;case c.SAMPLER_2D:d=
e=>{{const n=a.gl;e=e instanceof Y?e.g:e;n.activeTexture(n.TEXTURE0+a.g);n.bindTexture(n.TEXTURE_2D,
e);n.uniform1i(b,a.g++)}};break;default:throw Error("unknown type");}return d}
class J{constructor(a,b){this.canvas=a;const d=(e,n)=>{if(!e)throw Error(n);return e};
let c=a.getContext("webgl2",b);if(c)this.h=new W(c,d);else if(c=a.getContext("webgl",
b))this.h=new V(c,d);d(c,"webgl context not supported on supplied canvas element: "+
a);this.gl=c;a=c.createBuffer();c.bindBuffer(c.ARRAY_BUFFER,a);c.bufferData(c.ARRAY_BUFFER,
new Float32Array(O),c.STATIC_DRAW);c.bindBuffer(c.ARRAY_BUFFER,null);this.j=a;this.buffers=
{};this.g=0}}
function Z(a){const b=a.gl,d=b.createTexture();b.bindTexture(b.TEXTURE_2D,d);const c=
a.s(b);a.width=c.width;a.height=c.height;b.bindTexture(b.TEXTURE_2D,null);return d}
class Y{constructor(a,b){this.j=a.createFramebuffer();this.gl=a;this.s=b;this.g=this.h=
null;this.height=this.width=0}l(){this.h&&this.gl.deleteTexture(this.h);this.g&&this.gl.deleteTexture(this.g);
this.h=Z(this);this.g=Z(this)}i(a){const b=this.gl;b.bindFramebuffer(b.FRAMEBUFFER,
this.j);b.framebufferTexture2D(b.FRAMEBUFFER,b.COLOR_ATTACHMENT0,b.TEXTURE_2D,this.g,
0);a();b.framebufferTexture2D(b.FRAMEBUFFER,b.COLOR_ATTACHMENT0,b.TEXTURE_2D,null,
0);b.bindFramebuffer(b.FRAMEBUFFER,null)}};})()