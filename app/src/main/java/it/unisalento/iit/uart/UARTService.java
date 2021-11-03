/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package it.unisalento.iit.uart;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import it.unisalento.iit.IITMonitorApplication;
import it.unisalento.iit.profile.BleProfileService;
import it.unisalento.iit.profile.LoggableBleManager;
import no.nordicsemi.android.log.Logger;
import it.unisalento.iit.R;

public class UARTService extends BleProfileService implements UARTManagerCallbacks {
    //Per avere nomi univoci si usa il package
    public static final String BROADCAST_UART_TX = "it.unisalento.iit.uart.BROADCAST_UART_TX";
    public static final String BROADCAST_UART_RX = "it.unisalento.iit.uart.BROADCAST_UART_RX";
    public static final String EXTRA_PACCHETTO = "it.unisalento.iit.uart.EXTRA_PACCHETTO";

    /**
     * A broadcast message with this action and the message in {@link Intent#EXTRA_TEXT} will be sent t the UART device.
     */
    public final static String ACTION_SEND = "it.unisalento.iit.uart.ACTION_SEND";
    /**
     * A broadcast message with this action is triggered when a message is received from the UART device.
     */
    private final static String ACTION_RECEIVE = "it.unisalento.iit.uart.ACTION_RECEIVE";
    /**
     * Action send when user press the DISCONNECT button on the notification.
     */
    public final static String ACTION_DISCONNECT = "it.unisalento.iit.uart.ACTION_DISCONNECT";
    /**
     * A source of an action.
     */
    public final static String EXTRA_SOURCE = "it.unisalento.iit.uart.EXTRA_SOURCE";
    public final static int SOURCE_NOTIFICATION = 0;
    public final static int SOURCE_3RD_PARTY = 2;

    private final static int NOTIFICATION_ID = 349; // random
    private final static int DISCONNECT_REQ = 97; // random

    // Ho modificato il valore da false a True per fermare il Servizio quando viene premuto DISCONNECT
    @Override
    protected boolean stopWhenDisconnected() {
        return true;
    }

    @Override
    protected boolean shouldAutoConnect() {
        return true;
    }

    private UARTManager manager;

    private final LocalBinder binder = new UARTBinder();

    public class UARTBinder extends LocalBinder implements UARTInterface {
        @Override
        public void send(final String text) {
            manager.send(text);
        }
    }

    @Override
    public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
        // Note 1: Do not use the device argument here unless you change calling onDeviceDisconnected from the binder above

        // Note 2: if BleManager#shouldAutoConnect() for this device returned true, this callback will be
        // invoked ONLY when user requested disconnection (using Disconnect button). If the device
        // disconnects due to a link loss, the onLinkLossOccurred(BluetoothDevice) method will be called instead.

        final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

        if (stopWhenDisconnected())
            stopService();
    }

    @Override
    public void onLinkLossOccurred(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
        final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, true);
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, optionalServicesFound);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onDeviceReady(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(BROADCAST_DEVICE_READY);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onDeviceNotSupported(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false);
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false);

        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onBondingRequired(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onBonded(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onBondingFailed(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onError(@NonNull final BluetoothDevice device, @NonNull final String message, final int errorCode) {
        final Intent broadcast = new Intent(BROADCAST_ERROR);
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice);
        broadcast.putExtra(EXTRA_ERROR_MESSAGE, message);
        broadcast.putExtra(EXTRA_ERROR_CODE, errorCode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    protected LocalBinder getBinder() {
        return binder;
    }

    @Override
    protected LoggableBleManager<UARTManagerCallbacks> initializeManager() {
        return manager = new UARTManager(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(disconnectActionBroadcastReceiver, new IntentFilter(ACTION_DISCONNECT));
        registerReceiver(intentBroadcastReceiver, new IntentFilter(ACTION_SEND));
    }

    @Override
    public void onDestroy() {
        // when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
        stopForegroundService();
        unregisterReceiver(disconnectActionBroadcastReceiver);
        unregisterReceiver(intentBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onRebind() {
        stopForegroundService();
    }

    @Override
    protected void onUnbind() {
        startForegroundService();
    }

    private String notNull(final String name) {
        if (!TextUtils.isEmpty(name))
            return name;
        return getString(R.string.disp_sconosciuto);
    }

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, final String pacchetto) {

        final Intent broadcast = new Intent(BROADCAST_UART_RX);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_PACCHETTO, pacchetto);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

        // send the data received to other apps, e.g. the Tasker
        final Intent globalBroadcast = new Intent(ACTION_RECEIVE);
        globalBroadcast.putExtra(BluetoothDevice.EXTRA_DEVICE, getBluetoothDevice());
        globalBroadcast.putExtra(Intent.EXTRA_TEXT, pacchetto);
        sendBroadcast(globalBroadcast);
    }

    @Override
    public void onDataSent(@NonNull final BluetoothDevice device, final String pacchetto) {
        final Intent broadcast = new Intent(BROADCAST_UART_TX);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_PACCHETTO, pacchetto);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    /**
     * Sets the service as a foreground service
     */
    private void startForegroundService() {
        // when the activity closes we need to show the notification that user is connected to the peripheral sensor
        // We start the service as a foreground service as Android 8.0 (Oreo) onwards kills any running background services
        final Notification notification = createNotification(R.string.uart_notification_connected_message, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Stops the service as a foreground service
     */
    private void stopForegroundService() {
        // when the activity rebinds to the service, remove the notification and stop the foreground service
        // on devices running Android 8.0 (Oreo) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            cancelNotification();
        }
    }

    /**
     * Creates the notification
     *
     * @param messageResId message resource id. The message must have one String parameter,<br />
     *                     f.e. <code>&lt;string name="name"&gt;%s is connected&lt;/string&gt;</code>
     * @param defaults     signals that will be used to notify the user
     */
    @SuppressWarnings("SameParameterValue")
    protected Notification createNotification(final int messageResId, final int defaults) {

        final Intent disconnect = new Intent(ACTION_DISCONNECT);
        disconnect.putExtra(EXTRA_SOURCE, SOURCE_NOTIFICATION);

        final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, IITMonitorApplication.CONNECTED_DEVICE_CHANNEL);
        builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
        builder.setSmallIcon(R.drawable.ic_iitmonitor);
        builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_iitmonitor, getString(R.string.uart_notification_action_disconnect), disconnectAction));

        return builder.build();
    }

    /**
     * Cancels the existing notification. If there is no active notification this method does nothing
     */
    private void cancelNotification() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    /**
     * This broadcast receiver listens for {@link #ACTION_DISCONNECT} that may be fired by pressing Disconnect action button on the notification.
     */
    private final BroadcastReceiver disconnectActionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int source = intent.getIntExtra(EXTRA_SOURCE, SOURCE_NOTIFICATION);
            switch (source) {
                case SOURCE_NOTIFICATION:
                    Logger.i(getLogSession(), "[Notification] Disconnect action pressed");
                    break;
            }
            if (isConnected())
                getBinder().disconnect();
            else
                stopSelf();
        }
    };

    /**
     * Broadcast receiver that listens for {@link #ACTION_SEND} from other apps. Sends the String or int content of the {@link Intent#EXTRA_TEXT} extra to the remote device.
     * The integer content will be sent as String (65 -> "65", not 65 -> "A").
     */
    private BroadcastReceiver intentBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final boolean hasMessage = intent.hasExtra(Intent.EXTRA_TEXT);
            if (hasMessage) {
                String message = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (message == null) {
                    final int intValue = intent.getIntExtra(Intent.EXTRA_TEXT, Integer.MIN_VALUE); // how big is the chance of such data?
                    if (intValue != Integer.MIN_VALUE)
                        message = String.valueOf(intValue);
                }

                if (message != null) {
                    final int source = intent.getIntExtra(EXTRA_SOURCE, SOURCE_3RD_PARTY);
                    switch (source) {
                        case SOURCE_3RD_PARTY:
                        default:
                            Logger.i(getLogSession(), "[Broadcast] " + ACTION_SEND + " broadcast received with data: \"" + message + "\"");
                            break;
                    }
                    manager.send(message);
                    return;
                }
            }
            // No data od incompatible type of EXTRA_TEXT
            if (!hasMessage)
                Logger.i(getLogSession(), "[Broadcast] " + ACTION_SEND + " broadcast received no data.");
            else
                Logger.i(getLogSession(), "[Broadcast] " + ACTION_SEND + " broadcast received incompatible data type. Only String and int are supported.");
        }
    };
}
