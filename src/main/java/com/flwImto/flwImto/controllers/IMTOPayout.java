package com.flwImto.flwImto.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flwImto.flwImto.models.Meta;
import com.flwImto.flwImto.models.Payout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

// This class has 2 functions: 1. doDisbursement, 2. processPayoutResponse.
// The doDisbursement function receives calls to send money through bank, and assume KENYA BANK
// The processPayoutResponse receives the callBack after the transaction is complete.
@RestController
public class IMTOPayout {

    @Autowired
    private Environment env;
//Payout through bank
    @RequestMapping(value = "/doDisbursement",consumes = "application/json",produces = "application/json")
    public ResponseEntity doDisbursement(@RequestBody Payout dtls){
        Payout mtoDtls = new Payout();
//        Get url and callback base url from the parameter file (application.properties) in the application class path
        String payoutURL = env.getProperty("FWTVPIURL");
//        CallBack url comprises of the base url + the callback API end point
        String callBackURL = env.getProperty("BASEURL"+"/payoutResponse");

        mtoDtls.setAccount_bank(dtls.getAccount_bank());
        mtoDtls.setAccount_number(dtls.getAccount_number());
        mtoDtls.setAmount(dtls.getAmount());
        mtoDtls.setCurrency(dtls.getCurrency());
        mtoDtls.setReference(dtls.getReference());
        mtoDtls.setNarration(dtls.getNarration());
        mtoDtls.setCallback_url(dtls.getCallback_url());

        // meta data
         Meta mt = new Meta();

        mt.setMobile_number(dtls.getMetaData().getMobile_number());
        mt.setSender(dtls.getMetaData().getSender());
        mt.setSender_country(dtls.getMetaData().getSender_country());

        mtoDtls.setMetaData(mt);

//        Submit payout request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Payout> request = new HttpEntity<>(mtoDtls);
        ResponseEntity<String> response
                = restTemplate.postForEntity(payoutURL,request, String.class);

//		Process Acknowledgement Response
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode status = root.path("status");
            JsonNode message = root.path("message");
//            Log status and status message
            System.out.println("Response Message for Login :"+status.toString()+"\n "+message);


        }catch (JsonProcessingException jx){
            System.out.println(jx.getMessage());
        }
            return response;

    }

//      Callback processesor endpoint
    @RequestMapping(value = "/payoutResponse",consumes = "application/json",produces = "application/json")
    public void processPayoutResponse(@RequestBody String jsonResponse){
        JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();

            JsonElement event = jsonObject.get("event");

        JsonElement eventType = jsonObject.get("event.type");
        JsonObject data = jsonObject.getAsJsonObject("data");
        JsonElement transferStatus = data.get("status");
        JsonElement reference = data.get("reference");
        JsonElement statusMessage = data.get("complete_message");

//            Log status and status message for the incoming callBack
            System.out.println("Response Message for Transaction Reference :"+reference+ "Status :\n" +transferStatus+"\n Status message :"+statusMessage);
//  At this point you can proceess the Response received through the callback

    }

}
