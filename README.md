# IIT-Monitor
The IIT-Monitor APP allows you to communicate via BLE connection with a nRF-52832 sensory device
which, following the command received from the APP, returns the voltage values ​​acquired on 2 channels.

It's possible to customize the command by setting the number of samples, 
the sampling frequency and the gain

## General-info
The APP preliminarily allows you to:
- Connect the device transmit BLE; 
- Build the command to be given to the device; 
- Acquire data from the device.

Subsequently:
- View the acquired data graphically; 
- Store the data locally in csv format; 
- Share data in text format;
- Send the data as an attachment to an email.

The APP also allows you to open previously saved files and view the data graphically.     

## Tecnologies
This app was created starting from nRF-Toolbox
(available at https://github.com/NordicSemiconductor/Android-nRF-Toolbox).

### Libraries needed:
- [Android BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library) 
- [Android DFU Library](https://github.com/NordicSemiconductor/Android-DFU-Library) 
- [MPAndroidChart Library](https://github.com/PhilJay/MPAndroidChart)

## Note
- Android 4.3 or newer is required.
- Compatible with nRF5 devices running samples from the Nordic SDK and other devices implementing 
  standard profiles.
- Development kits: https://www.nordicsemi.com/Software-and-tools/Development-Kits.
- The nRF5 SDK and SoftDevices are available online at http://developer.nordicsemi.com.