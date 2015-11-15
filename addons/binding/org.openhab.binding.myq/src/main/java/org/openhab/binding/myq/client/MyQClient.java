package org.openhab.binding.myq.client;

import org.openhab.binding.myq.client.request.GetDeviceAttributeRequest;
import org.openhab.binding.myq.client.request.GetDeviceRequest;
import org.openhab.binding.myq.client.request.LoginRequest;
import org.openhab.binding.myq.client.request.SetDeviceAttributeRequest;
import org.openhab.binding.myq.client.response.GetDeviceAttributeResponse;
import org.openhab.binding.myq.client.response.GetDevicesResponse;
import org.openhab.binding.myq.client.response.LoginResponse;
import org.openhab.binding.myq.client.response.SetDeviceAttributeResponse;

public interface MyQClient {
    public LoginResponse login(LoginRequest request);

    public void logout();

    public GetDevicesResponse getDevices(GetDeviceRequest request);

    public GetDeviceAttributeResponse getDeviceAttributes(GetDeviceAttributeRequest request);

    public SetDeviceAttributeResponse setDeviceStatus(SetDeviceAttributeRequest request);
}
