package com.controlroom.androidmodule;

import com.unity3d.player.UnityPlayerActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.input.InputManager;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.os.Build;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.content.res.Configuration;
import android.view.InputDevice;
import android.widget.Toast;
import android.media.AudioManager;


public class ControlRoomActivity extends UnityPlayerActivity {

    public void ShowAlertPopup(String title,String message,String okText,String cancelText, boolean setCancelButton,PluginCallback pluginCallback)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(pluginCallback!=null)
                {
                    pluginCallback.OnOkButtonTouched();
                }
            }
        });

        if(setCancelButton)
        {
            builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(pluginCallback!=null)
                    {
                        pluginCallback.OnCancelButtonTouched();
                    }

                }
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void ShowToast(String message)
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ControlRoomActivity.this,message,Toast.LENGTH_LONG).show();
            }
        });
    }



    public void AddObserverPhysicalKeyboard(PluginCallback callback)
    {
        AddInputDeviceListener(InputDevice.SOURCE_KEYBOARD,callback);
    }

    public void AddObserverGamepad(PluginCallback callback)
    {
        AddInputDeviceListener(InputDevice.SOURCE_GAMEPAD,callback);
    }

    public void AddObserverMouse(PluginCallback callback)
    {
        AddInputDeviceListener(InputDevice.SOURCE_MOUSE,callback);
    }


    public boolean IsAnyKeyboardDeviceConnected()
    {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
        {
           return CheckKeyboardConfig();
        }
        else
        {
            return CheckKeyboardConfig() && IsDeviceConnected(InputDevice.SOURCE_KEYBOARD);
        }

    }

    public boolean CheckKeyboardConfig()
    {
        Configuration configuration = getResources().getConfiguration();
        Log.v("Unity", "CheckKeyboardConfig Config: " +configuration.keyboard);
        return configuration.keyboard != Configuration.KEYBOARD_NOKEYS;
    }

    public boolean IsAnyGamepadDeviceConnected()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            return IsDeviceConnected(InputDevice.SOURCE_GAMEPAD);
        }
        return false;
    }

    public boolean IsAnyMouseDeviceConnected()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            return IsDeviceConnected(InputDevice.SOURCE_MOUSE);
        }

        return false;
    }


    private boolean IsDeviceConnected(int deviceSources)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            InputManager inputManager= (InputManager) getSystemService(Context.INPUT_SERVICE);

            int[] deviceIds=inputManager.getInputDeviceIds();

            for(int deviceId: deviceIds)
            {
                InputDevice device = inputManager.getInputDevice(deviceId);
                if(device!=null)
                {
                    int sources = device.getSources();
                    if(device.isExternal())
                    {
                        switch (deviceSources)
                        {
                            case InputDevice.SOURCE_KEYBOARD:
                                if(IsDeviceKeyboard(sources,device.getName()) && false == IsExcludedDevice(device))
                                {
                                    if(device.getKeyboardType()==InputDevice.KEYBOARD_TYPE_ALPHABETIC)
                                    {

                                        Log.v("Unity", "onInputDeviceAdded(keyboard)-type: " +device.getKeyboardType());
                                        Log.v("Unity", "keyboard device connected: "+deviceId);
                                        return true;
                                    }
                                    else{
                                        Log.v("Unity", "keyboard device not alphabetic: "+device.getKeyboardType());
                                    }

                                }
                                else
                                {
                                    Log.v("Unity", deviceId+"is Not keyboard device : "+ sources);
                                }
                                break;

                            case InputDevice.SOURCE_GAMEPAD:
                                if(IsDeviceGamepad(sources))
                                {
                                    Log.v("Unity", "gamepad device connected: "+deviceId);
                                    return true;
                                }
                                break;

                            case InputDevice.SOURCE_MOUSE:
                                if(IsDeviceMouse(sources))
                                {
                                    Log.v("Unity", "mouse device connected: "+deviceId);
                                    return true;
                                }
                                break;
                            default:
                                Log.e("Unity", "IsDeviceConnected- unknown device sources : " +deviceSources);
                                break;
                        }
                    }
                }

            }
        }

        return false;
    }

    private void AddInputDeviceListener(int deviceSources, PluginCallback callback)
    {
        InputManager inputManager = (InputManager) getSystemService(Context.INPUT_SERVICE);

        InputManager.InputDeviceListener deviceListener = new InputManager.InputDeviceListener() {
            @Override
            public void onInputDeviceAdded(int deviceId) {

                android.view.InputDevice inputDevice = inputManager.getInputDevice(deviceId);
                if (inputDevice != null) {

                    int sources = inputDevice.getSources();
                    switch (deviceSources)
                    {
                        case InputDevice.SOURCE_KEYBOARD:
                            Log.v("Unity", "onInputDeviceAdded(keyboard)-type: " +inputDevice.getKeyboardType());
                            Log.v("Unity", "onInputDeviceAdded(keyboard): " +deviceId);

                            if(IsDeviceKeyboard(sources,inputDevice.getName()) && false == IsExcludedDevice(inputDevice))
                            {
                                if(inputDevice.getKeyboardType()==InputDevice.KEYBOARD_TYPE_ALPHABETIC)
                                {
                                    Log.v("Unity", "onInputDeviceAdded(keyboard) success: " +deviceId);
                                    callback.OnDefaultCallback(true);
                                }

                            }
                            break;
                        case InputDevice.SOURCE_GAMEPAD:
                            if(IsDeviceGamepad(sources))
                            {
                                Log.v("Unity", "onInputDeviceAdded(gamepad): " +deviceId);
                                callback.OnDefaultCallback(true);
                            }
                            break;
                        case InputDevice.SOURCE_MOUSE:
                            if(IsDeviceMouse(sources))
                            {
                                Log.v("Unity", "onInputDeviceAdded(mouse): " +deviceId);
                                callback.OnDefaultCallback(true);
                            }
                            break;
                        default:
                            break;
                    }


                }

            }

            @Override
            public void onInputDeviceRemoved(int deviceId) {


                switch (deviceSources)
                {
                    case InputDevice.SOURCE_KEYBOARD:
                        callback.OnDefaultCallback(false);
                        Log.v("Unity", "onInputDeviceRemoved(keyboard):" + deviceId);
                        break;
                    case InputDevice.SOURCE_GAMEPAD:
                        callback.OnDefaultCallback(false);
                        Log.v("Unity", "onInputDeviceRemoved(gamepad):" + deviceId);
                        break;
                    case InputDevice.SOURCE_MOUSE:
                        callback.OnDefaultCallback(false);
                        Log.v("Unity", "onInputDeviceRemoved(mouse):" + deviceId);
                        break;
                    default:
                        break;
                }
                Log.v("Unity", "onInputDeviceRemoved");


            }

            @Override
            public void onInputDeviceChanged(int deviceId) {

            }
        };

        inputManager.registerInputDeviceListener(deviceListener,null);
    }

    private boolean IsDeviceKeyboard(int sources,String deviceName)
    {
        boolean is_source_keyboard= (sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD;
        if(is_source_keyboard)
        {
            if(false == IsDeviceGamepad(sources))
            {
                return true;
            }
            else
            {
                //Gamepad가 포함된 키보드도있다. 디바이스이름에 키보드가 포함된경우라면 true처리.
                if(deviceName.toLowerCase().contains("keyboard"))
                {
                    return true;
                }
            }

        }

        return false;
    }

    private boolean IsDeviceGamepad(int sources)
    {
        boolean is_source_class_joystick= (sources & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK;
        boolean is_source_gamepad= (sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD;
        boolean is_source_joystick= (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;

        if(is_source_class_joystick || is_source_gamepad || is_source_joystick)
            return true;

        return false;
    }

    private boolean IsDeviceMouse(int sources)
    {
        if((sources& InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE)
            return true;

        return false;
    }

    private boolean IsExcludedDevice(InputDevice device)
    {
        String deviceName= device.getName().toLowerCase();

        Log.v("Unity", "IsExcludedDevice: deviceName:"+deviceName);

        boolean isSmartWatch = deviceName.contains("watch")|| deviceName.contains("gear") || deviceName.contains("wear");


        return isSmartWatch;
    }

    private boolean isAudioDevice(int connectedDeviceId) {
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

            for (AudioDeviceInfo device :devices)
            {
                 Log.v("Unity", "Audio Device id :"+ device.getId());

                 if(connectedDeviceId == device.getId())
                 {
                     Log.v("Unity", "Audio Device id Matching!!");
                     return true;
                 }


            }
        }

        return false;

    }



}
