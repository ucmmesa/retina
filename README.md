# ch.ethz.idsc.retina+gokart <a href="https://travis-ci.org/idsc-frazzoli/retina"><img src="https://travis-ci.org/idsc-frazzoli/retina.svg?branch=master" alt="Build Status"></a>

Sensor and actuator interfaces, Gokart software, version `0.0.1`

The repository was developed with the following objectives in mind
* interface sensors without loss of precision or temporal resolution
* interface actuators of gokart using a protocol that is specific to the MicroAutoBox implementation

The code in the repository operates a heavy and fast robot that may endanger living creatures.
We follow best practices and coding standards to protect from avoidable errors.
See [development_guidelines](doc/development_guidelines.md)

<table>
<tr>
<td>

![usecase_gokart](https://user-images.githubusercontent.com/4012178/35968269-a92a3b46-0cc3-11e8-8d5e-1276762cdc36.png)

Trajectory pursuit

</tr>
</table>

## Features

* interfaces to lidars Velodyne VLP-16, HDL-32E, Quanergy Mark8, HOKUYO URG-04LX-UG01
* interfaces to event based camera Davis240C with lossless compression by 4x
* lidar based localization
* offline processing of log data

## Architecture

We use `LCM` for message interchange.
All messages are encoded using a single type `BinaryBlob`.
The byte order of the binary data is `little endian` since the encoding is native on most architectures.

* [Video on Gokart Actuators](https://www.youtube.com/watch?v=t3oAqQlWoyo)
* [Video of Testing Software](https://www.youtube.com/watch?v=Oh9SyG4Lgm8)

## GOKART

Hardware protection modules:

* [code](src/main/java/ch/ethz/idsc/retina/dev/linmot/LinmotFireFighter.java) brake temperature critical => Linmot ZERO
* [code](src/main/java/ch/ethz/idsc/retina/dev/steer/SteerBatteryCharger.java) steer battery voltage above threshold => Steering passive

Emergency modules:

* [code](src/main/java/ch/ethz/idsc/gokart/core/fuse/SteerEmergencyModule.java) steering calibration out of range => RimoTorque ZERO
* [code](src/main/java/ch/ethz/idsc/gokart/core/fuse/MiscEmergencyModule.java) steering battery voltage out of range for at least 200[ms], or communication timeout detected => RimoTorque ZERO
* [code](src/main/java/ch/ethz/idsc/gokart/core/fuse/LinmotEmergencyModule.java) linmot not operational => RimoTorque ZERO
* [code](src/main/java/ch/ethz/idsc/gokart/core/fuse/LinmotTakeoverModule.java) external force detected on linmot/brake => Linmot OFF

Emergency support modules:

* [code](src/main/java/ch/ethz/idsc/gokart/core/fuse/LinmotCoolingModule.java) brake temperature close to critical => RimoTorque ZERO
* [code](src/main/java/ch/ethz/idsc/gokart/core/fuse/Vlp16ClearanceModule.java) obstacle detected by vlp16 lidar within certain range of predicted vehicle path => RimoTorque ZERO

Joystick Dead man switch:

* [code](src/main/java/ch/ethz/idsc/gokart/core/joy/DeadManSwitchModule.java) joystick signal missing, or gokart moving but joystick passive for timout period => trigger Linmot brake for ~2[s]


## LIDAR

### Velodyne VLP-16

* point cloud visualization and localization with lidar [video](https://www.youtube.com/watch?v=pykecjwixgg)

### Velodyne HDL-32E

* 3D-point cloud visualization: see [video](https://www.youtube.com/watch?v=abOYEIdBgRs)

distance as 360[deg] panorama

![velodyne distances](https://user-images.githubusercontent.com/4012178/29020149-581e9236-7b61-11e7-81eb-0fc4577b687d.gif)

intensity as 360[deg] panorama

![intensity](https://user-images.githubusercontent.com/4012178/29026760-c29ebbce-7b7d-11e7-9854-9280594cb462.gif)

### Quanergy Mark8

* 3D-point cloud visualization: see [video](https://www.youtube.com/watch?v=DjvEijz14co)

### HOKUYO URG-04LX-UG01

![urg04lx](https://user-images.githubusercontent.com/4012178/29029959-c052da4c-7b89-11e7-8b01-1b4efc3593c0.gif)

our code builds upon the
[urg_library-1.2.0](https://sourceforge.net/projects/urgnetwork/files/urg_library/)

## DVS

### IniLabs DAVIS240C

Rolling shutter mode

<table>
<tr>
<td>

![05tram](https://user-images.githubusercontent.com/4012178/30553969-2948547a-9ca3-11e7-91e8-159806c7e329.gif)

<td>

![04peds](https://user-images.githubusercontent.com/4012178/30553578-f3429ce2-9ca1-11e7-8870-85078c8aa96c.gif)

<td>

![00scene](https://user-images.githubusercontent.com/4012178/30553889-e59c0a5a-9ca2-11e7-8cc3-08de77e21e5e.gif)

</tr>
</table>

Global shutter mode

<table>
<tr>
<td>

![dvs_2500](https://user-images.githubusercontent.com/4012178/34606522-075a20ec-f210-11e7-966a-49384b048809.gif)

2.5[ms]

<td>

![dvs_5000](https://user-images.githubusercontent.com/4012178/34606520-073c7d08-f210-11e7-8ee2-1a35173bbade.gif)

5[ms]

</tr>
</table>

Events only

<table>
<tr>
<td>

![dvs_noaps_1000](https://user-images.githubusercontent.com/4012178/34684372-2eb4b200-f4a5-11e7-891e-74c2123a3bfe.gif)

1[ms]

<td>

![dvs_noaps_2500](https://user-images.githubusercontent.com/4012178/34684373-2eca8ee0-f4a5-11e7-9f70-f41d4722edf7.gif)

2.5[ms]

<td>

![dvs_noaps_5000](https://user-images.githubusercontent.com/4012178/34684374-2ee3aaba-f4a5-11e7-9ac6-72b7ac502793.gif)

5[ms]

</tr>
</table>



.aedat files

* parsing and visualization
* conversion to text+png format as used by the Robotics and Perception Group at UZH
* loss-less compression of DVS events by the factor of 2
* compression of raw APS data by factor 8 (where the ADC values are reduced from 10 bit to 8 bit)

### Device Settings

Quote from Luca/iniLabs:
* *Two parameters that are intended to control framerate:* `APS.Exposure` and `APS.FrameDelay`
* `APS.RowSettle` *is used to tell the ADC how many cycles to delay before reading a pixel value, and due to the ADC we're using, it takes at least three cycles for the value of the current pixel to be output by the ADC, so an absolute minimum value there is 3. Better 5-8, to allow the value to settle. Indeed changing this affects the framerate, as it directly changes how much time you spend reading a pixel, but anything lower than 3 gets you the wrong pixel, and usually under 5-6 gives you degraded image quality.*

We observed that in *global shutter mode*, during signal image capture the stream of events is suppressed. Whereas, in *rolling shutter mode* the events are more evenly distributed.

## streaming DAT files

![hdr](https://user-images.githubusercontent.com/4012178/27771907-a3bbcef4-5f58-11e7-8b0e-3dfb0cb0ecaf.gif)

## streaming DAVIS recordings

![shapes_6dof](https://user-images.githubusercontent.com/4012178/27771912-cb58ebb8-5f58-11e7-9566-79f3fbc5d9ba.gif)

## generating DVS from video sequence

![cat_final](https://user-images.githubusercontent.com/4012178/27771885-0eadb2aa-5f58-11e7-9f4d-78a57e610f56.gif)

## synthetic signal generation 

<table><tr>
<td>

![synth2](https://user-images.githubusercontent.com/4012178/27772611-32cc2e92-5f66-11e7-9d1f-ff15c42d54be.gif)

<td>

![synth1](https://user-images.githubusercontent.com/4012178/27772610-32af593e-5f66-11e7-8c29-64611f6ca3e6.gif)

</tr></table>

## Integration

Due to the rapid development of the code base, `retina` is not yet available as a maven artifact.
Instead, download the project and run `mvn install` on your machine.
Subsequently, you can use the project on your machine as

    <dependency>
      <groupId>ch.ethz.idsc</groupId>
      <artifactId>retina</artifactId>
      <version>0.0.1</version>
    </dependency>
