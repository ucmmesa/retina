//
// Created by maximilien on 22.05.19.
//

#include "TestPacejkaUKF.h"
#include <iostream>
#include <functional>
#include <stdlib.h>
#include <time.h>
#include "../InputOutput/WriterUKF.h"
#include "../InputOutput/ReaderCSV.cpp"
#include <math.h>

void TestPacejkaUKF::test(
        UKF::ParameterVec groundTruth,
        UKF::ParameterVec guess,
        UKF::ParameterMat variance
        ) {


    double r = 0.1; // measurement noise
    //double r = static_cast <double> (rand()) / static_cast <double> (RAND_MAX); // mea    surement noise
    UKF::MeasurementMat measurementNoise = r *r * UKF::MeasurementMat::Identity();
    double q = 0.1; //process noise
    UKF::ParameterMat processNoise = q * q * UKF::ParameterMat::Identity();

    // UKF start
    UKF::ParameterVec mean = guess;
    UKF ukf = UKF(mean, variance);

    std::function<UKF::ParameterVec(UKF::ParameterVec)> predictionFunction
    = [](UKF::ParameterVec parameterVec){
            return parameterVec;
    };

    // extract slip
    Eigen::MatrixXd slip =
            load_csv<Eigen::MatrixXd>("/home/maximilien/Documents/sp/logs/slip_20190708T114135_f3f46a8b.lcmObj.00.csv");

    // slip filter
    /*
        double size = 40;
        for (int i = size; i < slip.rows(); i++) {
            slip(i, 2) = 1 / size * slip(i, 2);
            for (int j = 0; j < size; j++) {
                slip(i, 2) += 1 / size * slip(i - j, 2);
            }
        }
    */


    //for plotting
    // TODO find new method for writing with more data
    Eigen::Matrix<double, NP + 1, NI+1> params;

    for (int i = 0; i<= NI; i++){
        // print
        if(print){
            std::cout << "iteration--------------------------------------- " << i << std::endl;
        }

        // side slip s
        //constant slip
        //double s = .391;

        // random parameter s in range [-1;2];
        //double s = 3*static_cast <double> (rand()) / static_cast <double> (RAND_MAX) - 1;

        // sinusoid around -1 and 2
        //double s = 1.5*sin(0.01*i)+0.5;

        // sinusoid around 0 and 2
        //double s = 0.5*sin(0.05*i) + 0.3*sin(3*i) + 0.2*sin(10*i) + 1 ;

        // using slip from gokart log
        double s = slip(i,2);


        if(print){
            std::cout << "s: " << s << std::endl;
        }

        std::function<UKF::MeasurementVec(UKF::ParameterVec)> measureFunction
                    = [s](UKF::ParameterVec parameter){
            double b = parameter(0);
            double c = parameter(1);
            double d = parameter(2);

            double r = d*sin(c*atan(b*s));

            UKF::MeasurementVec measurementVec;
            measurementVec << r  ;
            return measurementVec;
        };

        UKF::MeasurementVec z = measureFunction(groundTruth);

        if(print){
            std::cout << "zMes: " << z << std::endl;
        }

        // UKF Update
        //****************************
        ukf.update(
                measureFunction,
                predictionFunction,
                measurementNoise,
                processNoise,
                z);

        //for plotting
        Eigen::MatrixXd value(4, 1);
        value << i, ukf.mean(0), ukf.mean(1), ukf.mean(2);
        params.col(i) = value;


    }

    if (print){
        std::cout << "params" << std::endl << params << std::endl;
    }

    // compute rmse
    for (int i = 0; i < NI; i++){
        rmse += std::sqrt(pow(params(1,i) - groundTruth(0),2)
                +pow(params(2,i) - groundTruth(1),2)
                +pow(params(3,i) - groundTruth(2),2));
    }
    rmse = rmse/sqrt(NI);

    // compute convergence
    convergence = std::sqrt(pow(params(1,NI-1) - groundTruth(0),2)
                            +pow(params(2,NI-1) - groundTruth(1),2)
                            +pow(params(3,NI-1) - groundTruth(2),2));



    std::cout << "Mean: \t" << std::endl << ukf.mean << std::endl;
    std::cout << "Variance: \t" << std::endl << ukf.variance << std::endl;
    std::cout << "RMSE: \t " << rmse << std::endl;
    std::cout << "Convergence: \t " << convergence << std::endl;



    // export for plot
    if(writeCSV) {
        WriterUKF writerUkf;
        writerUkf.writeToCSV("paramsUKF.csv", params.transpose());
    }



}