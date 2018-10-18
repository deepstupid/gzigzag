beginfig(1)

psize = 5pt;

pair zcor;
z[0] = zcor = (100, 50);
pair ctr;
ctr = (300, 300);
pair out; 
out = (200,170);
out := out * 0.7;

numeric base;
base := 160;
numeric cthick;
cthick := 10;

numeric outl;
outl := 3pt;

pair outo;
outo := out rotated 90;
outo := (base/2) * (outo / abs(outo));


pickup pencircle scaled outl;
draw (ctr+zcor)--(ctr+(-x[0],y[0]))--(ctr+(-x[0],-y[0]))--(ctr+(x[0],-y[0]))
	--(ctr+zcor);

pickup pencircle scaled 1pt;

draw ctr -- ctr + out;

draw ctr -- ctr + outo;
draw ctr -- ctr - outo;
draw ctr + 1.2*out + outo -- ctr -0.2*out + outo;
draw ctr + 1.2*out - outo -- ctr -0.05*out - outo;

pair zint, zout, zsec;
zint = ctr + zcor + whatever*(-1,0);
zint = ctr + outo + whatever*out;
zout = ctr + zcor + whatever*(-1,0);
zout = ctr + whatever*out;
zsec = ctr + zcor + whatever*(0,-1);
zsec = ctr - outo +  whatever*out;

pickup pencircle scaled outl;

	numeric ret;

def func(expr a) = 
    begingroup
	numeric b;
	b := (abs(a) - cthick/2) / ((base-cthick)/2);
	if b < 0:
	    ret := 1;
	elseif b > 1:
	    ret := 0;
	else:
	    ret := 1-sqrt(1-(1-b)*(1-b));
	fi;
	show 'flip', ret;
	% draw (300,500)--(300+a,500+ret*200);
	ret
    endgroup;
enddef;

pair intersshownedge;
pair intersshownmid;
pair intersshowntop;

save z;
pair z[];

pickup pencircle scaled 2.5pt;
path p[];
p[1] := zint -- zout;
pair mid;
mid := ctr + zcor;
numeric ang;
ang = angle outo;
p[2] := ((zsec) -- (ctr+zcor) -- (zout));
for i = 1 upto 2:
    for ii = 0 upto 100:
	pair tmp;
	tmp := (point i*ii/100 of p[i]);
	pair rtmp;
	rtmp := (tmp - ctr) rotated -ang;
	% draw (rtmp + (300,300)) -- (rtmp);
	show 'rtmp', rtmp;
	numeric b;
	b := func(xpart rtmp);
	show 'b', b;
	pair inters;
	inters = ctr + out + whatever * outo;
	inters = tmp + whatever * out;
	z[ii] := b[
		tmp, inters
	    ];
	if (i = 1) and (ii = 60) :
	    intersshownedge := tmp;
	    intersshownmid := z[ii];
	    intersshowntop := inters;
	fi;
	show 'final', z[ii];
    endfor;
    draw z[0] for ii = 1 upto 100:
	--z[ii]
    endfor;
    ;
endfor;

numeric cw;
cw = 5;
def circle(expr p) =
    draw p+(0,cw)..p+(-cw,0)..p+(0,-cw)..p+(cw,0)..cycle;
enddef;

pickup pencircle scaled 1.5pt;

circle(intersshownedge);
circle(intersshownmid);
circle(intersshowntop);

pickup pencircle scaled 1pt;

draw intersshowntop -- intersshowntop - out;
draw ctr + out - outo -- ctr + out + outo;

ahlength := 7;

numeric f;
f := 1;
z[0] := intersshownedge + f * outo;
z[1] := intersshownmid + f * outo;

defaultfont:="cmmi10";
defaultscale:=2;
label.lrt("r",.5[z0,z1]);

numeric diam;
diam = 7;
draw ctr withpen pencircle scaled diam;
label.llft("P",ctr+(-3,-3));
draw ctr+out withpen pencircle scaled diam;
label.rt("C",ctr+out+(6,0));

draw intersshownmid -- z[1] dashed evenly;

drawdblarrow z0--z1;

f := 1.2;
z[0] := intersshownedge + f * outo;
z[1] := intersshowntop + f * outo;

draw intersshownedge -- z[0] dashed evenly;
draw intersshowntop -- z[1] dashed evenly;
label.ulft("s",.5[z0,z1]);

drawdblarrow z0--z1;

f := 0.2;
z[0] := intersshowntop + f * out;
z[1] := ctr + (1+f) * out;

draw intersshowntop -- z[0] dashed evenly;
draw ctr + out -- z[1] dashed evenly;
label.urt("t",.5[z0,z1]);

drawdblarrow z0--z1;


% draw zint .. controls (zint + (40, 0)) and
% 		(ctr + 0.3 * out + 0.1 * outo) 
% 		.. ctr + 0.7 * out + 0.1 * outo;

endfig;

end;
