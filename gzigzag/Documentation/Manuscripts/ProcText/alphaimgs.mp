beginfig(1)

pen edgepen;
edgepen = pencircle scaled 3pt;
pen alphaedge;
alphaedge = pencircle scaled 0.5pt;

def drawbg(expr ctrx) = 
    pair ctr;
    ctr = ctrx-(50,75/2);
    fill ctr+(120,0)..ctr+(50,100)..ctr+(-20,0)..ctr+(60,-40)..cycle
     withcolor .7white;
enddef;

def drawcell(expr ctrx) = 
    pair ctr;
    ctr = ctrx-(50,75/2);
    path outline;
    outline := (0,0)--(100,0)--(100,75)--(0,75)--cycle;
    outline := outline shifted ctr;
    fill outline withcolor white;
    draw outline withpen edgepen;
enddef;

def drawconn(expr ctrx) = 
    pair ctr;
    ctr = ctrx-(50,75/2);
    path side;
    path otherside;
    path tocell;
    side := (25,0) .. controls (43,0) and (45,-30) .. (45,-40);
    otherside := side xscaled -1 shifted (100,0);
    tocell := (25,0) .. controls(25, 10) and (75, 10) .. (75, 0);
    fill ((tocell .. otherside .. reverse(side) .. cycle) shifted ctr) withcolor white;
    draw side shifted ctr withpen edgepen;
    draw otherside shifted ctr withpen edgepen;
    % draw tocell shifted ctr withpen alphaedge;

enddef;

z1=(100,300);
z2=(300,300);
z3=(200,150);

z4 = z3 + (0, 65);

ahlength := 7;
pickup pencircle scaled 3pt;
drawarrow (z1+(0,-20)){right}..0.8[0.5[z1,z2],z4]{down}..z4;
drawarrow (z2+(0,-20)){left}..0.8[0.5[z1,z2],z4]{down}..z4;

drawbg(z1);
drawcell(z1);

defaultfont:="cmsy10";
defaultscale:=2;

% label("+", (250, 250));

drawbg(z2);
drawconn(z2);


drawbg(z3);
drawcell(z3);
drawconn(z3);

endfig;

end;
