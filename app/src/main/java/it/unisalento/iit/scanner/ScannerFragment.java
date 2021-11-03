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
package it.unisalento.iit.scanner;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.unisalento.iit.R;
import it.unisalento.iit.uart.ConnectActivity;
import it.unisalento.iit.utility.MessaggiHelper;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * ScannerFragment class scan required BLE devices and shows them in a list. This class scans and filter
 * devices with standard BLE Service UUID and devices with custom BLE Service UUID. It contains a
 * list and a button to scan/cancel. There is a interface {@link OnDeviceSelectedListener} which is
 * implemented by activity in order to receive selected device. The scanning will continue to scan
 * for 5 seconds and then stop.
 */

//Codice Modificato (visualizzo solo i dispositivi nelle vicinanze con servizio NUS )
public class ScannerFragment extends DialogFragment{

	private final static String PARAM_UUID = "param_uuid";

	private final static long TIME_OF_SCAN = 5000;

	private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number

	private BluetoothAdapter bluetoothAdapter;
	private OnDeviceSelectedListener listener;
	private DeviceListAdapter adapter;
	private final Handler handler = new Handler();
	private Button btnCerca;

	private ParcelUuid uuid;

	private boolean scanning = false;

	public static ScannerFragment getInstance(final UUID uuid) {
		final ScannerFragment fragment = new ScannerFragment();

		final Bundle args = new Bundle();
		if (uuid != null)
			args.putParcelable(PARAM_UUID, new ParcelUuid(uuid));
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Interface required to be implemented by activity.
	 */
	public interface OnDeviceSelectedListener {
		/**
		 * Fired when user selected the device.
		 *
		 * @param device
		 *            the device to connect to
		 * @param name
		 *            the device name. Unfortunately on some devices {@link BluetoothDevice#getName()}
		 *            always returns <code>null</code>, i.e. Sony Xperia Z1 (C6903) with Android 4.3.
		 *            The name has to be parsed manually form the Advertisement packet.
		 */
		void onDeviceSelected(@NonNull final BluetoothDevice device, @Nullable final String name);

		/**
		 * Fired when scanner dialog has been cancelled without selecting a device.
		 */
		void onDialogCanceled();
	}

	/**
	 * This will make sure that {@link OnDeviceSelectedListener} interface is implemented by activity.
	 */
	@Override
	public void onAttach(@NonNull final Context context) {
		super.onAttach(context);
		try {
			this.listener = (OnDeviceSelectedListener) context;
		} catch (final ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement OnDeviceSelectedListener");
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle args = getArguments();
		if (args != null && args.containsKey(PARAM_UUID)) {
			uuid = args.getParcelable(PARAM_UUID);
		}

		final BluetoothManager manager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);
		if (manager != null) {
			bluetoothAdapter = manager.getAdapter();
		}
	}

	@Override
	public void onDestroyView() {
		stopScan();
		super.onDestroyView();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
		final View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_device_selection, null);
		final ListView listview = dialogView.findViewById(android.R.id.list);

		listview.setEmptyView(dialogView.findViewById(android.R.id.empty));
		listview.setAdapter(adapter = new DeviceListAdapter());

		builder.setTitle(R.string.scanner_title);
		final AlertDialog dialog = builder.setView(dialogView).create();

		listview.setOnItemClickListener((parent, view, position, id) -> {
			stopScan();
			dialog.dismiss();
			final ExtendedBluetoothDevice d = (ExtendedBluetoothDevice) adapter.getItem(position);
			listener.onDeviceSelected(d.device, d.name);
		});

		btnCerca = dialogView.findViewById(R.id.btnCercaDisp);
		btnCerca.setOnClickListener(v -> {
			if (v.getId() == R.id.btnCercaDisp) {
				if (scanning) {
					dialog.cancel();
				} else {
					ScannerFragment.this.startScan();
				}
			}
		});
		if (savedInstanceState == null)
			startScan();
		return dialog;
	}

	@Override
	public void onCancel(@NonNull DialogInterface dialog) {
		super.onCancel(dialog);
		listener.onDialogCanceled();
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
		if (requestCode == REQUEST_PERMISSION_REQ_CODE) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startScan();
			} else {
				//l'applicazione di schiude automaticamente
			}
		}
	}

	/**
	 * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then lEScanCallback
	 * is activated This will perform regular scan for custom BLE Service UUID and then filter out.
	 * using class ScannerServiceParser
	 */
	private void startScan() {
		// A partire da Android 6.0, per la scansione dei Dispositivi BLE bisogna accettare il permesso Manifest.permission.ACCESS_FINE_LOCATION
		if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// When user pressed Deny and still wants to use this functionality, show the rationale
			if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
				return;
			}
			requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
			return;
		}

		adapter.clearDevices();
		btnCerca.setText(R.string.annulla);

		final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
		final ScanSettings settings = new ScanSettings.Builder().setLegacy(false).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
		final List<ScanFilter> filters = new ArrayList<>();
		filters.add(new ScanFilter.Builder().setServiceUuid(uuid).build());
		scanner.startScan(filters, settings, scanCallback);
		scanning = true;
		handler.postDelayed(() -> {
			if (scanning) {
				ScannerFragment.this.stopScan();
			}
		}, TIME_OF_SCAN);
	}

	/**
	 * Stop scan if user tap Cancel button
	 */
	private void stopScan() {
		if (scanning) {
			btnCerca.setText(R.string.cerca);
			final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
			scanner.stopScan(scanCallback);
			scanning = false;
		}
	}

	private ScanCallback scanCallback = new ScanCallback() {
		@Override
		public void onScanResult(final int callbackType, @NonNull final ScanResult result) { /*do nothing*/}

		@Override
		public void onBatchScanResults(@NonNull final List<ScanResult> results) {
			adapter.update(results);
		}

		@Override
		public void onScanFailed(final int errorCode) {/*should never be called*/}
	};

}
