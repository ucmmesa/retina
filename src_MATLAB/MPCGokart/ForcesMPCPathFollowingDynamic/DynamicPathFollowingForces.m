%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Dynamic MPC Script
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% code by mh
% annotation mcp


%add force path (change that for yourself)
addpath('..');
userDir = getuserdir;
addpath([userDir '/Forces']); % Location of FORCES PRO
addpath('casadi');
addpath('../shared_dynamic')
    
clear model
clear problem
clear all
close all

%% Baseline params

maxSpeed = 10; % in [m/s]
maxxacc = 5; % in [m/s^-1]
steeringreg = 0.02;  
specificmoi = 0.3;
pointsO = 4; % number of Parameters
pointsN = 10; % Number of points for B-splines (10 in 3 coordinates)
splinestart = 1;
nextsplinepoints = 0;
%parameters: p = [maxspeed, xmaxacc,ymaxacc,latacclim,rotacceffect,torqueveceffect, brakeeffect, pointsx, pointsy]
% variables z = [dotab,dotbeta,ds,tv,slack,x,y,theta,dottheta,v,yv,ab,beta,s]


%% global parameters index
global index
index.dotab = 1;            %rate of change of Acceleration of rear axle
index.dotbeta = 2;         %rate of change of Angle of wheels
index.ds = 3;
index.tv = 4;                %Acceleration offset of Right axle from mean(AB)
index.slack = 5;
index.x = 6;                  %X pos in w.r.f
index.y = 7;                  %Y pos in w.r.f
index.theta = 8;            %Angle of cart in. w.r.f measured from +Y toward +X
index.dottheta = 9;        %rate of change of Angle of cart in w.r.f
index.v = 10;                %forward Velocity of cart (v in x direction w.r.t cart ref frame) 
index.yv = 11;              %Sideways Velocity of cart (v in y direction w.r.t cart ref frame) 
index.ab = 12;              %Acceleration of rear axle (Fr = AB+TV, Fl = AB-TV)
index.beta = 13;            %Angle of front wheels
index.s = 14;
index.ns = 9;       %number of state variables
index.nu = 5;       %number of non-state variables
index.nv = index.ns+index.nu;   % = 14
index.sb = index.nu+1;          % = 6
index.ps = 1;
index.pax = 2;
index.pbeta = 3;
index.pmoi = 4;

solvetimes = [];

integrator_stepsize = 0.1;

%% model params
model.N = 31;                       % Forward horizon
model.nvar = index.nv;              % = 14
model.neq = index.ns;               % = 9
model.eq = @(z,p) RK4( ...
    z(index.sb:end), ...
    z(1:index.nu), ...
    @(x,u,p)interstagedx(x,u,p), ... %PACEJKA PARAMETERS
    integrator_stepsize,...
    p);
model.E = [zeros(index.ns,index.nu), eye(index.ns)];

l = 1;

%limit lateral acceleration
model.nh = 5; 
model.ineq = @(z,p) nlconst(z,p);
model.hu = [0;0;1;0;0];
model.hl = [-inf;-inf;-inf;-inf;-inf];


% Random control points for trajectory sampling
%points = [1,2,2,4,2,2,1;0,0,5.7,6,6.3,10,10]';
  %  controlPointsX.append(Quantity.of(36.2, SI.METER));
  %  controlPointsX.append(Quantity.of(52, SI.METER));
  %  controlPointsX.append(Quantity.of(57.2, SI.METER));
  %  controlPointsX.append(Quantity.of(53, SI.METER));
  %  controlPointsX.append(Quantity.of(52, SI.METER));
  %  controlPointsX.append(Quantity.of(47, SI.METER));
  %  controlPointsX.append(Quantity.of(41.8, SI.METER));
  %  // Y
  %  controlPointsY.append(Quantity.of(44.933, SI.METER));
  %  controlPointsY.append(Quantity.of(58.2, SI.METER));
  %  controlPointsY.append(Quantity.of(53.8, SI.METER));
  %  controlPointsY.append(Quantity.of(49, SI.METER));
  %  controlPointsY.append(Quantity.of(47, SI.METER));
  %  controlPointsY.append(Quantity.of(43, SI.METER));
  %  controlPointsY.append(Quantity.of(38.333, SI.METER));  
points = [0,5,10,5,10,10,20,40,52,40,45,35,25,10;...          %x
            10,35,45,55,75,90,100,90,70,42,15,14,12,5; ...    %y
            4,3,5,4,3,4,4,4,4,4,4,4,4,4]';
%points = getPoints('/wildpoints.csv');
points(:,3)=points(:,3)-0.2;
%points = [36.2,52,57.2,53,55,47,41.8;44.933,58.2,53.8,49,44,43,38.33;1.8,1.8,1.8,0.2,0.2,0.2,1.8]';
%points = [0,40,40,5,0;0,0,10,9,10]';

trajectorytimestep = integrator_stepsize;
%[p,steps,speed,ttpos]=getTrajectory(points,2,1,trajectorytimestep);
model.npar = pointsO + 3*pointsN;
for i=1:model.N
   model.objective{i} = @(z,p)objective(...
       z,...
       getPointsFromParameters(p, pointsO, pointsN),...
       getRadiiFromParameters(p, pointsO, pointsN),...
       p(index.ps),...
       p(index.pax),...
       p(index.pbeta));
end
%model.objective{model.N} = @(z,p)objectiveN(z,getPointsFromParameters(p, pointsO, pointsN),p(index.ps));

model.xinitidx = index.sb:index.nv;
% variables z = [ab,dotbeta,ds,x,y,theta,v,beta,s,braketemp]
model.ub = ones(1,index.nv)*inf;
model.lb = -ones(1,index.nv)*inf;
%model.ub(index.dotbeta)=5;
%model.lb(index.dotbeta)=-5;
model.ub(index.ds)=5;
model.lb(index.ds)=-1;

%model.ub(index.ab)=2;
%model.lb(index.ab)=-4.5;
model.lb(index.ab)=-inf;%Max Breaking/Acceleration

model.ub(index.tv)=1.7;%Maximum torque vectoring
model.lb(index.tv)=-1.7;
%model.ub(index.tv)=0.1;
%model.lb(index.tv)=-0.1;

model.lb(index.slack)=0;

model.lb(index.v)=0;%No reversing constraint

model.ub(index.beta)=0.5;%Steering Angle constraint
model.lb(index.beta)=-0.5;

model.ub(index.s)=pointsN-2;
model.lb(index.s)=0;

%model.ub = [inf, +5, 1.6, +inf, +inf, +inf, +inf,0.45,pointsN-2,85];  % simple upper bounds 
%model.lb = [-inf, -5, -0.1, -inf, -inf,  -inf, 0,-0.45,0,-inf];  % simple lower bounds 



%% CodeOptions for FORCES solver
codeoptions = getOptions('MPCPathFollowing'); % Need FORCES License to run
codeoptions.maxit = 200;    % Maximum number of iterations
codeoptions.printlevel = 2; % Use printlevel = 2 to print progress (but not for timings)
codeoptions.optlevel = 2;   % 0: no optimization, 1: optimize for size, 2: optimize for speed, 3: optimize for size & speed
codeoptions.cleanup = false;
codeoptions.timing = 1;

output = newOutput('alldata', 1:model.N, 1:model.nvar);

FORCES_NLP(model, codeoptions,output); % Need FORCES License to run

tend = 200;
eulersteps = 10;
planintervall = 1
%[...,x,y,theta,v,ab,beta,s,braketemp]
%[49.4552   43.1609   -2.4483    7.3124   -1.0854   -0.0492    1.0496   39.9001]
fpoints = points(1:2,1:2);
pdir = diff(fpoints);
[pstartx,pstarty] = casadiDynamicBSPLINE(0.01,points);
pstart = [pstartx,pstarty];
pangle = atan2(pdir(2),pdir(1));
xs(index.x-index.nu)=pstart(1);
xs(index.y-index.nu)=pstart(2);
xs(index.theta-index.nu)=pangle;
xs(index.v-index.nu)=5;
xs(index.ab-index.nu)=0;
xs(index.beta-index.nu)=0;
xs(index.s-index.nu)=0.01;
%xs(index.braketemp-index.nu)=40;
history = zeros(tend*eulersteps,model.nvar+1);
splinepointhist = zeros(tend,pointsN*3+1);
plansx = [];
plansy = [];
planss = [];
targets = [];
planc = 10;
x0 = [zeros(model.N,index.nu),repmat(xs,model.N,1)]';
%x0 = zeros(model.N*model.nvar,1); 
tstart = 1;
%paras = ttpos(tstart:tstart+model.N-1,2:3)';
for i =1:tend
    tstart = i;
    %model.xinit = [0,5,0,0.1,0,0];

    %find bspline
    if(1)
        if xs(index.s-index.nu)>1
            nextSplinePoints
            %spline step forward
            splinestart = splinestart+1;
            xs(index.s-index.nu)=xs(index.s-index.nu)-1;
            %if(splinestart>pointsN)
                %splinestart = splinestart-pointsN;
            %end
        end
    end
    %xs(6)=xs(6)+normrnd(0,0.04);
    xs(index.ab-index.nu)=min(casadiGetMaxAcc(xs(index.v-index.nu))-0.0001,xs(index.ab-index.nu));
    problem.xinit = xs';
    %do it every time because we don't care about the performance of this
    %script
    ip = splinestart;
    [nkp, ~] = size(points);
    nextSplinePoints = zeros(pointsN,3);
    for jj=1:pointsN
       while ip>nkp
            ip = ip -nkp;
       end
       nextSplinePoints(jj,:)=points(ip,:);
       ip = ip + 1;
    end
    splinepointhist(i,:)=[xs(index.s-index.nu),nextSplinePoints(:)'];
    
    
    %paras = ttpos(tstart:tstart+model.N-1,2:3)';
    problem.all_parameters = repmat (getParameters(maxSpeed,maxxacc,steeringreg,specificmoi,nextSplinePoints) , model.N ,1);
    %problem.all_parameters = zeros(22,1);
    problem.x0 = x0(:);
    %problem.x0 = rand(341,1);
    
    % solve mpc
    [output,exitflag,info] = MPCPathFollowing(problem);
    solvetimes(end+1)=info.solvetime;
    if(exitflag==0)
       a = 1; 
    end
    if(exitflag~=1 && exitflag ~=0)
        draw
       return 
    end
    %nextSplinePoints
    %get output
    outputM = reshape(output.alldata,[model.nvar,model.N])';
    x0 = outputM';
    u = repmat(outputM(1,1:index.nu),eulersteps,1);
    [xhist,time] = euler(@(x,u)interstagedx(x,u,problem.all_parameters),xs,u,integrator_stepsize/eulersteps);
    xs = xhist(end,:);
    xs
    history((tstart-1)*eulersteps+1:(tstart)*eulersteps,:)=[time(1:end-1)+(tstart-1)*integrator_stepsize,u,xhist(1:end-1,:)];
    planc = planc + 1;
    if(planc>planintervall)
       planc = 1; 
       plansx = [plansx; outputM(:,index.x)'];
       plansy = [plansy; outputM(:,index.y)'];
       planss = [planss; outputM(:,index.s)'];
       [tx,ty]=casadiDynamicBSPLINE(outputM(end,index.s),nextSplinePoints);
       targets = [targets;tx,ty];
    end
end
%[t,ab,dotbeta,x,y,theta,v,beta,s]
draw

