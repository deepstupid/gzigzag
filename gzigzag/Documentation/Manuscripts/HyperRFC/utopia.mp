prologues:=0;
picture pic;
input boxes;

verbatimtex \input utopiapara etex

beginfig(1);

picture p;
picture ts;
picture thing;
pair wholeUL, wholeLR, wholeUR, wholeLL;
numeric ind;
numeric ys[], xs[] yes[], xc[], yc[];
numeric ystart;
pair trans, ftrans;
ind = 20;
z0 = (10,10);
x1 := x0; y1 := y0;
truecorners := 1;
bboxmargin := 0;
labeloffset := 0;

def putitrot(expr thingfoo, size, indent, rot) =
 begingroup
	pair tmp;
 	thing := (thingfoo shifted -ulcorner(bbox(thingfoo)));
 	ys[ystart] := y0;
	trans := z0 + (indent, 0);
 	ts := thing scaled size rotated rot shifted trans;
	fill bbox(thing) scaled size rotated rot shifted trans withcolor white;
	draw ts;
	show y0;
	tmp = whatever[ulcorner(bbox(ts)), lrcorner(bbox(ts))]
	   = whatever[urcorner(bbox(ts)), llcorner(bbox(ts))];
	xc[ystart] := xpart(tmp);
	yc[ystart] := ypart(tmp);
	y0 := y0 - ypart(urcorner(bbox(ts)) - lrcorner(bbox(ts))) - 15 * size;
	show y0;
	yes[ystart] := y0;
	ystart := ystart + 1;
 endgroup
enddef;

def putit(expr thing, size, indent) =
  begingroup
	putitrot(thing, size, indent, 0);
  endgroup
enddef;

ystart := 0;
putitrot(btex \vbox{\terminology} etex, 0.8, +5, -5); % 0
y0 := y0 - 10;
putitrot(btex \vbox{\typedef} etex, 0.8, -20, 3); % 1
x0 := x1 + ind + xpart(urcorner(ts) - ulcorner(ts)) + 10;
y0 := y1;

p := btex \vbox{\ptii} etex;
putit(p, 0.6,0); % 2
putit(btex \vbox{\ptiii} etex, 0.6,	0); % 3
putit(btex \vbox{\ptiiii} etex, 0.7,	0); % 4
putit(btex \vbox{\ptv} etex, 1.0,	0); % 5
putit(btex \vbox{\ptvi} etex, 0.7,	ind); % 6
putit(btex \vbox{\ptvii} etex, 0.7,	ind); % 7
putit(btex \vbox{\ptviii} etex, 1.3,	ind); % 8
w := xpart(urcorner(ts) - ulcorner(ts));
putit(btex \vbox{\ptviiii} etex, 0.9,	0); % 9
putit(btex \vbox{\ptx} etex, 0.8,	0); % 10
putit(btex \vbox{\ptxi} etex, 0.7,	0); % 11

bord := 10;
rX := x0 + w + 17;
draw ((x0-bord, ys[2]+bord) -- (rX+bord, ys[2]+bord) --
      (rX+bord, y0-bord) -- (x0-bord, y0-bord) -- cycle);

ystart := 100;
x0 := x0 + w + ind; 
y0 := ys[2] - 10;
putitrot(btex \vbox{\RFCrefvi} etex, 0.7, 50, 5); % 100
y0 := ys[8] + 150;
putitrot(btex \vbox{\RFCrefviii} etex, 1.1, 70, -3); % 101
y0 := yes[101];
putitrot(btex \vbox{\RFCrefviiii} etex, 0.9, 50, 2); % 102
y0 := yes[102];
putitrot(btex \vbox{\RFCrefx} etex, 0.8, 60, -5); % 103

% p := btex \vbox{\ptiii}  etex ;
% dotlabel.lrt(p, z0);
% y0 := y0 - ypart(urcorner(p) - lrcorner(p));
% 
% p := btex \vbox{\ptiiii}  etex ;
% dotlabel.lrt(p, z0);

pickup pencircle scaled 5;

def draws(text t) = 
begingroup;
draw (xc[0], yc[0]) -- (xc[5], yc[5]) t;
draw (xc[1], yc[1]) -- (xc[8], yc[8]) t;
draw (xc[100], yc[100]) -- (xc[6], yc[6]) t;
draw (xc[101], yc[101]) -- (xc[8], yc[8]) t;
draw (xc[102], yc[102]) -- (xc[9], yc[9]) t;
draw (xc[103], yc[103]) -- (xc[10], yc[10]) t;
endgroup;
enddef;

pickup pencircle scaled 2.5;
draws(withcolor white);
pickup pencircle scaled 1.5;
draws(withcolor black);

%show ulcorner(bbox(currentpicture));
%show lrcorner(bbox(currentpicture));
wholeUL := (-10,20.25);
wholeLR := (848.53668,-632.82071);
wholeUR := (xpart(wholeLR), ypart(wholeUL));
wholeLL := (xpart(wholeUL), ypart(wholeLR));
clip currentpicture to wholeUL--wholeUR--wholeLR--wholeLL--cycle;

endfig;

end;;
