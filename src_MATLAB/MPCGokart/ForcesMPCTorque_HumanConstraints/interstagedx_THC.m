function dx = interstagedx_THC(x,u,p)
    addpath('../TireAnalysis');
    %#codegen
    global index
    
    %just for the moment
    Cf = p(index.pmoi); % moment of inertia    


    FB = p(index.pacFB);
    FC = p(index.pacFC);
    FD = p(index.pacFD); % gravity acceleration considered

    RB = p(index.pacRB);
    RC = p(index.pacRC);
    RD = p(index.pacRD); % gravity acceleration considered
    
    
    scK=p(index.steerStiff);
    scD=p(index.steerDamp);
    scJ=p(index.steerInertia);
    param = [FB,FC,FD,RB,RC,RD,Cf,scK,scD,scJ];

    %Control vars
    dotab = u(index.dotab);                              %Dot_AB               [m*s^-3]          [Kart RF]
    dottau=u(index.dottau);                             %Dot_Tau               [SCT*s^-1]      
    tv = u(index.tv);                                        %TV                    [m*s^-2]          [Kart RF]
    ds = u(index.ds);
    
    
    %State vars
    ab = x(index.ab-index.nu);                              %AB               [m*s^-2]      [Global RF]
    dotbeta = x(index.dotbeta-index.nu);                %Dot_Beta      [SCE*s^-1]  
    theta = x(index.theta-index.nu);                      %Theta           [rad]            [Global RF]
    vx = x(index.v-index.nu);                                %X_Vel           [m*s^-1]      [Kart RF]
    vy = x(index.yv-index.nu);                              %Y_Vel           [m*s^-1]      [Kart RF]
    dottheta = x(index.dottheta-index.nu);              %Dot_Theta    [rad*s^-1]    [Global RF]
    beta = x(index.beta-index.nu);                         %Beta            [SCE]
    tauC= x(index.tau-index.nu);                           %Tau_C          [SCT] 
    
    
    %Ackermann Mapping of steering angle to Front Tire 
    ackermannAngle = -0.63.*beta.*beta.*beta+0.94*beta;  
    %Dynamic Model of cart, (Tricycle model)
    [ACCX,ACCY,ACCROTZ] = modelDx(vx,vy,dottheta,ackermannAngle,ab,tv, param);
    %Dynamic Model of Steering (Linear)
    [ACCBETA] = modelDb(vx,vx,dottheta,beta,dotbeta,tauC, param);
    
    import casadi.*
    if isa(x(1), 'double')
        dx = zeros(index.ns,1);
    else
        dx = SX.zeros(index.ns,1);
    end
    
    rotmat = @(beta)[cos(beta),-sin(beta);sin(beta),cos(beta)];%kart RF to Global RF
    lv = [vx;vy]; %velocity in Kart RF
    gv = rotmat(theta)*lv; %velocity in Global RF
    
    %Calculate Dx
    dx(index.x-index.nu)=gv(1);                     %Dot_X           [m*s^-1]   [Global RF]
    dx(index.y-index.nu)=gv(2);                     %Dot_Y           [m*s^-1]   [Global RF]
    dx(index.theta-index.nu)=dottheta;           % dot_Phi        [rad*s^-1] [Global RF]
    dx(index.dottheta-index.nu)=ACCROTZ;      % dot_dot_Phi  [rad*s^-2] [Global RF]
    dx(index.v-index.nu)=ACCX;                     %Dot_VX         [m*s^-2]    [Kart RF]          
    dx(index.yv-index.nu)=ACCY;                    %Dot_VX        [m*s^-2]    [Kart RF] 
    dx(index.ab-index.nu)=dotab;                    %Dot_AB        [m*s^-3]
    dx(index.beta-index.nu)=dotbeta;               %Dot_Beta     [SCE*s^-1]
    dx(index.s-index.nu)=ds;
    dx(index.dotbeta-index.nu)=ACCBETA;       %Dot_Dot_Beta     [SCE*s^-2]
    dx(index.tau-index.nu)=dottau;                 %Dot_Tau            [SCT*s^-1]
end

