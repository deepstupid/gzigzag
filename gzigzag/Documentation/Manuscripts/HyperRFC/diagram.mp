%
%
%  diagram to demonstrate regional bi-directional links
%
%

prologues:=1;

picture pic;
input boxes;

defaultscale := 1.3;

%------------------------------------------------------------
% figure 1
%------------------------------------------------------------
beginfig(1);

%
% left box
%
boxit.bl();
bl.dx=20;
bl.dy=50;
bl.nw=(100,100);
drawboxed(bl);;

label("1034", (120,90));

%
% middle box
%
boxit.bm();
bm.dx=20;
bm.dy=50;
bm.nw=(200,100);
drawboxed(bm);

label("1123", (220,90));

%
% right box
%
boxit.br();
br.dx=20;
br.dy=30;
br.nw=(300,100);
drawboxed(br);

label("2128", (320,90));

%
% label coordinates
%
ax=120;
ay=70;
by=50;
cy=30;

dx=220;
dy=70;
ey=50;

fx=320;
fy=70;

%
% labels
%
label( "a" , (ax,ay));
label( "b" , (ax,by));
label( "c" , (ax,cy));
label( "d" , (dx,dy));
label( "e" , (dx,ey));
label( "f" , (fx,fy));

%
% arrows
%

draw (ax,ay+5){right} ... (200,120 ) ... (fx,dy+5){right} cutafter bpath br  cutbefore bpath bl;
draw (ax,ay-5){right} ... (200,110 ) ... (fx,dy-5){right} cutafter bpath br  cutbefore bpath bl;

draw (ax,cy+5){right} ... (dx,dy+5){right} cutafter bpath bm  cutbefore bpath bl;
draw (ax,cy-5){right} ... (dx,dy-5){right} cutafter bpath bm  cutbefore bpath bl;

draw (ax,by+5){right} ... (dx,ey+5){right} cutafter bpath bm  cutbefore bpath bl;
draw (ax,by-5){right} ... (dx,ey-5){right} cutafter bpath bm  cutbefore bpath bl;

endfig;

%------------------------------------------------------------
% figure 2
%------------------------------------------------------------
beginfig(2);

%
% left box
%
boxit.bl();
bl.dx=40;
bl.dy=50;
bl.nw=(100,100);
drawboxed(bl);

label("1034", (140,90));

%
% middle box
%
boxit.bm();
bm.dx=40;
bm.dy=50;
bm.nw=(200,100);
drawboxed(bm);

label("1123", (240,90));

%
% right box
%
boxit.br();
br.dx=40;
br.dy=30;
br.nw=(300,100);
drawboxed(br);

label("2128", (340,90));

%
% label coordinates
%
ax2=120;
ay2=70;
by2=50;
cy2=30;

dx2=220;
dy2=70;
ey2=30;

fx2=320;
fy2=70;

%
% labels
%
label( "a" , (ax2,ay2));
label( "b" , (ax2,by2));
label( "c" , (ax2,cy2));
label( "d" , (dx2,dy2));
label( "e" , (dx2,ey2));
label( "f" , (fx2,fy2));


label( "f" , (ax2+30,ay2));
label( "e" , (ax2+30,by2));
label( "d" , (ax2+30,cy2));

label( "c" , (dx2+30,dy2));
label( "b" , (dx2+30,cy2));

label( "a" , (fx2+30,fy2));

%
% page lines
%
draw( (110, 80) .. (110, 10));
draw( (110+30, 80) .. (110+30, 10));

draw( (210, 80) .. (210, 10));
draw( (210+30, 80) .. (210+30, 10));


draw( (310, 80) .. (310, 45));
draw( (310+30, 80) .. (310+30, 45));


%
% arrows
%

%
% to
%
drawarrow (ax2,ay2){right} ... (180,ay2 ) ... (200,110 ) ... 
          (fx2,dy2-5){right} cutafter bpath br  cutbefore bpath bl;

drawarrow (ax2,cy2){right} ... (180,cy2 ) ... (dx2,dy2){right} cutafter bpath bm  cutbefore bpath bl;

drawarrow (ax2,by2){right} ... (180,by2 ) ... (dx2,ey2){right} cutafter bpath bm  cutbefore bpath bl;

%
% from
%
drawarrow (280,dy2){right} ... (260,110){left} ... (100,110){left}  ... (ax2,ay2){right} cutafter bpath bl  cutbefore bpath bm;


drawarrow (280,ey2){right} ... (260,5){left} ... (100,5){left}  ... (ax2,cy2){right} cutafter bpath bl  cutbefore bpath bm;


drawarrow (380,fy2){right} ... (260,2){left} ... (100,2){left}  ... (ax2,by2){right} cutafter bpath bl  cutbefore bpath br;



endfig;

end
