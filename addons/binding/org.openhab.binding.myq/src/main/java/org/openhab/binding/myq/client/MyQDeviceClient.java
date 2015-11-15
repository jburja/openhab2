package org.openhab.binding.myq.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.ws.Response;

import org.apache.http.client.utils.URIBuilder;
import org.openhab.binding.myq.client.request.GetDeviceAttributeRequest;
import org.openhab.binding.myq.client.request.GetDeviceRequest;
import org.openhab.binding.myq.client.request.LoginRequest;
import org.openhab.binding.myq.client.request.SetDeviceAttributeRequest;
import org.openhab.binding.myq.client.response.GetDeviceAttributeResponse;
import org.openhab.binding.myq.client.response.GetDevicesResponse;
import org.openhab.binding.myq.client.response.LoginResponse;
import org.openhab.binding.myq.client.response.SetDeviceAttributeResponse;
import org.openhab.binding.myq.garagedoor.Device;
import org.openhab.binding.myq.garagedoor.DeviceType;
import org.openhab.binding.myq.garagedoor.DoorState;
import org.openhab.binding.myq.internal.MyQConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MyQDeviceClient implements MyQClient {

    private static final Logger logger = LoggerFactory.getLogger(MyQDeviceClient.class);
    private static final String URI_SCHEME = "https";
    private static final String LOGIN_PATH = "/Membership/ValidateUserWithCulture";
    private static final String USER_DEVICE_DETAILS = "/api/UserDeviceDetails";
    private static final String DEVICE_ATTRIBUTES = "/Device/getDeviceAttribute";
    private static final String SET_ATTRIBUTE = "/Device/setDeviceAttribute";
    private static String deviceId = null;
    private static DoorState doorState = null;
    private static DoorState previousDoorState = null;
    private MyQConnectionProperties connectionProps;

    public MyQDeviceClient(MyQConnectionProperties connectionProps) {
        this.connectionProps = connectionProps;
        init();
    }

    public static void main(String[] args) {
        MyQConnectionProperties properties = new MyQConnectionProperties("myqexternal.myqdevice.com",
                "jburja@gmail.com", "h0tp3pp3r", "en");
        MyQDeviceClient c = new MyQDeviceClient(properties);

        LoginRequest loginReq = new LoginRequest();
        LoginResponse loginRes = c.login(loginReq);
        System.out.println(loginRes.getSecurityToken());
        GetDeviceRequest getDevicesReq = new GetDeviceRequest();
        GetDevicesResponse getDevicesRes = c.getDevices(getDevicesReq);
        System.out.println(getDevicesRes.getDevices().get(0).getDeviceName());
        GetDeviceAttributeRequest getDeviceReq = new GetDeviceAttributeRequest();
        getDeviceReq.setName("doorstate");
        GetDeviceAttributeResponse getDeviceRes = c.getDeviceAttributes(getDeviceReq);
        System.out.println(getDeviceRes.getAttributeValue());
        SetDeviceAttributeRequest setDeviceReq = new SetDeviceAttributeRequest();
        setDeviceReq.setAttributeName("desireddoorstate");
        setDeviceReq.setAttributeValue(DoorState.OPEN.getStateCode());
        SetDeviceAttributeResponse setDeviceRes = c.setDeviceStatus(setDeviceReq);
        System.out.println(setDeviceRes.getReturnCode());
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        logger.info("Logging into MyQ");

        LoginResponse response = new LoginResponse();

        URIBuilder uriBuilder = new URIBuilder();

        uriBuilder.setScheme(URI_SCHEME);

        uriBuilder.setHost(connectionProps.getHostname());
        uriBuilder.setPath(LOGIN_PATH);
        uriBuilder.addParameter("appId", connectionProps.getAppId());
        uriBuilder.addParameter("securityToken", connectionProps.getSecurityToken());
        uriBuilder.addParameter("username", connectionProps.getUsername());
        uriBuilder.addParameter("password", connectionProps.getPassword());
        uriBuilder.addParameter("culture", connectionProps.getCulture());

        if (logger.isDebugEnabled()) {
            logger.debug("URL: " + uriBuilder.toString());
        }

        AsyncHttpClient client = null;

        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            client = new AsyncHttpClient();
            Future<Response> futureResponse = client.prepareGet(uriBuilder.toString()).execute();
            Response httpResponse = futureResponse.get();
            String responseBody = httpResponse.getResponseBody();

            if (logger.isDebugEnabled()) {
                logger.debug("Response:" + responseBody);
            }

            response = gson.fromJson(responseBody, LoginResponse.class);

            String securityToken = response.getSecurityToken();
            connectionProps.setSecurityToken(securityToken);

            if (logger.isDebugEnabled()) {
                logger.debug("Security Token:" + securityToken);
            }
        } catch (InterruptedException | ExecutionException | CancellationException | IOException e) {
            logger.error("Failed logging in", e);
        } finally {
            client.close();
        }

        return response;
    }

    @Override
    public void logout() {
        // TODO: Is there a way to kill the security token?
        if (connectionProps != null) {
            connectionProps = null;
        }
    }

    @Override
    public GetDevicesResponse getDevices(GetDeviceRequest request) {
        GetDevicesResponse response = new GetDevicesResponse();
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(URI_SCHEME);
        uriBuilder.setHost(connectionProps.getHostname());
        uriBuilder.setPath(USER_DEVICE_DETAILS);
        uriBuilder.addParameter("appId", connectionProps.getAppId());
        uriBuilder.addParameter("securityToken", connectionProps.getSecurityToken());

        if (logger.isDebugEnabled()) {
            logger.debug("URL: " + uriBuilder.toString());
        }

        AsyncHttpClient client = null;

        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            client = new AsyncHttpClient();
            Future<Response> futureResponse = client.prepareGet(uriBuilder.toString()).execute();

            if (futureResponse != null && futureResponse.get() != null) {
                Response httpResponse = futureResponse.get();
                String responseBody = httpResponse.getResponseBody();

                if (logger.isDebugEnabled()) {
                    logger.debug("Response:" + responseBody);
                }

                response = gson.fromJson(responseBody, GetDevicesResponse.class);
                parseGarageDoorOpener(response);
            }
        } catch (InterruptedException | ExecutionException | CancellationException | IOException e) {
            logger.error("Failed retreiving devices", e);
        } finally {
            client.close();
        }

        return response;
    }

    @Override
    public GetDeviceAttributeResponse getDeviceAttributes(GetDeviceAttributeRequest request) {
        GetDeviceAttributeResponse response = new GetDeviceAttributeResponse();
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(URI_SCHEME);
        uriBuilder.setHost(connectionProps.getHostname());
        uriBuilder.setPath(DEVICE_ATTRIBUTES);
        uriBuilder.addParameter("appId", connectionProps.getAppId());
        uriBuilder.addParameter("securityToken", connectionProps.getSecurityToken());
        uriBuilder.addParameter("devId", deviceId);
        uriBuilder.addParameter("name", request.getName());

        if (logger.isDebugEnabled()) {
            logger.debug("URL: " + uriBuilder.toString());
        }

        AsyncHttpClient client = null;

        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            client = new AsyncHttpClient();
            Future<Response> futureResponse = client.prepareGet(uriBuilder.toString()).execute();

            if (futureResponse != null && futureResponse.get() != null) {
                Response httpResponse = futureResponse.get();
                String responseBody = httpResponse.getResponseBody();

                if (logger.isDebugEnabled()) {
                    logger.debug("Response:" + responseBody);
                }

                response = gson.fromJson(responseBody, GetDeviceAttributeResponse.class);
            }
        } catch (InterruptedException | ExecutionException | CancellationException | IOException e) {
            logger.error("Failed retrieving device attributes", e);
        } finally {
            client.close();
        }

        return response;
    }

    @Override
    public SetDeviceAttributeResponse setDeviceStatus(SetDeviceAttributeRequest request) {
        SetDeviceAttributeResponse response = null;

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(URI_SCHEME);
        uriBuilder.setHost(connectionProps.getHostname());
        uriBuilder.setPath(SET_ATTRIBUTE);
        request.setDeviceId(deviceId);
        request.setSecurityToken(connectionProps.getSecurityToken());
        request.setApplicationId(connectionProps.getAppId());

        if (logger.isDebugEnabled()) {
            logger.debug("URL: " + uriBuilder.toString());
            logger.debug("Device: " + request.getDeviceId());
            logger.debug("Name: " + request.getAttributeName());
            logger.debug("Value: " + request.getAttributeValue());
            logger.debug("Token: " + request.getSecurityToken());
            logger.debug("Application Id: " + request.getApplicationId());
            logger.debug("URL: " + uriBuilder.toString());
        }

        AsyncHttpClient client = null;
        try {

            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            String requestBody = gson.toJson(request);
            logger.info("RequestBody: " + requestBody);
            client = new AsyncHttpClient();

            Future<Response> futureResponse = client.preparePut(uriBuilder.toString()).setBody(requestBody)
                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json").execute();

            if (futureResponse != null && futureResponse.get() != null) {
                Response httpResponse = futureResponse.get();
                String responseBody = httpResponse.getResponseBody();

                if (logger.isDebugEnabled()) {
                    logger.debug("Response:" + responseBody);
                }

                response = gson.fromJson(responseBody, SetDeviceAttributeResponse.class);
                updateState(response, request.getAttributeValue());
            }
        } catch (InterruptedException | ExecutionException | CancellationException | IOException e) {
            logger.error("Failed setting the attribute", e);
        } finally {
            client.close();
        }

        return response;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static void setDeviceId(String deviceId) {
        MyQDeviceClient.deviceId = deviceId;
    }

    public static DoorState getDoorState() {
        return doorState;
    }

    public static DoorState getPreviousDoorState() {
        return previousDoorState;
    }

    private void updateState(SetDeviceAttributeResponse response, String updatedState) {
        boolean success = false;

        if (response != null) {
            logger.debug("Correlation Id: {}", response.getCorrelationId());
            logger.debug("Return code: {}", response.getReturnCode());
            logger.debug("Error message: {}", response.getErrorMessage());
            logger.debug("Updated time: {}", response.getUpdatedTime());

            if (Integer.parseInt(response.getReturnCode()) == 0) {
                logger.debug("Successfully updated state");
                success = true;
            } else {
                logger.debug("Not able to update state");
            }
        } else {
            logger.error("Response from MyQ is null");
        }

        if (success) {
            logger.debug("Successful update.  Updating the door state");
            previousDoorState = doorState;
            doorState = DoorState.forCode(updatedState);
        }
    }

    private void init() {
        logger.debug("Initializing MyQ client");

        LoginRequest loginReq = new LoginRequest();
        LoginResponse loginRes = login(loginReq);

        logger.debug("Got login response");

        if (loginRes.getSecurityToken() != null && !loginRes.getSecurityToken().equals("null")) {
            logger.debug("Successfully logged in");
        }

        logger.debug("Getting devices");
        GetDeviceRequest getDevicesReq = new GetDeviceRequest();
        GetDevicesResponse getDevicesRes = getDevices(getDevicesReq);

        if (getDevicesRes != null) {
            List<Device> devices = getDevicesRes.getDevices();

            for (Device device : devices) {
                logger.debug("Device name:" + device.getMyQDeviceTypeName());
            }
        }

        logger.debug("Getting device attributes");
        GetDeviceAttributeRequest getDeviceReq = new GetDeviceAttributeRequest();
        getDeviceReq.setName("doorstate");
        GetDeviceAttributeResponse getDeviceRes = getDeviceAttributes(getDeviceReq);

        if (getDeviceRes != null) {
            String currentDoorState = getDeviceRes.getAttributeValue();
            DoorState state = DoorState.forCode(currentDoorState);
            logger.debug("Door state:" + state);
            doorState = state;
        } else {
            logger.error("GetDevice response is null");
        }
    }

    private void parseGarageDoorOpener(GetDevicesResponse response) {
        List<Device> devices = response.getDevices();

        if (logger.isDebugEnabled()) {
            logger.debug("Searching through devices:");
        }

        for (Device currentDevice : devices) {

            String deviceName = currentDevice.getMyQDeviceTypeName();
            if (logger.isDebugEnabled()) {
                logger.debug("Current device name: " + deviceName);
            }

            if (DeviceType.GARAGE_DOOR_OPENER.getType().equalsIgnoreCase(deviceName)) {
                deviceId = currentDevice.getDeviceId();
            }
        }
    }

}
